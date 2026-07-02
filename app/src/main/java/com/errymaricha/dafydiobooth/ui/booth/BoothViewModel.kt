package com.errymaricha.dafydiobooth.ui.booth

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.BitmapFactory
import android.os.Build
import com.errymaricha.dafydiobooth.data.api.TemplateSlotDto
import com.errymaricha.dafydiobooth.DafydioApplication
import com.errymaricha.dafydiobooth.data.local.DeviceConfig
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.local.StoredTemplate
import com.errymaricha.dafydiobooth.data.local.TemplateAssetStore
import com.errymaricha.dafydiobooth.data.local.TemplateSqliteStore
import com.errymaricha.dafydiobooth.data.session.SessionStateManager
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.data.station.toStationBaseUrl
import com.errymaricha.dafydiobooth.station.model.HeartbeatCapabilities
import com.errymaricha.dafydiobooth.station.model.HeartbeatRequest
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatus
import com.errymaricha.dafydiobooth.station.network.AppResult
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatusStore
import com.errymaricha.dafydiobooth.station.security.TokenStore
import com.errymaricha.dafydiobooth.station.worker.HeartbeatWorker
import com.errymaricha.dafydiobooth.station.worker.OfflineQueueWorker
import com.errymaricha.dafydiobooth.ui.booth.external.CanonUsbController
import com.errymaricha.dafydiobooth.ui.booth.external.ExternalCaptureStore
import com.errymaricha.dafydiobooth.ui.booth.external.PtpSession
import com.errymaricha.dafydiobooth.BuildConfig
import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.isRejectedPaymentStatus
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases
import java.io.File
import java.net.URI
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class BoothViewModel(
    appContext: Context,
    private val useCases: PhotoboothUseCases,
    private val configStore: DeviceConfigStore,
    private val templateSqliteStore: TemplateSqliteStore,
    private val templateAssetStore: TemplateAssetStore,
    private val stationConnectionChecker: StationConnectionChecker,
    private val sessionStateManager: SessionStateManager,
    private val renderedOutputComposer: TemplateRenderedOutputComposer,
    private val heartbeatStatusStore: HeartbeatStatusStore,
    private val stationTokenStore: TokenStore,
    private val stationDeviceRepository: DeviceRepository,
) : ViewModel() {
    private companion object {
        const val ACTION_USB_PERMISSION = "com.errymaricha.dafydiobooth.USB_PERMISSION"
        const val CANON_VENDOR_ID = 0x04A9
    }

    private val appContext = appContext.applicationContext
    private val stationBootstrap = (this.appContext as DafydioApplication).stationBootstrap
    private val _state = MutableStateFlow(BoothUiState())
    val state: StateFlow<BoothUiState> = _state.asStateFlow()
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedTemplates: List<StoredTemplate> = emptyList()
    private var cachedTemplateSlotsById: Map<String, List<TemplateSlotLayout>> = emptyMap()
    private var cachedTemplateSlotCountById: Map<String, Int> = emptyMap()
    private val canonUsbController = CanonUsbController(appContext)
    private val externalCaptureStore = ExternalCaptureStore(appContext)
    private var canonPtpSession: PtpSession? = null
    private var externalPreviewJob: Job? = null
    private var externalCaptureJob: Job? = null
    private var externalConnectInProgress: Boolean = false
    private var lastPreviewUiUpdateAt: Long = 0L
    private var lastUsbAttachHandledAt: Long = 0L
    private var usbReceiverRegistered = false
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_USB_PERMISSION -> handleUsbPermissionResult(intent)
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleUsbAttached(intent)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> handleUsbDetached(intent)
            }
        }
    }

    init {
        registerUsbReceiver()
        viewModelScope.launch {
            configStore.config.collect { config ->
                stationBootstrap.setStationBaseUrl(config.stationIp.toStationBaseUrl())
                sessionStateManager.updateConnection(
                    stationIp = config.stationIp,
                    deviceId = config.deviceId,
                    apiKey = config.token,
                    authToken = config.authToken,
                    isStationReachable = state.value.isStationReachable,
                )
                _state.update { current ->
                    // welcomeBgUri hanya di-update dari DataStore jika event ID-nya cocok
                    // dengan state saat ini, untuk menghindari overwrite saat ganti event
                    val bgUri = if (config.launchSelectedEventId == current.launchSelectedEventId) config.welcomeBgUri else current.welcomeBgUri
                    val bgIsVideo = if (config.launchSelectedEventId == current.launchSelectedEventId) config.welcomeBgIsVideo else current.welcomeBgIsVideo
                    current.copy(
                        cameraSource = config.cameraSource.toEnum(CameraSource.AndroidDefault),
                        externalCameraStatus = config.externalCameraStatus.toEnum(ExternalCameraStatus.Disconnected),
                        externalPreviewFps = config.externalPreviewFps.coerceIn(10, 24),
                        customerId = config.customerId,
                        customerWhatsapp = config.customerWhatsapp,
                        launchEventName = config.launchEventName,
                        launchSelectedEventId = config.launchSelectedEventId,
                        launchAllowedTemplateIds = config.launchAllowedTemplateIds,
                        launchAdditionalPrintCount = config.launchAdditionalPrintCount.coerceAtLeast(0),
                        voucherCode = config.voucherCode,
                        voucherType = config.voucherType,
                        sessionType = config.sessionType,
                        paymentMethod = config.paymentMethod,
                        kioskExitCode = config.kioskExitCode,
                        mirrorLiveView = config.mirrorLiveView,
                        mirrorCapture = config.mirrorCapture,
                        imageQuality = config.imageQuality.toEnum(ImageQuality.High),
                        useBackCamera = config.useBackCamera,
                        useFrontCamera = config.useFrontCamera,
                        denoisePhoto = config.denoisePhoto,
                        countdownSeconds = config.countdownSeconds,
                        countdownAudio = config.countdownAudio,
                        shutterSound = config.shutterSound,
                        defaultPrinting = config.defaultPrinting,
                        printUsePhotoboothStation = config.printUsePhotoboothStation,
                        welcomeBgUri = bgUri,
                        welcomeBgIsVideo = bgIsVideo,
                        printerMarginTop = config.printerMarginTop,
                        printerMarginBottom = config.printerMarginBottom,
                        printerMarginLeft = config.printerMarginLeft,
                        printerMarginRight = config.printerMarginRight,
                        printerScaleMode = config.printerScaleMode,
                    )
                }
            }
        }
        viewModelScope.launch {
            sessionStateManager.state.collect { session ->
                _state.update {
                    it.copy(
                        stationIp = session.stationIp,
                        deviceId = session.deviceId,
                        token = session.apiKey,
                        authToken = session.authToken,
                        isStationConnected = session.isStationAuthenticated,
                        customerId = session.customerId.ifBlank { it.customerId },
                        customerWhatsapp = session.customerWhatsapp.ifBlank { it.customerWhatsapp },
                        launchSelectedEventId = session.selectedEventId.ifBlank { it.launchSelectedEventId },
                        session = session.toBoothSession() ?: it.session,
                    )
                }
            }
        }
        viewModelScope.launch {
            templateSqliteStore.templates.collect { templates ->
                cachedTemplates = templates
                val parsedById = withContext(Dispatchers.Default) {
                    templates.associate { stored ->
                        stored.templateId to parseSlots(stored.slotsJson)
                    }
                }
                cachedTemplateSlotsById = parsedById
                cachedTemplateSlotCountById = parsedById.mapValues { (_, slots) ->
                    uniqueCaptureSlotCount(slots)
                }
                parsedById.forEach { (templateId, slots) ->
                    val captureSlots = slots.map { it.sourceSlotIndex }.distinct().sorted()
                    logSlotDebug(
                        "templateId=$templateId visualSlots=${slots.map { it.slotIndex }} sourceSlots=${slots.map { it.sourceSlotIndex }} captureSlots=$captureSlots",
                    )
                }
                _state.update {
                    val selected = it.selectedTemplate
                        ?.let { selectedName -> templates.firstOrNull { template -> template.templateName == selectedName } }
                    val slotCount = selected?.templateId?.let { id -> cachedTemplateSlotCountById[id] } ?: it.templateSlotCount
                    val parsedSlots = selected?.templateId?.let { id -> cachedTemplateSlotsById[id] }.orEmpty()
                    val templateItems = templates.map { stored ->
                        val readiness = buildTemplateAssetReadiness(stored)
                        TemplateListItem(
                            templateId = stored.templateId,
                            templateName = stored.templateName,
                            templateCode = stored.templateCode,
                            category = stored.category,
                            paperSize = stored.paperSize,
                            thumbnailUrl = resolveLocalTemplateThumbnailPath(stored),
                            thumbnailReady = readiness.thumbnailReady,
                            previewReady = readiness.previewReady,
                            overlayReady = readiness.overlayReady,
                            slotCount = cachedTemplateSlotCountById[stored.templateId] ?: 1,
                        )
                    }
                    it.copy(
                        availableTemplates = templates.map(StoredTemplate::templateName),
                        availableTemplateItems = templateItems,
                        templatesUpdated = templates.isNotEmpty(),
                        templateSlotCount = slotCount.coerceAtLeast(1),
                        selectedTemplatePreviewUrl = selected?.previewUrl?.let { url -> resolveAssetUrl(url, it.stationIp) }
                            ?: it.selectedTemplatePreviewUrl,
                        selectedTemplatePreviewLocalPath = selected?.previewLocalPath ?: it.selectedTemplatePreviewLocalPath,
                        selectedTemplateOverlayUrl = selected?.overlayUrl?.let { url -> resolveAssetUrl(url, it.stationIp) }
                            ?: it.selectedTemplateOverlayUrl,
                        selectedTemplateOverlayLocalPath = selected?.overlayLocalPath ?: it.selectedTemplateOverlayLocalPath,
                        selectedTemplateCanvasWidth = selected?.canvasWidth ?: it.selectedTemplateCanvasWidth,
                        selectedTemplateCanvasHeight = selected?.canvasHeight ?: it.selectedTemplateCanvasHeight,
                        selectedTemplateSlots = if (parsedSlots.isEmpty()) it.selectedTemplateSlots else parsedSlots,
                        launchAllowedTemplateIds = it.launchAllowedTemplateIds
                            .intersect(templates.map(StoredTemplate::templateId).toSet()),
                    )
                }
            }
        }
        viewModelScope.launch {
            heartbeatStatusStore.status.collect { hb ->
                _state.update {
                    it.copy(
                        heartbeatLocalIp = hb.localIp,
                        heartbeatAppVersion = hb.appVersion,
                        heartbeatOsVersion = hb.os,
                        heartbeatCapabilities = hb.capabilities,
                        heartbeatLastAt = hb.lastHeartbeatAt,
                        heartbeatLastSyncAt = hb.lastSyncAt,
                        heartbeatLastResult = hb.lastResult,
                        heartbeatLastSuccessAt = hb.lastSuccessAt,
                        consecutiveHeartbeatFailures = hb.consecutiveFailures,
                    )
                }
                sessionStateManager.updateReachability(_state.value.isStationReachable)
            }
        }
    }

    fun continueFromSplash() = _state.update { it.copy(step = BoothStep.Dashboard, errorMessage = null) }

    fun onStepChanged(step: BoothStep) {
        val session = canonPtpSession
        if (
            step == BoothStep.Camera &&
            state.value.cameraSource == CameraSource.ExternalCanon &&
            state.value.externalCameraStatus == ExternalCameraStatus.Connected &&
            session != null &&
            externalPreviewJob?.isActive != true
        ) {
            startExternalPreviewLoop(session)
            return
        }
        if (step != BoothStep.Camera) {
            externalPreviewJob?.cancel()
            externalPreviewJob = null
            _state.update { it.copy(externalPreviewPath = null, externalPreviewBytes = null) }
        }
    }

    fun openDashboard() = _state.update { it.copy(step = BoothStep.Dashboard, errorMessage = null) }

    fun startNowPhoto() = _state.update { current ->
        val hasLocalTemplate = current.availableTemplateItems.isNotEmpty() || current.availableTemplates.isNotEmpty()
        if (hasLocalTemplate) {
            sessionStateManager.clearSession()
            current.copy(
                step = BoothStep.TemplatePicker,
                localOnlySession = true,
                selectedColorFilter = ColorFilterType.Normal,
                errorMessage = null,
            )
        } else {
            sessionStateManager.clearSession()
            current.copy(
                selectedTemplateId = null,
                selectedTemplate = "Quick Photo",
                selectedTemplatePaperSize = null,
                selectedTemplatePreviewUrl = null,
                selectedTemplatePreviewLocalPath = null,
                selectedTemplateOverlayUrl = null,
                selectedTemplateOverlayLocalPath = null,
                selectedTemplateCanvasWidth = 0,
                selectedTemplateCanvasHeight = 0,
                selectedTemplateSlots = emptyList(),
                step = BoothStep.Camera,
                nextCaptureIndex = 1,
                templateSlotCount = 1,
                capturedPhotoName = null,
                capturedPhotoPath = null,
                capturedPhotosBySlot = emptyMap(),
                uploadedSessionPhotosBySlot = emptyMap(),
                localOnlySession = true,
                selectedColorFilter = ColorFilterType.Normal,
                eventStatusMessage = if (current.isStationConnected) {
                    "Template lokal belum tersedia. Masuk Quick Photo mode."
                } else {
                    "Offline mode aktif. Masuk Quick Photo mode."
                },
                errorMessage = null,
            )
        }
    }

    fun openConnectedTemplateFlow() = _state.update {
        if (!it.isStationConnected) {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        } else {
            it.copy(
                step = BoothStep.TemplatePicker,
                localOnlySession = false,
                errorMessage = null,
                eventStatusMessage = "Approval diterima. Silakan pilih template.",
            )
        }
    }

    fun openCustomTemplate() = _state.update { it.copy(step = BoothStep.CustomTemplate, errorMessage = null) }

    fun openSettings() = _state.update { it.copy(step = BoothStep.Settings, errorMessage = null) }

    fun sendHeartbeatNow(localIp: String) = viewModelScope.launch {
        if (!isValidIpv4(localIp)) {
            _state.update { it.copy(errorMessage = "IP lokal belum valid untuk heartbeat: $localIp") }
            return@launch
        }
        val now = java.time.Instant.now().toString()
        val request = HeartbeatRequest(
            deviceType = "android",
            localIp = localIp,
            batteryPercent = 0,
            networkStrength = 0,
            appVersion = BuildConfig.VERSION_NAME,
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            capabilities = HeartbeatCapabilities(
                camera = true,
                printer = false,
                offlineQueue = true,
                localRender = true,
            ),
            metrics = emptyMap(),
            lastSyncAt = now,
        )
        val previousHeartbeat = heartbeatStatusStore.snapshot()
        when (val result = stationDeviceRepository.sendHeartbeat(request)) {
            is AppResult.Success -> {
                heartbeatStatusStore.save(
                    HeartbeatStatus(
                        localIp = request.localIp,
                        appVersion = request.appVersion,
                        os = "${request.osName} ${request.osVersion}",
                        capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                        lastHeartbeatAt = now,
                        lastSyncAt = now,
                        lastResult = "success",
                        lastSuccessAt = now,
                        consecutiveFailures = 0,
                    ),
                )
                _state.update { it.copy(eventStatusMessage = "Heartbeat sent.", errorMessage = null) }
            }
            is AppResult.Failure -> {
                heartbeatStatusStore.save(
                    HeartbeatStatus(
                        localIp = request.localIp,
                        appVersion = request.appVersion,
                        os = "${request.osName} ${request.osVersion}",
                        capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                        lastHeartbeatAt = now,
                        lastSyncAt = now,
                        lastResult = "failed",
                        lastSuccessAt = previousHeartbeat.lastSuccessAt,
                        consecutiveFailures = previousHeartbeat.consecutiveFailures + 1,
                    ),
                )
                _state.update { it.copy(errorMessage = "Heartbeat gagal: ${result.error}") }
            }
        }
    }

    private fun isValidIpv4(ip: String): Boolean {
        val regex = Regex("""^((25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)$""")
        return regex.matches(ip.trim())
    }

    fun disconnectStation() = updateAndPersistConfig {
        HeartbeatWorker.cancel(appContext)
        OfflineQueueWorker.cancel(appContext)
        stationTokenStore.clear()
        sessionStateManager.clearSession()
        sessionStateManager.updateConnection(
            stationIp = it.stationIp,
            deviceId = it.deviceId,
            apiKey = it.token,
            authToken = "",
            isStationReachable = false,
        )
        buildDisconnectedState(it)
    }

    fun refreshTemplates() = launchRequest {
        val current = state.value
        if (!current.isStationConnected) {
            _state.update { it.copy(errorMessage = "Connect Photobooth Station dulu") }
            return@launchRequest
        }
        val stationBaseUrl = current.stationIp.toStationBaseUrl()
        when (val result = useCases.refreshTemplates(current.authToken)) {
            is BoothResult.Success -> {
                val existingById = cachedTemplates.associateBy { it.templateId }
                var templates = result.value.map {
                    val existing = existingById[it.templateId]
                    val thumbnailChanged = existing?.thumbnailUrl != it.thumbnailUrl
                    val previewChanged = existing?.previewUrl != it.previewUrl
                    val overlayChanged = existing?.overlayUrl != it.overlayUrl
                    val thumbnailLocalPath = templateAssetStore.cacheThumbnail(
                        templateId = it.templateId,
                        thumbnailUrl = it.thumbnailUrl,
                        stationBaseUrl = stationBaseUrl,
                        authToken = current.authToken,
                        forceRefresh = thumbnailChanged,
                    ) ?: existing?.thumbnailLocalPath
                    val previewLocalPath = templateAssetStore.cachePreview(
                        templateId = it.templateId,
                        previewUrl = it.previewUrl,
                        stationBaseUrl = stationBaseUrl,
                        authToken = current.authToken,
                        forceRefresh = previewChanged,
                    ) ?: thumbnailLocalPath ?: existing?.previewLocalPath
                    val overlayLocalPath = templateAssetStore.cacheOverlay(
                        templateId = it.templateId,
                        overlayUrl = it.overlayUrl,
                        stationBaseUrl = stationBaseUrl,
                        authToken = current.authToken,
                        forceRefresh = overlayChanged,
                    ) ?: existing?.overlayLocalPath
                    StoredTemplate(
                        templateId = it.templateId,
                        templateCode = it.templateCode,
                        templateName = it.templateName,
                        category = it.category,
                        paperSize = it.paperSize,
                        canvasWidth = it.canvasWidth,
                        canvasHeight = it.canvasHeight,
                        thumbnailUrl = it.thumbnailUrl,
                        thumbnailLocalPath = thumbnailLocalPath,
                        previewUrl = it.previewUrl,
                        previewLocalPath = previewLocalPath,
                        overlayUrl = it.overlayUrl,
                        overlayLocalPath = overlayLocalPath,
                        configJson = it.configJson,
                        slotsJson = it.slotsJson,
                    )
                }
                val sourceById = result.value.associateBy { it.templateId }
                repeat(2) {
                    val incompleteIds = templates
                        .filterNot(::isTemplateAssetReady)
                        .map { it.templateId }
                        .toSet()
                    if (incompleteIds.isEmpty()) return@repeat
                    templates = templates.map { stored ->
                        if (!incompleteIds.contains(stored.templateId)) return@map stored
                        val source = sourceById[stored.templateId] ?: return@map stored
                        val thumbnailLocalPath = if (source.thumbnailUrl.isNullOrBlank()) {
                            stored.thumbnailLocalPath
                        } else {
                            templateAssetStore.cacheThumbnail(
                                templateId = source.templateId,
                                thumbnailUrl = source.thumbnailUrl,
                                stationBaseUrl = stationBaseUrl,
                                authToken = current.authToken,
                            ) ?: stored.thumbnailLocalPath
                        }
                        val previewLocalPath = if (source.previewUrl.isNullOrBlank()) {
                            stored.previewLocalPath ?: stored.thumbnailLocalPath
                        } else {
                            templateAssetStore.cachePreview(
                                templateId = source.templateId,
                                previewUrl = source.previewUrl,
                                stationBaseUrl = stationBaseUrl,
                                authToken = current.authToken,
                            ) ?: stored.thumbnailLocalPath ?: stored.previewLocalPath
                        }
                        val overlayLocalPath = if (source.overlayUrl.isNullOrBlank()) {
                            stored.overlayLocalPath
                        } else {
                            templateAssetStore.cacheOverlay(
                                templateId = source.templateId,
                                overlayUrl = source.overlayUrl,
                                stationBaseUrl = stationBaseUrl,
                                authToken = current.authToken,
                            ) ?: stored.overlayLocalPath
                        }
                        stored.copy(
                            thumbnailLocalPath = thumbnailLocalPath,
                            previewLocalPath = previewLocalPath,
                            overlayLocalPath = overlayLocalPath,
                        )
                    }
                }
                templateSqliteStore.replaceTemplates(templates)
                val totalTemplates = templates.size
                val readyTemplates = templates.count(::isTemplateAssetReady)
                val corruptedOrMissing = (totalTemplates - readyTemplates).coerceAtLeast(0)
                val missingTemplateNames = templates
                    .filterNot(::isTemplateAssetReady)
                    .map { it.templateName }
                    .take(3)
                _state.update {
                    val templateItems = templates.map { stored ->
                        val readiness = buildTemplateAssetReadiness(stored)
                        TemplateListItem(
                            templateId = stored.templateId,
                            templateName = stored.templateName,
                            templateCode = stored.templateCode,
                            category = stored.category,
                            paperSize = stored.paperSize,
                            thumbnailUrl = resolveLocalTemplateThumbnailPath(stored),
                            thumbnailReady = readiness.thumbnailReady,
                            previewReady = readiness.previewReady,
                            overlayReady = readiness.overlayReady,
                            slotCount = cachedTemplateSlotCountById[stored.templateId] ?: 1,
                        )
                    }
                    it.copy(
                        availableTemplates = templates.map(StoredTemplate::templateName),
                        availableTemplateItems = templateItems,
                        templatesUpdated = templates.isNotEmpty(),
                        eventStatusMessage = if (corruptedOrMissing == 0) {
                            "Template terupdate. Asset lokal siap di device: $readyTemplates/$totalTemplates template."
                        } else {
                            val sample = if (missingTemplateNames.isEmpty()) "" else " Contoh: ${missingTemplateNames.joinToString(", ")}."
                            "Template terupdate, tapi $corruptedOrMissing template asset belum valid/corrupt.$sample Cek jaringan lalu Update Template lagi."
                        },
                        errorMessage = if (corruptedOrMissing == 0) {
                            null
                        } else {
                            "Asset template lokal belum lengkap/valid: $readyTemplates/$totalTemplates siap."
                        },
                    )
                }
            }
            is BoothResult.Failure -> showStationConnectionError(result.error)
        }
    }

    fun openLaunchEvent() = _state.update {
        if (it.isStationConnected) {
            it.copy(step = BoothStep.LaunchEvent, localOnlySession = false, errorMessage = null, eventStatusMessage = null)
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun openSettingEvent() = _state.update {
        if (it.isStationConnected) {
            it.copy(
                step = BoothStep.SettingEvent,
                localOnlySession = false,
                errorMessage = null,
                eventStatusMessage = "Atur default event sebelum masuk Launch Event.",
            )
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun openSettingAllowedTemplates() = _state.update {
        if (it.isStationConnected) {
            it.copy(step = BoothStep.SettingAllowedTemplates, errorMessage = null)
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun syncLaunchSession(
        session: LaunchSession?,
        customerWhatsapp: String,
        authToken: String?,
        selectedEventId: String,
    ) {
        sessionStateManager.updateFromLaunchSession(session, customerWhatsapp, authToken, selectedEventId)
        updateAndPersistConfig {
            it.copy(
                launchSelectedEventId = selectedEventId.ifBlank { it.launchSelectedEventId },
                nextCaptureIndex = 1,
                errorMessage = null,
            )
        }
    }

    fun selectTemplate(templateId: String) {
        _state.update {
            val selected = cachedTemplates.firstOrNull { stored -> stored.templateId == templateId }
            val slotCount = selected?.templateId?.let { id -> cachedTemplateSlotCountById[id] } ?: 1
            val slots = selected?.templateId?.let { id -> cachedTemplateSlotsById[id] }.orEmpty()
            val slotValidationError = validateTemplateSlots(slots)
            if (slotValidationError != null) {
                return@update it.copy(
                    errorMessage = "Template mapping invalid: $slotValidationError. Sync template ulang dari station.",
                )
            }
            val localPreviewPath = selected?.previewLocalPath
                ?: selected?.templateId?.let { id -> templateAssetStore.getCachedPreviewPath(id) }
            val localOverlayPath = selected?.overlayLocalPath
                ?: selected?.templateId?.let { id -> templateAssetStore.getCachedOverlayPath(id) }
            it.copy(
                selectedTemplateId = selected?.templateId,
                selectedTemplate = selected?.templateName ?: it.selectedTemplate,
                selectedTemplatePaperSize = selected?.paperSize,
                selectedTemplatePreviewUrl = selected?.previewUrl?.let { url -> resolveAssetUrl(url, it.stationIp) },
                selectedTemplatePreviewLocalPath = localPreviewPath,
                selectedTemplateOverlayUrl = selected?.overlayUrl?.let { url -> resolveAssetUrl(url, it.stationIp) },
                selectedTemplateOverlayLocalPath = localOverlayPath,
                selectedTemplateCanvasWidth = selected?.canvasWidth ?: 0,
                selectedTemplateCanvasHeight = selected?.canvasHeight ?: 0,
                selectedTemplateSlots = slots,
                step = BoothStep.Camera,
                nextCaptureIndex = 1,
                templateSlotCount = slotCount.coerceAtLeast(1),
                capturedPhotoName = null,
                capturedPhotoPath = null,
                capturedPhotosBySlot = emptyMap(),
                uploadedSessionPhotosBySlot = emptyMap(),
                selectedColorFilter = ColorFilterType.Normal,
                errorMessage = null,
            )
        }
        ensureTemplateAssetsCached(templateId)
    }

    fun saveCustomTemplate(name: String) = _state.update {
        it.copy(
            selectedTemplate = name.ifBlank { "Custom Template" },
            selectedTemplateId = null,
            selectedTemplatePaperSize = null,
            selectedTemplatePreviewUrl = null,
            selectedTemplatePreviewLocalPath = null,
            selectedTemplateOverlayUrl = null,
            selectedTemplateOverlayLocalPath = null,
            selectedTemplateCanvasWidth = 0,
            selectedTemplateCanvasHeight = 0,
            selectedTemplateSlots = emptyList(),
            step = BoothStep.Camera,
            nextCaptureIndex = 1,
            templateSlotCount = 1,
            capturedPhotoName = null,
            capturedPhotoPath = null,
            capturedPhotosBySlot = emptyMap(),
            uploadedSessionPhotosBySlot = emptyMap(),
            selectedColorFilter = ColorFilterType.Normal,
            errorMessage = null,
        )
    }

    fun capturePhoto() {
        if (state.value.cameraSource == CameraSource.ExternalCanon) {
            if (externalCaptureJob?.isActive == true || state.value.isLoading) return
            externalCaptureJob = viewModelScope.launch {
                try {
                    val countdown = state.value.countdownSeconds.coerceAtLeast(0)
                    _state.update { it.copy(isLoading = true, errorMessage = null) }
                    if (countdown > 0) {
                        for (second in countdown downTo 1) {
                            _state.update {
                                it.copy(
                                    eventStatusMessage = "Capture Canon dalam ${second}...",
                                    errorMessage = null,
                                )
                            }
                            delay(1000)
                        }
                    }
                    captureExternalPhotoInternal()
                } finally {
                    externalCaptureJob = null
                }
            }
            return
        }
        _state.update {
            it.copy(capturedPhotoName = "capture-${System.currentTimeMillis()}.jpg", step = BoothStep.CapturePreview)
        }
    }

    fun capturePhoto(filePath: String) {
        val name = filePath.substringAfterLast('/').substringAfterLast('\\')
        _state.update {
            it.copy(
                capturedPhotoName = name,
                capturedPhotoPath = filePath,
                step = if (it.step == BoothStep.LaunchEvent) BoothStep.LaunchEvent else BoothStep.CapturePreview,
                errorMessage = null
            )
        }
        if (state.value.step == BoothStep.LaunchEvent) {
            acceptCapturePreview()
        }
    }

    fun retakePhoto() = _state.update { it.copy(step = BoothStep.Camera, capturedPhotoName = null, capturedPhotoPath = null) }

    fun retakeSpecificSlot(sourceSlotIndex: Int) {
        val current = state.value
        val slots = captureSlotsOf(current)
        val indexInCaptureSlots = slots.indexOf(sourceSlotIndex)
        if (indexInCaptureSlots != -1) {
            val targetNextCaptureIndex = indexInCaptureSlots + 1
            _state.update {
                it.copy(
                    nextCaptureIndex = targetNextCaptureIndex,
                    capturedPhotoPath = null,
                    capturedPhotoName = null,
                    step = BoothStep.Camera,
                    eventStatusMessage = "Retake foto untuk Slot $sourceSlotIndex.",
                    errorMessage = null,
                )
            }
        }
    }

    fun acceptCapturePreview() {
        if (state.value.isLoading) return
        launchRequest {
            val current = state.value
            val requiredCaptures = current.templateSlotCount.coerceAtLeast(1)
            val capturePath = current.capturedPhotoPath
            val session = sessionStateManager.snapshot()
            if (capturePath.isNullOrBlank()) {
                proceedAfterCaptureAccepted(
                    current = current,
                    requiredCaptures = requiredCaptures,
                    successMessage = "Foto #${current.nextCaptureIndex} diterima.",
                    capturePath = capturePath,
                )
                return@launchRequest
            }
            if (current.localOnlySession || !current.isStationConnected || session.sessionId.isNullOrBlank()) {
                proceedAfterCaptureAccepted(
                    current = current,
                    requiredCaptures = requiredCaptures,
                    successMessage = "Foto #${current.nextCaptureIndex} tersimpan lokal.",
                    capturePath = capturePath,
                )
                return@launchRequest
            }

            _state.update {
                it.copy(
                    eventStatusMessage = "Mengirim foto #${it.nextCaptureIndex} ke Photobooth Station...",
                    errorMessage = null,
                )
            }
            when (
                val result = useCases.uploadCapture(
                    authToken = session.authToken,
                    deviceId = session.deviceId,
                    sessionId = session.sessionId,
                    captureIndex = current.nextCaptureIndex,
                    slotIndex = currentCaptureSlotIndex(current),
                    photoFile = File(capturePath),
                )
            ) {
                is BoothResult.Success -> {
                    val uploadedId = result.value.sessionPhotoId
                    val captureSlot = currentCaptureSlotIndex(current)
                    logSlotDebug(
                        "uploadCapture captureIndex=${current.nextCaptureIndex} sourceSlot=$captureSlot uploadedSessionPhotoId=$uploadedId",
                    )
                    _state.update { st ->
                        st.copy(
                            uploadedSessionPhotosBySlot = if (uploadedId.isNullOrBlank()) {
                                st.uploadedSessionPhotosBySlot
                            } else {
                                st.uploadedSessionPhotosBySlot + (captureSlot to uploadedId)
                            },
                        )
                    }
                    proceedAfterCaptureAccepted(
                        current = current,
                        requiredCaptures = requiredCaptures,
                        successMessage = "Foto #${current.nextCaptureIndex} berhasil dikirim.",
                        capturePath = capturePath,
                    )
                }
                is BoothResult.Failure -> {
                    val message = when (result.error) {
                        is BoothError.Unauthorized -> "Upload capture gagal (401): ${result.error.message}"
                        is BoothError.Forbidden -> "Upload capture gagal (403): ${result.error.message}"
                        is BoothError.Validation -> {
                            val detail = result.error.message
                            if (detail.contains("(409)")) {
                                "Upload capture gagal (409): $detail"
                            } else {
                                "Upload capture gagal (422): $detail"
                            }
                        }
                        is BoothError.Network -> "Upload capture gagal (network): ${result.error.message}"
                        is BoothError.Unknown -> "Upload capture gagal: ${result.error.message}"
                    }
                    _state.update {
                        it.copy(
                            step = if (it.step == BoothStep.LaunchEvent) BoothStep.LaunchEvent else BoothStep.CapturePreview,
                            eventStatusMessage = "Foto belum terkirim ke Photobooth Station.",
                            errorMessage = message,
                        )
                    }
                }
            }
        }
    }

    fun finishSession() = launchRequest {
        val current = state.value
        val requiredCaptures = current.templateSlotCount.coerceAtLeast(1)
        if (current.capturedPhotosBySlot.size < requiredCaptures) {
            _state.update {
                it.copy(
                    errorMessage = "Capture belum lengkap (${current.capturedPhotosBySlot.size}/$requiredCaptures).",
                )
            }
            return@launchRequest
        }
        val session = sessionStateManager.snapshot()
        if (current.localOnlySession) {
            val renderState = prepareStateForRender(current)
            val renderedFile = renderedOutputComposer.compose(renderState)
            _state.update {
                it.copy(
                    step = BoothStep.Finish,
                    eventStatusMessage = if (renderedFile != null) "Session selesai (local-only mode). Hasil disimpan secara lokal." else "Session selesai (local-only mode). Gagal menyimpan hasil.",
                    errorMessage = null,
                )
            }
            return@launchRequest
        }
        val requiredCaptureSlots = captureSlotsOf(current)
        logSlotDebug(
            "finishSession requiredCaptureSlots=$requiredCaptureSlots uploadedSlots=${current.uploadedSessionPhotosBySlot.keys.sorted()}",
        )
        val missingUploadedSlots = requiredCaptureSlots
            .filterNot { current.uploadedSessionPhotosBySlot.containsKey(it) }
        if (current.isStationConnected && session.sessionId != null && missingUploadedSlots.isNotEmpty()) {
            _state.update {
                it.copy(
                    step = BoothStep.CapturePreview,
                    errorMessage = "Upload slot belum lengkap: ${missingUploadedSlots.joinToString(", ")}. Ulangi capture slot tersebut.",
                )
            }
            return@launchRequest
        }
        val renderItems = requiredCaptureSlots
            .mapNotNull { sourceSlot ->
                val sessionPhotoId = current.uploadedSessionPhotosBySlot[sourceSlot]
                if (sessionPhotoId.isNullOrBlank()) {
                    null
                } else {
                    com.errymaricha.dafydiobooth.domain.model.RenderItem(
                        sessionPhotoId = sessionPhotoId,
                        slotIndex = sourceSlot,
                    )
                }
            }
        logSlotDebug(
            "renderItems=${renderItems.map { "sourceSlot=${it.slotIndex}->sessionPhoto=${it.sessionPhotoId}" }} visualToSource=${
                current.selectedTemplateSlots.sortedBy { it.slotIndex }.map { "visual=${it.slotIndex}->source=${it.sourceSlotIndex}" }
            }",
        )
        if (renderItems.size != requiredCaptureSlots.size && requiredCaptureSlots.isNotEmpty()) {
            _state.update {
                it.copy(
                    step = BoothStep.CapturePreview,
                    errorMessage = "Mapping slot duplikat belum lengkap. Ulangi capture source slot yang belum terupload.",
                )
            }
            return@launchRequest
        }
        if (!current.isStationConnected || session.sessionId.isNullOrBlank()) {
            val renderState = prepareStateForRender(current)
            val renderedFile = renderedOutputComposer.compose(renderState)
            _state.update {
                it.copy(
                    step = BoothStep.Finish,
                    eventStatusMessage = if (renderedFile != null) "Preview selesai. Hasil disimpan secara lokal." else "Preview selesai. Gagal menyimpan hasil.",
                    errorMessage = null,
                )
            }
            return@launchRequest
        }

        _state.update { it.copy(eventStatusMessage = "Menyiapkan hasil preview final...", errorMessage = null) }
        val renderedFile = renderedOutputComposer.compose(current)
        if (renderedFile == null) {
            val missingPreview = current.selectedTemplatePreviewLocalPath.isNullOrBlank() && !current.selectedTemplatePreviewUrl.isNullOrBlank()
            val missingOverlay = current.selectedTemplateOverlayLocalPath.isNullOrBlank() && !current.selectedTemplateOverlayUrl.isNullOrBlank()
            val renderError = when {
                missingPreview && missingOverlay -> "Render preview lokal gagal. Preview dan overlay template belum tersedia di device."
                missingPreview -> "Render preview lokal gagal. Preview template belum tersedia di device."
                missingOverlay -> "Render preview lokal gagal. Overlay template belum tersedia di device."
                else -> "Render preview lokal gagal. Periksa slot/template lalu coba lagi."
            }
            _state.update {
                it.copy(
                    step = BoothStep.TemplatePreview,
                    errorMessage = renderError,
                )
            }
            return@launchRequest
        }

        _state.update { it.copy(eventStatusMessage = "Finalisasi session ke Photobooth Station...", errorMessage = null) }
        when (
            val completeResult = useCases.completeSession(
                authToken = session.authToken,
                deviceId = session.deviceId,
                sessionId = session.sessionId,
            )
        ) {
            is BoothResult.Success -> {
                val templateId = current.selectedTemplateId.orEmpty()
                if (templateId.isBlank()) {
                    _state.update {
                        it.copy(
                            step = BoothStep.TemplatePreview,
                            errorMessage = "Template ID tidak ditemukan untuk proses edit job.",
                        )
                    }
                    if (renderedFile.exists()) renderedFile.delete()
                    return@launchRequest
                }
                _state.update { it.copy(eventStatusMessage = "Membuat edit job...", errorMessage = null) }
                when (
                    val editJobResult = useCases.createEditJob(
                        authToken = session.authToken,
                        deviceId = session.deviceId,
                        sessionId = session.sessionId,
                        templateId = templateId,
                        items = renderItems,
                    )
                ) {
                    is BoothResult.Success -> {
                        _state.update {
                            it.copy(
                                eventStatusMessage = "Mengirim rendered output final...",
                                errorMessage = null,
                            )
                        }
                        when (
                            val uploadResult = useCases.uploadRenderedOutput(
                                authToken = session.authToken,
                                deviceId = session.deviceId,
                                sessionId = session.sessionId,
                                editJobId = editJobResult.value,
                                photoFile = renderedFile,
                                width = current.selectedTemplateCanvasWidth.takeIf { it > 0 },
                                height = current.selectedTemplateCanvasHeight.takeIf { it > 0 },
                                dpi = 300,
                                force = true,
                            )
                        ) {
                            is BoothResult.Success -> _state.update {
                                it.copy(
                                    step = BoothStep.Finish,
                                    eventStatusMessage = "Rendered output berhasil dikirim ke Photobooth Station.",
                                    errorMessage = null,
                                )
                            }
                            is BoothResult.Failure -> {
                                val message = when (uploadResult.error) {
                                    is BoothError.Unauthorized -> "Kirim rendered output gagal (401): ${uploadResult.error.message}"
                                    is BoothError.Forbidden -> "Kirim rendered output gagal (403): ${uploadResult.error.message}"
                                    is BoothError.Validation -> "Kirim rendered output gagal (422): ${uploadResult.error.message}"
                                    is BoothError.Network -> "Kirim rendered output gagal (network): ${uploadResult.error.message}"
                                    is BoothError.Unknown -> "Kirim rendered output gagal: ${uploadResult.error.message}"
                                }
                                _state.update {
                                    it.copy(
                                        step = BoothStep.TemplatePreview,
                                        errorMessage = message,
                                    )
                                }
                            }
                        }
                    }
                    is BoothResult.Failure -> {
                        val message = when (editJobResult.error) {
                            is BoothError.Unauthorized -> "Create edit job gagal (401): ${editJobResult.error.message}"
                            is BoothError.Forbidden -> "Create edit job gagal (403): ${editJobResult.error.message}"
                            is BoothError.Validation -> "Create edit job gagal (422): ${editJobResult.error.message}"
                            is BoothError.Network -> "Create edit job gagal (network): ${editJobResult.error.message}"
                            is BoothError.Unknown -> "Create edit job gagal: ${editJobResult.error.message}"
                        }
                        _state.update {
                            it.copy(
                                step = BoothStep.TemplatePreview,
                                errorMessage = message,
                            )
                        }
                    }
                }
            }
            is BoothResult.Failure -> {
                val message = when (completeResult.error) {
                    is BoothError.Unauthorized -> "Finalisasi session gagal (401): ${completeResult.error.message}"
                    is BoothError.Forbidden -> "Finalisasi session gagal (403): ${completeResult.error.message}"
                    is BoothError.Validation -> "Finalisasi session gagal (422): ${completeResult.error.message}"
                    is BoothError.Network -> "Finalisasi session gagal (network): ${completeResult.error.message}"
                    is BoothError.Unknown -> "Finalisasi session gagal: ${completeResult.error.message}"
                }
                _state.update {
                    it.copy(
                        step = BoothStep.TemplatePreview,
                        errorMessage = message,
                    )
                }
            }
        }
        if (renderedFile.exists()) {
            renderedFile.delete()
        }
    }

    fun newSession() = _state.update {
        sessionStateManager.clearSession()
        it.copy(
            step = BoothStep.Dashboard,
            selectedTemplateId = null,
            selectedTemplate = null,
            selectedTemplatePaperSize = null,
            selectedTemplatePreviewUrl = null,
            selectedTemplatePreviewLocalPath = null,
            selectedTemplateOverlayUrl = null,
            selectedTemplateOverlayLocalPath = null,
            selectedTemplateCanvasWidth = 0,
            selectedTemplateCanvasHeight = 0,
            selectedTemplateSlots = emptyList(),
            capturedPhotoName = null,
            capturedPhotoPath = null,
            capturedPhotosBySlot = emptyMap(),
            uploadedSessionPhotosBySlot = emptyMap(),
            nextCaptureIndex = 1,
            templateSlotCount = 1,
            localOnlySession = false,
            voucher = null,
            quote = null,
            session = null,
            paymentStatus = null,
            mockPrintStatus = MockPrintStatus.Idle,
            mockPrintMessage = null,
            selectedColorFilter = ColorFilterType.Normal,
            errorMessage = null,
        )
    }

    fun downloadResult() = launchRequest {
        val current = state.value
        val renderState = prepareStateForRender(current)
        val renderedFile = renderedOutputComposer.compose(renderState)
        if (renderedFile == null) {
            _state.update {
                it.copy(
                    errorMessage = "Download gagal: overlay template tidak bisa dimuat.",
                )
            }
            return@launchRequest
        }
        val savedPath = renderedOutputComposer.saveToGallery(renderedFile)
        if (renderedFile.exists()) {
            renderedFile.delete()
        }
        if (savedPath.isNullOrBlank()) {
            _state.update {
                it.copy(errorMessage = "Download gagal: file tidak bisa disimpan ke perangkat.")
            }
            return@launchRequest
        }
        _state.update {
            it.copy(
                eventStatusMessage = "File berhasil disimpan: $savedPath",
                errorMessage = null,
            )
        }
    }

    fun onStoragePermissionDenied() {
        _state.update {
            it.copy(
                errorMessage = "Izin storage belum diizinkan. Aktifkan permission storage untuk menyimpan hasil download.",
            )
        }
    }

    fun triggerMockPrint() {
        val current = state.value
        if (current.mockPrintStatus == MockPrintStatus.Queued) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    mockPrintStatus = MockPrintStatus.Queued,
                    mockPrintMessage = "Mock print job queued...",
                )
            }
            delay(900)
            val success = Random.nextInt(100) >= 15
            _state.update {
                if (success) {
                    it.copy(
                        mockPrintStatus = MockPrintStatus.Sent,
                        mockPrintMessage = "Mock print sent successfully.",
                    )
                } else {
                    it.copy(
                        mockPrintStatus = MockPrintStatus.Failed,
                        mockPrintMessage = "Mock print failed. Coba lagi.",
                    )
                }
            }
        }
    }

    private suspend fun prepareStateForRender(current: BoothUiState): BoothUiState {
        val hasOverlay = !current.selectedTemplateOverlayLocalPath.isNullOrBlank() || !current.selectedTemplateOverlayUrl.isNullOrBlank()
        if (!hasOverlay) return current
        if (!current.selectedTemplateOverlayLocalPath.isNullOrBlank()) {
            val localFile = File(current.selectedTemplateOverlayLocalPath)
            val decoded = runCatching {
                BitmapFactory.decodeFile(localFile.absolutePath)?.also { it.recycle() }
            }.getOrNull()
            if (decoded != null) return current
            runCatching { localFile.delete() }
        }
        val templateId = current.selectedTemplateId ?: return current
        val cachedPath = templateAssetStore.cacheOverlay(
            templateId = templateId,
            overlayUrl = current.selectedTemplateOverlayUrl,
            stationBaseUrl = current.stationIp.toStationBaseUrl(),
            authToken = current.authToken,
        ) ?: return current
        templateSqliteStore.updateOverlayLocalPath(templateId, cachedPath)
        _state.update {
            if (it.selectedTemplateId == templateId) {
                it.copy(selectedTemplateOverlayLocalPath = cachedPath)
            } else {
                it
            }
        }
        return state.value
    }

    fun updateDeviceId(value: String) = updateAndPersistConfig {
        it.copy(deviceId = value, authToken = "", isStationConnected = false, errorMessage = null)
    }

    fun updateToken(value: String) = updateAndPersistConfig {
        it.copy(token = value, authToken = "", isStationConnected = false, errorMessage = null)
    }

    fun updateStationIp(value: String) = updateAndPersistConfig {
        it.copy(stationIp = value, authToken = "", isStationConnected = false, errorMessage = null)
    }

    fun updateCustomerWhatsapp(value: String) = updateAndPersistConfig {
        it.copy(customerWhatsapp = value.filter(Char::isDigit), errorMessage = null)
    }

    fun updateLaunchEventName(value: String) = updateAndPersistConfig {
        it.copy(launchEventName = value.trim(), errorMessage = null)
    }

    fun setLaunchSelectedEventId(eventId: String) {
        val trimmedId = eventId.trim()
        // Langsung update state: set event ID baru + reset BG dulu (agar UI responsif)
        _state.update { it.copy(launchSelectedEventId = trimmedId, welcomeBgUri = "", welcomeBgIsVideo = false, errorMessage = null) }
        viewModelScope.launch {
            // 1. Simpan event ID ke DataStore (tanpa overwrite BG event lain)
            configStore.saveSelectedEventId(trimmedId)
            // 2. Load BG yang tersimpan untuk event baru ini
            val (bgUri, bgIsVideo) = configStore.loadWelcomeBgForEvent(trimmedId)
            // 3. Update state dengan BG yang benar untuk event ini
            _state.update { current ->
                // Pastikan event ID masih sama (user belum ganti lagi)
                if (current.launchSelectedEventId == trimmedId) {
                    current.copy(welcomeBgUri = bgUri, welcomeBgIsVideo = bgIsVideo)
                } else current
            }
        }
    }

    fun toggleLaunchAllowedTemplate(templateId: String) = updateAndPersistConfig {
        val next = it.launchAllowedTemplateIds.toMutableSet()
        if (next.contains(templateId)) {
            next.remove(templateId)
        } else {
            next.add(templateId)
        }
        it.copy(launchAllowedTemplateIds = next, errorMessage = null)
    }

    fun clearLaunchAllowedTemplates() = updateAndPersistConfig {
        it.copy(launchAllowedTemplateIds = emptySet(), errorMessage = null)
    }

    fun selectAllLaunchAllowedTemplates() = updateAndPersistConfig {
        val allIds = it.availableTemplateItems.map(TemplateListItem::templateId).toSet()
        it.copy(launchAllowedTemplateIds = allIds, errorMessage = null)
    }

    fun updateLaunchTemplateSearchQuery(value: String) = _state.update {
        it.copy(launchTemplateSearchQuery = value, errorMessage = null)
    }

    fun updateLaunchAdditionalPrintCount(value: String) = updateAndPersistConfig {
        val count = value.filter(Char::isDigit).toIntOrNull() ?: 0
        it.copy(launchAdditionalPrintCount = count.coerceAtLeast(0), errorMessage = null)
    }

    fun setColorFilter(filter: ColorFilterType) {
        _state.update { it.copy(selectedColorFilter = filter) }
    }

    fun updateVoucherCode(value: String) = updateAndPersistConfig { it.copy(voucherCode = value, errorMessage = null) }

    fun updateVoucherType(value: String) = updateAndPersistConfig { it.copy(voucherType = value, errorMessage = null) }

    fun updateSessionType(value: String) = updateAndPersistConfig { it.copy(sessionType = value, errorMessage = null) }

    fun updateCustomerId(value: String) = updateAndPersistConfig { it.copy(customerId = value, errorMessage = null) }

    fun updatePaymentMethod(value: String) = updateAndPersistConfig {
        val normalized = value.trim().lowercase()
        val allowed = setOf("manual", "qris")
        it.copy(paymentMethod = if (normalized in allowed) normalized else "manual", errorMessage = null)
    }

    fun updateKioskExitCode(value: String) = updateAndPersistConfig {
        it.copy(kioskExitCode = value.trim(), errorMessage = null)
    }

    fun startLaunchEventGate() = _state.update {
        if (it.isStationConnected) {
            it.copy(
                step = BoothStep.VoucherCheck,
                localOnlySession = false,
                voucher = null,
                quote = null,
                session = null,
                paymentStatus = null,
                eventStatusMessage = "Masukkan voucher atau lanjutkan tanpa voucher.",
                errorMessage = null,
            )
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun setCameraSource(value: CameraSource) = updateAndPersistConfig {
        if (value != CameraSource.ExternalCanon) {
            externalPreviewJob?.cancel()
            externalPreviewJob = null
            runCatching { canonPtpSession?.stopLiveView() }
            canonPtpSession = null
            runCatching { canonUsbController.close() }
            it.copy(
                cameraSource = value,
                externalCameraStatus = ExternalCameraStatus.Disconnected,
                externalCameraType = "-",
                externalPreviewPath = null,
                externalPreviewBytes = null,
            )
        } else {
            it.copy(cameraSource = value)
        }
    }

    fun scanExternalCamera() = updateAndPersistConfig {
        val scanningState = it.copy(
            cameraSource = CameraSource.ExternalCanon,
            externalCameraStatus = ExternalCameraStatus.Scanning,
            eventStatusMessage = "Scanning kamera Canon...",
            errorMessage = null,
        )
        val device = canonUsbController.findCanonDevice()
        val cameraType = canonUsbController.describeCamera(device)
        scanningState.copy(
            cameraSource = CameraSource.ExternalCanon,
            externalCameraStatus = if (device != null) ExternalCameraStatus.Pairing else ExternalCameraStatus.Disconnected,
            externalCameraType = cameraType,
            eventStatusMessage = if (device != null) {
                "$cameraType terdeteksi. Lanjut Pairing untuk minta izin USB."
            } else {
                "Canon belum terdeteksi. Cek kabel USB/OTG dan nyalakan kamera."
            },
        )
    }

    fun pairExternalCamera() = updateAndPersistConfig {
        val device = canonUsbController.findCanonDevice()
        val cameraType = canonUsbController.describeCamera(device)
        when {
            device == null -> it.copy(
                externalCameraStatus = ExternalCameraStatus.Disconnected,
                externalCameraType = "-",
                eventStatusMessage = "Canon tidak ditemukan saat pairing.",
            )
            canonUsbController.hasPermission(device) -> it.copy(
                externalCameraStatus = ExternalCameraStatus.Pairing,
                externalCameraType = cameraType,
                eventStatusMessage = "$cameraType: USB permission sudah ada. Lanjut Connect.",
            )
            else -> {
                canonUsbController.requestPermission(device)
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Pairing,
                    externalCameraType = cameraType,
                    eventStatusMessage = "$cameraType: meminta USB permission. Izinkan di popup lalu klik Connect.",
                )
            }
        }
    }

    fun markExternalCameraConnected() = viewModelScope.launch {
        if (externalConnectInProgress) return@launch
        externalConnectInProgress = true
        try {
        _state.update {
            it.copy(
                externalCameraStatus = ExternalCameraStatus.Pairing,
                eventStatusMessage = "Menghubungkan ke Canon...",
                errorMessage = null,
            )
        }
        val device = canonUsbController.findCanonDevice()
        if (device == null) {
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    externalCameraType = "-",
                    eventStatusMessage = "Canon tidak ditemukan.",
                )
            }
            return@launch
        }
        if (!canonUsbController.hasPermission(device)) {
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Pairing,
                    eventStatusMessage = "USB permission belum ada. Klik Pairing dulu.",
                )
            }
            return@launch
        }
        if (!canonUsbController.connect(device)) {
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    externalCameraType = "-",
                    eventStatusMessage = "Gagal membuka koneksi USB Canon.",
                )
            }
            return@launch
        }
        val connection = canonUsbController.getConnection()
        val bulkIn = canonUsbController.getBulkInEndpoint()
        val bulkOut = canonUsbController.getBulkOutEndpoint()
        if (connection == null || bulkIn == null || bulkOut == null) {
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    eventStatusMessage = "Endpoint USB Canon tidak valid.",
                )
            }
            return@launch
        }
        val session = PtpSession(connection, bulkIn, bulkOut)
        val connected = withContext(Dispatchers.IO) { session.openSession() }
        if (connected) {
            // Some Canon models fail on GetDeviceInfo despite valid open session.
            withContext(Dispatchers.IO) { session.getDeviceInfo() }
        }
        if (!connected) {
            canonUsbController.close()
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    eventStatusMessage = "Gagal membuka sesi PTP Canon. Cek mode kamera (M/Av), kabel data, dan ulangi Pairing.",
                )
            }
            return@launch
        }
        canonPtpSession = session
        updateAndPersistConfig {
            it.copy(
                cameraSource = CameraSource.ExternalCanon,
                externalCameraStatus = ExternalCameraStatus.Connected,
                externalCameraType = canonUsbController.describeCamera(device),
                eventStatusMessage = "Canon connected. Siap capture.",
                errorMessage = null,
            )
        }
        if (state.value.step == BoothStep.Camera) {
            startExternalPreviewLoop(session)
        }
        } finally {
            externalConnectInProgress = false
        }
    }

    fun setMirrorLiveView(value: Boolean) = updateAndPersistConfig { it.copy(mirrorLiveView = value) }

    fun setExternalPreviewFps(value: Int) = updateAndPersistConfig {
        it.copy(externalPreviewFps = value.coerceIn(10, 24))
    }

    fun setMirrorCapture(value: Boolean) = updateAndPersistConfig { it.copy(mirrorCapture = value) }

    fun setImageQuality(value: ImageQuality) = updateAndPersistConfig { it.copy(imageQuality = value) }

    fun updateDetectedCameras(hasBack: Boolean, hasFront: Boolean) = _state.update {
        val useBack = when {
            it.useBackCamera && hasBack -> true
            it.useFrontCamera && !hasFront && hasBack -> true
            else -> false
        }
        val useFront = when {
            it.useFrontCamera && hasFront -> true
            it.useBackCamera && !hasBack && hasFront -> true
            else -> false
        }
        it.copy(
            hasBackCamera = hasBack,
            hasFrontCamera = hasFront,
            useBackCamera = useBack,
            useFrontCamera = useFront,
        )
    }

    fun setUseBackCamera(value: Boolean) = updateAndPersistConfig {
        if (value && !it.hasBackCamera) {
            it
        } else {
            it.copy(
                useBackCamera = value,
                useFrontCamera = if (value) false else it.useFrontCamera && it.hasFrontCamera,
            )
        }
    }

    fun setUseFrontCamera(value: Boolean) = updateAndPersistConfig {
        if (value && !it.hasFrontCamera) {
            it
        } else {
            it.copy(
                useFrontCamera = value,
                useBackCamera = if (value) false else it.useBackCamera && it.hasBackCamera,
            )
        }
    }

    fun setDenoisePhoto(value: Boolean) = updateAndPersistConfig { it.copy(denoisePhoto = value) }

    fun setCountdownSeconds(value: Int) = updateAndPersistConfig { it.copy(countdownSeconds = value.coerceIn(0, 10)) }

    fun setCountdownAudio(value: Boolean) = updateAndPersistConfig { it.copy(countdownAudio = value) }

    fun setShutterSound(value: Boolean) = updateAndPersistConfig { it.copy(shutterSound = value) }

    fun setDefaultPrinting(value: Boolean) = updateAndPersistConfig { it.copy(defaultPrinting = value) }

    fun setPrintUsePhotoboothStation(value: Boolean) = updateAndPersistConfig {
        if (it.localOnlySession) {
            it.copy(printUsePhotoboothStation = false)
        } else {
            it.copy(printUsePhotoboothStation = value)
        }
    }

    fun setWelcomeBgUri(value: String) = updateAndPersistConfig {
        it.copy(welcomeBgUri = value)
    }

    fun setWelcomeBgIsVideo(value: Boolean) = updateAndPersistConfig {
        it.copy(welcomeBgIsVideo = value)
    }

    fun setPrinterMarginTop(value: Float) = updateAndPersistConfig { it.copy(printerMarginTop = value) }
    fun setPrinterMarginBottom(value: Float) = updateAndPersistConfig { it.copy(printerMarginBottom = value) }
    fun setPrinterMarginLeft(value: Float) = updateAndPersistConfig { it.copy(printerMarginLeft = value) }
    fun setPrinterMarginRight(value: Float) = updateAndPersistConfig { it.copy(printerMarginRight = value) }
    fun setPrinterScaleMode(value: String) = updateAndPersistConfig { it.copy(printerScaleMode = value) }

    fun loginDevice() = launchRequest {
        val current = state.value
        if (current.deviceId.isBlank()) {
            _state.update { it.copy(errorMessage = "Device ID wajib diisi") }
            return@launchRequest
        }
        when (
            val result = stationConnectionChecker.connect(
                stationIp = current.stationIp,
                deviceId = current.deviceId,
                token = current.token,
            )
        ) {
            is BoothResult.Success -> {
                val now = java.time.Instant.now().toString()
                HeartbeatWorker.cancel(appContext)
                OfflineQueueWorker.cancel(appContext)
                val connectedState = current.copy(
                    stationIp = result.value.baseUrl,
                    deviceId = current.deviceId.trim(),
                    token = current.token.trim(),
                    authToken = result.value.bearerToken,
                    isStationConnected = true,
                    errorMessage = null,
                )
                configStore.save(connectedState.toDeviceConfig())
                stationBootstrap.setStationBaseUrl(connectedState.stationIp.toStationBaseUrl())
                sessionStateManager.updateConnection(
                    stationIp = connectedState.stationIp,
                    deviceId = connectedState.deviceId,
                    apiKey = connectedState.token,
                    authToken = connectedState.authToken,
                    isStationReachable = connectedState.isStationReachable,
                )
                val previousHeartbeat = heartbeatStatusStore.snapshot()
                heartbeatStatusStore.save(
                    HeartbeatStatus(
                        localIp = connectedState.heartbeatLocalIp.takeIf { it.isNotBlank() && it != "-" }
                            ?: previousHeartbeat.localIp,
                        appVersion = BuildConfig.VERSION_NAME,
                        os = "Android ${Build.VERSION.RELEASE}",
                        capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                        lastHeartbeatAt = now,
                        lastSyncAt = now,
                        lastResult = "success",
                        lastSuccessAt = now,
                        consecutiveFailures = 0,
                    ),
                )
                if (connectedState.authToken.isNotBlank()) {
                    stationTokenStore.saveToken(connectedState.authToken)
                }
                HeartbeatWorker.ensurePeriodic(appContext)
                OfflineQueueWorker.enqueue(appContext)
                _state.update { connectedState }
                val immediateLocalIp = connectedState.heartbeatLocalIp.takeIf { it.isNotBlank() && it != "-" }
                    ?: current.heartbeatLocalIp.takeIf { it.isNotBlank() && it != "-" }
                if (!immediateLocalIp.isNullOrBlank() && isValidIpv4(immediateLocalIp)) {
                    sendHeartbeatNow(immediateLocalIp)
                }
            }
            is BoothResult.Failure -> showStationConnectionError(result.error)
        }
    }

    fun verifyVoucher() = launchRequest {
        val current = state.value
        when (val result = useCases.verifyVoucher(current.deviceId, current.voucherCode, current.voucherType)) {
            is BoothResult.Success -> {
                if (result.value.isValid) {
                    _state.update {
                        it.copy(
                            voucher = result.value,
                            step = BoothStep.PaymentGate,
                            eventStatusMessage = "Voucher valid. Siapkan payment quote.",
                            errorMessage = null,
                        )
                    }
                    requestQuote()
                } else {
                    _state.update { it.copy(voucher = result.value, errorMessage = result.value.message ?: "Voucher tidak valid") }
                }
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun requestQuote() = launchRequest {
        val current = state.value
        when (
            val result = useCases.requestPaymentQuote(
                current.deviceId,
                current.voucherCode,
                current.voucherType,
                current.sessionType,
                current.customerId.ifBlank { null },
            )
        ) {
            is BoothResult.Success -> _state.update {
                it.copy(
                    quote = result.value,
                    step = BoothStep.PaymentGate,
                    eventStatusMessage = if (result.value.paymentRequired) {
                        "Payment dibutuhkan. Pilih manual payment untuk menunggu approval station."
                    } else {
                        "Payment tidak dibutuhkan. Lanjutkan ke template."
                    },
                    errorMessage = null,
                )
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun createSession() = launchRequest {
        val current = state.value
        val quoteId = current.quote?.quoteId.orEmpty()
        when (
            val result = useCases.createSession(
                current.deviceId,
                current.launchSelectedEventId,
                current.voucherCode,
                current.voucherType,
                quoteId,
                current.sessionType,
                current.customerId.ifBlank { null },
            )
        ) {
            is BoothResult.Success -> _state.update {
                sessionStateManager.updateFromBoothSession(result.value, current.customerId)
                it.copy(
                    session = result.value,
                    step = BoothStep.Camera,
                    nextCaptureIndex = 1,
                    capturedPhotosBySlot = emptyMap(),
                    uploadedSessionPhotosBySlot = emptyMap(),
                    capturedPhotoName = null,
                    capturedPhotoPath = null,
                    errorMessage = null,
                )
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun checkPayment() = launchRequest {
        val sessionId = state.value.session?.sessionId.orEmpty()
        when (val result = useCases.checkPayment(sessionId)) {
            is BoothResult.Success -> _state.update {
                val paymentStatus = result.value.paymentStatus
                val reviewStatus = result.value.reviewStatus
                val approvalStatus = result.value.approvalStatus
                val approved = result.value.canUpload || result.value.unlockPhoto || paymentStatus == "paid"
                val rejected = paymentStatus.isRejectedPaymentStatus() ||
                    reviewStatus.isRejectedPaymentStatus() ||
                    approvalStatus.isRejectedPaymentStatus() ||
                    !result.value.rejectionReason.isNullOrBlank()
                if (approved) {
                    it.copy(
                        paymentStatus = result.value,
                        step = BoothStep.TemplatePicker,
                        eventStatusMessage = "Payment approved untuk session ${result.value.sessionCode ?: result.value.sessionId}. Silakan pilih template.",
                        errorMessage = null,
                    )
                } else if (rejected) {
                    val reason = result.value.rejectionReason?.takeIf { msg -> msg.isNotBlank() } ?: "alasan tidak tersedia"
                    it.copy(
                        paymentStatus = result.value,
                        step = BoothStep.PaymentGate,
                        eventStatusMessage = "Payment ditolak untuk session ${result.value.sessionCode ?: result.value.sessionId}: $reason. Silakan ulangi manual payment.",
                        errorMessage = null,
                    )
                } else {
                    it.copy(
                        paymentStatus = result.value,
                        step = BoothStep.WaitingApproval,
                        eventStatusMessage = "Menunggu approval untuk session ${result.value.sessionCode ?: result.value.sessionId}.",
                        errorMessage = null,
                    )
                }
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun continueWithoutVoucher() = _state.update {
        it.copy(
            voucherCode = "",
            voucherType = "regular",
            voucher = null,
            step = BoothStep.PaymentGate,
            eventStatusMessage = "Lanjut tanpa voucher. Buat payment quote atau session manual.",
            errorMessage = null,
        )
    }

    fun createManualPaymentSession() = launchRequest {
        val current = state.value
        val selectedPaymentMethod = current.paymentMethod.ifBlank { "manual" }
        when (
            val result = useCases.openManualSession(
                current.launchSelectedEventId,
                current.customerWhatsapp.ifBlank { null },
                current.voucherCode,
                selectedPaymentMethod,
                current.launchAdditionalPrintCount,
            )
        ) {
            is BoothResult.Success -> _state.update {
                sessionStateManager.updateFromBoothSession(result.value, current.customerId)
                it.copy(
                    session = result.value,
                    step = BoothStep.WaitingApproval,
                    nextCaptureIndex = 1,
                    capturedPhotosBySlot = emptyMap(),
                    uploadedSessionPhotosBySlot = emptyMap(),
                    capturedPhotoName = null,
                    capturedPhotoPath = null,
                    eventStatusMessage = "Session ${result.value.sessionCode ?: result.value.sessionId} dibuat dengan metode ${selectedPaymentMethod}. Tunggu approval dari Photobooth Station.",
                    errorMessage = null,
                )
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun continueAfterFreeQuote() = launchRequest {
        val current = state.value
        if (current.quote?.paymentRequired == true) {
            _state.update { it.copy(errorMessage = "Payment masih dibutuhkan") }
            return@launchRequest
        }
        val quoteId = current.quote?.quoteId.orEmpty()
        when (
            val result = useCases.createSession(
                current.deviceId,
                current.launchSelectedEventId,
                current.voucherCode,
                current.voucherType,
                quoteId,
                current.sessionType,
                current.customerId.ifBlank { null },
            )
        ) {
            is BoothResult.Success -> _state.update {
                sessionStateManager.updateFromBoothSession(result.value, current.customerId)
                it.copy(
                    session = result.value,
                    step = BoothStep.TemplatePicker,
                    nextCaptureIndex = 1,
                    capturedPhotosBySlot = emptyMap(),
                    uploadedSessionPhotosBySlot = emptyMap(),
                    capturedPhotoName = null,
                    capturedPhotoPath = null,
                    eventStatusMessage = "Event siap. Silakan pilih template.",
                    errorMessage = null,
                )
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun confirmPayment() = launchRequest {
        val current = state.value
        val sessionId = current.session?.sessionId.orEmpty()
        when (val result = useCases.confirmPayment(current.deviceId, sessionId)) {
            is BoothResult.Success -> _state.update {
                it.copy(paymentStatus = result.value, step = if (result.value.canUpload) BoothStep.Finish else BoothStep.Settings)
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun captureDone() = _state.update { it.copy(step = BoothStep.CapturePreview, errorMessage = null) }

    fun retry() {
        when (state.value.step) {
            BoothStep.Splash -> continueFromSplash()
            BoothStep.Dashboard -> loginDevice()
            BoothStep.TemplatePicker -> startNowPhoto()
            BoothStep.CustomTemplate -> openCustomTemplate()
            BoothStep.Camera -> capturePhoto()
            BoothStep.CapturePreview -> acceptCapturePreview()
            BoothStep.TemplatePreview -> finishSession()
            BoothStep.Finish -> newSession()
            BoothStep.Settings -> loginDevice()
            BoothStep.LaunchEvent -> openLaunchEvent()
            BoothStep.SettingEvent -> openSettingEvent()
            BoothStep.SettingAllowedTemplates -> openSettingAllowedTemplates()
            BoothStep.VoucherCheck -> verifyVoucher()
            BoothStep.PaymentGate -> requestQuote()
            BoothStep.WaitingApproval -> checkPayment()
        }
    }

    private fun launchRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            block()
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun showError(error: BoothError) {
        if (error is BoothError.Validation && isVoucherValidationContext() && isVoucherValidationError(error.message)) {
            _state.update {
                it.copy(
                    errorMessage = null,
                    eventStatusMessage = "Kode voucher tidak valid",
                )
            }
            return
        }
        val rawMessage = when (error) {
            is BoothError.Unauthorized -> "401: ${error.message}"
            is BoothError.Forbidden -> "403: ${error.message}"
            is BoothError.Validation -> "422: ${error.message}"
            is BoothError.Network -> "Network: ${error.message}"
            is BoothError.Unknown -> error.message
        }
        if (isInvalidCustomerWaError(rawMessage)) {
            _state.update {
                it.copy(
                    errorMessage = null,
                    eventStatusMessage = "No WA tidak valid",
                )
            }
            return
        }
        _state.update { it.copy(errorMessage = rawMessage) }
    }

    private fun showStationConnectionError(error: BoothError) {
        val friendlyMessage = when (error) {
            is BoothError.Network -> {
                val detail = error.message.ifBlank { "Station tidak bisa dijangkau" }
                "Station tidak ditemukan. Periksa IP station, jaringan Wi-Fi/LAN, dan pastikan station sedang aktif. Detail: $detail"
            }
            is BoothError.Unauthorized -> {
                "Autentikasi station gagal. Periksa token/API key yang dipakai."
            }
            is BoothError.Forbidden -> {
                "Station menolak akses. Periksa izin device pada station."
            }
            is BoothError.Validation -> {
                val lower = error.message.lowercase()
                when {
                    lower.contains("device") -> "Device ID tidak valid. Periksa kode device pada station."
                    lower.contains("token") || lower.contains("credential") -> "Credential station tidak valid. Periksa token dan device ID."
                    lower.contains("format station ip") -> "Format Station IP tidak valid. Contoh yang benar: 10.0.2.2:8000"
                    else -> "Konfigurasi station tidak valid. Periksa IP, Device ID, dan token."
                }
            }
            is BoothError.Unknown -> {
                "Gagal menghubungkan station. ${error.message}"
            }
        }
        _state.update {
            it.copy(
                errorMessage = friendlyMessage,
                eventStatusMessage = "Photobooth Station belum terhubung.",
                isStationConnected = false,
            )
        }
    }

    private fun isVoucherValidationContext(): Boolean {
        return state.value.step == BoothStep.VoucherCheck || state.value.step == BoothStep.PaymentGate
    }

    private fun isVoucherValidationError(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("voucher") ||
            lower.contains("kode voucher") ||
            lower.contains("unprocessable") ||
            lower.contains("422")
    }

    private fun isInvalidCustomerWaError(message: String): Boolean {
        val lower = message.lowercase()
        val hasTargetField = lower.contains("customer") || lower.contains("whatsapp") || lower.contains("wa")
        val hasInvalidHint = lower.contains("invalid")
            || lower.contains("tidak valid")
            || lower.contains("not valid")
            || lower.contains("tidak ditemukan")
            || lower.contains("not found")
            || lower.contains("unregistered")
            || lower.contains("tidak terdaftar")
        return hasTargetField && hasInvalidHint
    }

    private fun updateAndPersistConfig(transform: (BoothUiState) -> BoothUiState) {
        val updated = transform(state.value)
        _state.value = updated
        stationBootstrap.setStationBaseUrl(updated.stationIp.toStationBaseUrl())
        sessionStateManager.updateConnection(
            stationIp = updated.stationIp,
            deviceId = updated.deviceId,
            apiKey = updated.token,
            authToken = updated.authToken,
            isStationReachable = updated.isStationReachable,
        )
        viewModelScope.launch {
            configStore.save(updated.toDeviceConfig())
        }
    }

    override fun onCleared() {
        externalPreviewJob?.cancel()
        externalPreviewJob = null
        unregisterUsbReceiver()
        runCatching { canonUsbController.close() }
        canonPtpSession = null
        super.onCleared()
    }

    private fun registerUsbReceiver() {
        if (usbReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                appContext,
                usbReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
        usbReceiverRegistered = true
    }

    private fun unregisterUsbReceiver() {
        if (!usbReceiverRegistered) return
        runCatching { appContext.unregisterReceiver(usbReceiver) }
        usbReceiverRegistered = false
    }

    private fun handleUsbPermissionResult(intent: Intent) {
        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
        val device = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
        if (device?.vendorId != CANON_VENDOR_ID) return
        viewModelScope.launch {
            if (granted) {
                _state.update {
                    it.copy(
                        externalCameraStatus = ExternalCameraStatus.Pairing,
                        externalCameraType = canonUsbController.describeCamera(device),
                        eventStatusMessage = "USB permission disetujui. Menghubungkan ke Canon...",
                        errorMessage = null,
                    )
                }
                markExternalCameraConnected()
            } else {
                _state.update {
                    it.copy(
                        externalCameraStatus = ExternalCameraStatus.Pairing,
                        eventStatusMessage = "USB permission ditolak. Klik Pairing lalu izinkan akses USB.",
                    )
                }
            }
        }
    }

    private fun handleUsbAttached(intent: Intent) {
        val device = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
        if (device?.vendorId != CANON_VENDOR_ID) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - lastUsbAttachHandledAt < 1500L) return@launch
            lastUsbAttachHandledAt = now
            if (state.value.cameraSource != CameraSource.ExternalCanon) return@launch
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Scanning,
                    externalCameraType = canonUsbController.describeCamera(device),
                    eventStatusMessage = "Canon terpasang. Scanning perangkat...",
                    errorMessage = null,
                )
            }
            delay(250)
            _state.update {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Pairing,
                    externalCameraType = canonUsbController.describeCamera(device),
                    eventStatusMessage = "Canon terdeteksi. Menyiapkan Pairing USB...",
                    errorMessage = null,
                )
            }
            if (canonUsbController.hasPermission(device)) {
                markExternalCameraConnected()
            } else {
                canonUsbController.requestPermission(device)
                _state.update {
                    it.copy(
                        externalCameraStatus = ExternalCameraStatus.Pairing,
                        eventStatusMessage = "Meminta USB permission. Izinkan popup untuk lanjut koneksi otomatis.",
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun handleUsbDetached(intent: Intent) {
        val device = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
        if (device?.vendorId != CANON_VENDOR_ID) return
        viewModelScope.launch {
            externalPreviewJob?.cancel()
            externalPreviewJob = null
            runCatching { canonPtpSession?.stopLiveView() }
            canonPtpSession = null
            runCatching { canonUsbController.close() }
            updateAndPersistConfig {
                it.copy(
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    externalCameraType = "-",
                    externalPreviewPath = null,
                    externalPreviewBytes = null,
                    eventStatusMessage = "Canon terlepas. Sambungkan ulang lalu Pairing/Connect.",
                    errorMessage = null,
                )
            }
        }
    }

    private inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }
    }

    private fun startExternalPreviewLoop(session: PtpSession) {
        externalPreviewJob?.cancel()
        externalPreviewJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val snapshot = state.value
                if (snapshot.cameraSource != CameraSource.ExternalCanon ||
                    snapshot.externalCameraStatus != ExternalCameraStatus.Connected
                ) {
                    _state.update { it.copy(externalPreviewPath = null, externalPreviewBytes = null) }
                    break
                }
                val previewBytes = session.downloadLatestPreviewJpeg()
                if (previewBytes != null) {
                    // Ignore tiny thumb frames to keep preview stable on EVF stream.
                    if (previewBytes.size >= 50 * 1024) {
                        val now = System.currentTimeMillis()
                        val targetFps = snapshot.externalPreviewFps.coerceIn(10, 24)
                        // Keep UI update conservative to avoid GC-driven jank/freezes on long sessions.
                        val minInterval = if (targetFps >= 20) 80L else 100L
                        val uiFrameIntervalMs = (1000L / targetFps).coerceAtLeast(minInterval)
                        if (now - lastPreviewUiUpdateAt >= uiFrameIntervalMs) {
                            lastPreviewUiUpdateAt = now
                            _state.update { it.copy(externalPreviewBytes = previewBytes) }
                            logSlotDebug("canonPreview updated bytes=${previewBytes.size} source=memory")
                        }
                    }
                } else {
                    logSlotDebug("canonPreview unavailable on this poll")
                }
                val targetFps = snapshot.externalPreviewFps.coerceIn(10, 24)
                val minDelay = if (targetFps >= 20) 80L else 100L
                val pollDelay = (1000L / targetFps).coerceAtLeast(minDelay)
                delay(pollDelay)
            }
        }
    }

    private suspend fun captureExternalPhotoInternal() {
        val ptp = canonPtpSession
        if (ptp == null || state.value.externalCameraStatus != ExternalCameraStatus.Connected) {
            _state.update {
                it.copy(
                    errorMessage = "Canon belum connected. Lakukan Scan -> Pairing -> Connect.",
                )
            }
            return
        }
        val previewJob = externalPreviewJob
        previewJob?.cancelAndJoin()
        externalPreviewJob = null
        _state.update { it.copy(isLoading = true, errorMessage = null, eventStatusMessage = "Trigger shutter Canon...") }
        val filePath = withContext(Dispatchers.IO) {
            val beforeHandles = ptp.listObjectHandles().orEmpty().toSet()
            var fired = false
            for (retry in 0 until 3) {
                fired = ptp.triggerShutter()
                if (fired) break
                logSlotDebug("canonCapture trigger retry=${retry + 1}")
                delay(250)
            }
            if (!fired) return@withContext null
            // Wait until camera publishes a new object handle, then download that object.
            var jpeg: ByteArray? = null
            var fallbackCandidate: ByteArray? = null
            repeat(14) { attempt ->
                if (jpeg != null) return@repeat
                delay(if (attempt < 2) 1000 else 800)
                val nowHandles = ptp.listObjectHandles().orEmpty()
                val newHandles = nowHandles.filterNot { beforeHandles.contains(it) }
                logSlotDebug("canonCapture attempt=${attempt + 1} handles=${nowHandles.size} newHandles=${newHandles.size}")
                val requireJpeg = newHandles.isNotEmpty()
                val rankedHandles = rankHandlesByObjectInfo(
                    ptp = ptp,
                    handles = if (newHandles.isNotEmpty()) newHandles else nowHandles.asReversed().take(3),
                    requireJpeg = requireJpeg,
                )
                if (newHandles.isNotEmpty() && rankedHandles.isEmpty()) {
                    logSlotDebug("canonCapture new handles exist but no JPEG object yet, waiting next attempt")
                    return@repeat
                }
                val targetHandles = if (newHandles.isNotEmpty()) {
                    rankedHandles
                } else {
                    rankedHandles
                }
                targetHandles.forEach { handle ->
                    if (jpeg != null) return@forEach
                    val candidate = if (newHandles.isNotEmpty()) {
                        // For newly created objects, prefer full object only (avoid white thumb placeholders).
                        ptp.downloadJpegFromHandle(handle)
                    } else {
                        ptp.downloadJpegFromHandleWithThumbFallback(handle)
                    }
                    if (candidate != null) {
                        val eval = evaluateJpegCandidate(candidate)
                        logSlotDebug(
                            "canonCapture handle=$handle bytes=${candidate.size} whiteRatio=${"%.3f".format(eval.whiteRatio)} blank=${eval.isBlank}",
                        )
                        if (!eval.isBlank) {
                            jpeg = candidate
                        } else if (fallbackCandidate == null || candidate.size > fallbackCandidate!!.size) {
                            fallbackCandidate = candidate
                        }
                    }
                }
                if (jpeg == null && newHandles.isEmpty() && attempt >= 6) {
                    val fallback = ptp.downloadLatestObjectJpeg()
                    if (fallback != null) {
                        val eval = evaluateJpegCandidate(fallback)
                        logSlotDebug(
                            "canonCapture fallbackLatest bytes=${fallback.size} whiteRatio=${"%.3f".format(eval.whiteRatio)} blank=${eval.isBlank}",
                        )
                        if (!eval.isBlank) {
                            jpeg = fallback
                        } else if (fallbackCandidate == null || fallback.size > fallbackCandidate!!.size) {
                            fallbackCandidate = fallback
                        }
                    }
                }
            }
            if (jpeg == null) {
                logSlotDebug("canonCapture using fallbackCandidate=${fallbackCandidate?.size ?: 0}")
                jpeg = fallbackCandidate
            }
            if (jpeg == null) return@withContext null
            externalCaptureStore.saveJpeg(jpeg)
        }
        _state.update { it.copy(isLoading = false) }
        if (filePath.isNullOrBlank()) {
            _state.update {
                it.copy(
                    errorMessage = "Capture Canon gagal. Kemungkinan AF/Busy/Storage kamera belum siap. Coba fokuskan objek, cek SD card, lalu ulangi.",
                )
            }
            if (state.value.externalCameraStatus == ExternalCameraStatus.Connected) {
                startExternalPreviewLoop(ptp)
            }
            return
        }
        if (state.value.externalCameraStatus == ExternalCameraStatus.Connected) {
            startExternalPreviewLoop(ptp)
        }
        capturePhoto(filePath)
    }

    private fun proceedAfterCaptureAccepted(
        current: BoothUiState,
        requiredCaptures: Int,
        successMessage: String,
        capturePath: String?,
    ) {
        val captureSlot = currentCaptureSlotIndex(current)
        val updatedPhotos = if (!capturePath.isNullOrBlank()) {
            current.capturedPhotosBySlot + (captureSlot to capturePath)
        } else {
            current.capturedPhotosBySlot
        }
        if (updatedPhotos.size >= requiredCaptures) {
            _state.update {
                it.copy(
                    capturedPhotoName = null,
                    capturedPhotoPath = null,
                    capturedPhotosBySlot = updatedPhotos,
                    step = if (it.step == BoothStep.LaunchEvent) BoothStep.LaunchEvent else BoothStep.TemplatePreview,
                    eventStatusMessage = successMessage,
                    errorMessage = null,
                )
            }
            return
        }
        val hasMoreCapture = current.nextCaptureIndex < requiredCaptures
        if (hasMoreCapture) {
            _state.update {
                it.copy(
                    nextCaptureIndex = current.nextCaptureIndex + 1,
                    capturedPhotoName = null,
                    capturedPhotoPath = null,
                    capturedPhotosBySlot = updatedPhotos,
                    step = if (it.step == BoothStep.LaunchEvent) BoothStep.LaunchEvent else BoothStep.Camera,
                    eventStatusMessage = "$successMessage Lanjut foto #${current.nextCaptureIndex + 1}/$requiredCaptures.",
                    errorMessage = null,
                )
            }
            return
        }
        _state.update {
            it.copy(
                capturedPhotoName = null,
                capturedPhotoPath = null,
                capturedPhotosBySlot = updatedPhotos,
                step = if (it.step == BoothStep.LaunchEvent) BoothStep.LaunchEvent else BoothStep.TemplatePreview,
                eventStatusMessage = successMessage,
                errorMessage = null,
            )
        }
    }

    private fun ensureTemplateAssetsCached(templateId: String) {
        val selected = cachedTemplates.firstOrNull { it.templateId == templateId } ?: return
        val current = state.value
        viewModelScope.launch {
            val thumbnailLocalPath = if (selected.thumbnailLocalPath.isNullOrBlank() && !selected.thumbnailUrl.isNullOrBlank()) {
                templateAssetStore.cacheThumbnail(
                    templateId = templateId,
                    thumbnailUrl = selected.thumbnailUrl,
                    stationBaseUrl = current.stationIp.toStationBaseUrl(),
                    authToken = current.authToken,
                )
            } else {
                selected.thumbnailLocalPath
            }
            val previewLocalPath = if (selected.previewLocalPath.isNullOrBlank() && !selected.previewUrl.isNullOrBlank()) {
                templateAssetStore.cachePreview(
                    templateId = templateId,
                    previewUrl = selected.previewUrl,
                    stationBaseUrl = current.stationIp.toStationBaseUrl(),
                    authToken = current.authToken,
                )
            } else {
                selected.previewLocalPath
            }
            val overlayLocalPath = if (selected.overlayLocalPath.isNullOrBlank() && !selected.overlayUrl.isNullOrBlank()) {
                templateAssetStore.cacheOverlay(
                    templateId = templateId,
                    overlayUrl = selected.overlayUrl,
                    stationBaseUrl = current.stationIp.toStationBaseUrl(),
                    authToken = current.authToken,
                )
            } else {
                selected.overlayLocalPath
            }
            if (thumbnailLocalPath.isNullOrBlank() && previewLocalPath.isNullOrBlank() && overlayLocalPath.isNullOrBlank()) return@launch
            val updatedTemplates = cachedTemplates.map { item ->
                if (item.templateId == templateId) {
                    item.copy(
                        thumbnailLocalPath = thumbnailLocalPath ?: item.thumbnailLocalPath,
                        previewLocalPath = previewLocalPath ?: item.previewLocalPath,
                        overlayLocalPath = overlayLocalPath ?: item.overlayLocalPath,
                    )
                } else {
                    item
                }
            }
            cachedTemplates = updatedTemplates
            templateSqliteStore.replaceTemplates(updatedTemplates)
            _state.update {
                if (it.selectedTemplateId == templateId) {
                    it.copy(
                        selectedTemplatePreviewLocalPath = previewLocalPath ?: it.selectedTemplatePreviewLocalPath,
                        selectedTemplateOverlayLocalPath = overlayLocalPath ?: it.selectedTemplateOverlayLocalPath,
                    )
                } else {
                    it
                }
            }
        }
    }

    private fun parseSlotCount(slotsJson: String): Int {
        return runCatching {
            json.decodeFromString(
                ListSerializer(TemplateSlotDto.serializer()),
                slotsJson,
            ).size
        }
            .getOrElse { 1 }
            .coerceAtLeast(1)
    }

    private fun parseSlots(slotsJson: String): List<TemplateSlotLayout> {
        return runCatching {
            json.decodeFromString(
                ListSerializer(TemplateSlotDto.serializer()),
                slotsJson,
            ).sortedBy { it.slotIndex }
                .map {
                    val sourceSlot = it.sourceSlotIndex
                        ?: it.metadata?.get("source_slot_index")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                        ?: it.metadata?.get("sourceSlotIndex")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                        ?: it.slotIndex
                    TemplateSlotLayout(
                        slotIndex = it.slotIndex,
                        sourceSlotIndex = sourceSlot.coerceAtLeast(1),
                        x = it.x,
                        y = it.y,
                        width = it.width,
                        height = it.height,
                        rotation = it.rotation,
                        borderRadius = it.borderRadius,
                    )
                }
        }.onFailure { error ->
            logSlotError("parseSlots gagal: ${error.message}")
        }.getOrElse { emptyList() }
            .let { slots ->
                val error = validateTemplateSlots(slots)
                if (error != null) {
                    logSlotError("template slot invalid: $error")
                    emptyList()
                } else {
                    slots
                }
            }
    }

    private fun isTemplateAssetReady(stored: StoredTemplate): Boolean {
        val readiness = buildTemplateAssetReadiness(stored)
        return readiness.thumbnailReady && readiness.previewReady && readiness.overlayReady
    }

    private fun uniqueCaptureSlotCount(slots: List<TemplateSlotLayout>): Int {
        return slots.map { it.sourceSlotIndex }.distinct().size.coerceAtLeast(1)
    }

    private fun validateTemplateSlots(slots: List<TemplateSlotLayout>): String? {
        if (slots.isEmpty()) return "slot kosong"
        val duplicateVisualSlot = slots
            .groupBy { it.slotIndex }
            .entries
            .firstOrNull { it.value.size > 1 }
            ?.key
        if (duplicateVisualSlot != null) return "slot_index duplikat: $duplicateVisualSlot"
        val invalidSourceSlot = slots.firstOrNull { it.sourceSlotIndex < 1 }?.sourceSlotIndex
        if (invalidSourceSlot != null) return "source_slot_index tidak valid: $invalidSourceSlot"
        return null
    }

    private fun captureSlotsOf(state: BoothUiState): List<Int> {
        return state.selectedTemplateSlots
            .map { it.sourceSlotIndex }
            .distinct()
            .sorted()
            .ifEmpty { listOf(1) }
    }

    private fun currentCaptureSlotIndex(state: BoothUiState): Int {
        val slots = captureSlotsOf(state)
        val index = (state.nextCaptureIndex - 1).coerceIn(0, slots.lastIndex)
        return slots[index]
    }

    private fun logSlotDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("BoothSlotDebug", message)
        }
    }

    private fun logSlotError(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e("BoothSlotDebug", message)
        }
    }

    private fun isLikelyBlankJpeg(bytes: ByteArray): Boolean {
        return evaluateJpegCandidate(bytes).isBlank
    }

    private data class JpegCandidateEvaluation(
        val isBlank: Boolean,
        val whiteRatio: Double,
    )

    private fun evaluateJpegCandidate(bytes: ByteArray): JpegCandidateEvaluation {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return JpegCandidateEvaluation(isBlank = true, whiteRatio = 1.0)
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = 8
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: return JpegCandidateEvaluation(isBlank = true, whiteRatio = 1.0)
        return try {
            val width = bitmap.width.coerceAtLeast(1)
            val height = bitmap.height.coerceAtLeast(1)
            var nearWhite = 0
            val stepX = (width / 80).coerceAtLeast(1)
            val stepY = (height / 80).coerceAtLeast(1)
            var sampled = 0
            var y = 0
            while (y < height) {
                var x = 0
                while (x < width) {
                    val p = bitmap.getPixel(x, y)
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    if (r > 245 && g > 245 && b > 245) nearWhite++
                    sampled++
                    x += stepX
                }
                y += stepY
            }
            if (sampled <= 0) return JpegCandidateEvaluation(isBlank = true, whiteRatio = 1.0)
            val whiteRatio = nearWhite.toDouble() / sampled.toDouble()
            JpegCandidateEvaluation(isBlank = whiteRatio >= 0.97, whiteRatio = whiteRatio)
        } finally {
            bitmap.recycle()
        }
    }

    private fun rankHandlesByObjectInfo(
        ptp: PtpSession,
        handles: List<Int>,
        requireJpeg: Boolean = false,
    ): List<Int> {
        if (handles.isEmpty()) return emptyList()
        val infos = handles.mapNotNull { handle ->
            val info = ptp.getObjectInfo(handle) ?: return@mapNotNull null
            logSlotDebug(
                "canonCapture objectInfo handle=$handle format=0x${info.objectFormat.toString(16)} size=${info.compressedSize} isJpeg=${info.isJpeg}",
            )
            handle to info
        }
        if (infos.isEmpty()) return handles.asReversed()
        val filtered = if (requireJpeg) infos.filter { (_, info) -> info.isJpeg } else infos
        if (filtered.isEmpty()) return emptyList()
        return filtered.sortedWith(
            compareByDescending<Pair<Int, PtpSession.ObjectInfo>> { (_, info) -> info.isJpeg }
                .thenByDescending { (_, info) -> info.compressedSize }
        ).map { it.first }
    }

    private fun buildTemplateAssetReadiness(stored: StoredTemplate): TemplateAssetReadiness {
        val thumbReady = if (stored.thumbnailUrl.isNullOrBlank()) true else templateAssetStore.isLocalImageValid(stored.thumbnailLocalPath)
        val previewReady = if (stored.previewUrl.isNullOrBlank()) {
            true
        } else {
            templateAssetStore.isLocalImageValid(stored.previewLocalPath)
                || templateAssetStore.isLocalImageValid(stored.thumbnailLocalPath)
        }
        val overlayReady = if (stored.overlayUrl.isNullOrBlank()) true else templateAssetStore.isLocalImageValid(stored.overlayLocalPath)
        return TemplateAssetReadiness(
            thumbnailReady = thumbReady,
            previewReady = previewReady,
            overlayReady = overlayReady,
        )
    }

    private fun resolveLocalTemplateThumbnailPath(stored: StoredTemplate): String? {
        return when {
            templateAssetStore.isLocalImageValid(stored.thumbnailLocalPath) -> stored.thumbnailLocalPath
            templateAssetStore.isLocalImageValid(stored.previewLocalPath) -> stored.previewLocalPath
            else -> null
        }
    }

    private fun resolveAssetUrl(rawUrl: String, stationBase: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return rawUrl
        val base = runCatching { URI(stationBase.toStationBaseUrl()) }.getOrNull()
        val uri = runCatching { URI(trimmed) }.getOrNull()
        if (uri?.isAbsolute == true) {
            val host = uri.host?.lowercase()
            val isLocalHost = host == "localhost" || host == "127.0.0.1" || host == "0.0.0.0"
            if (isLocalHost && base != null && !base.host.isNullOrBlank()) {
                return runCatching {
                    URI(
                        uri.scheme ?: base.scheme,
                        uri.userInfo,
                        base.host,
                        if (uri.port != -1) uri.port else base.port,
                        uri.path,
                        uri.query,
                        uri.fragment,
                    ).toString()
                }.getOrElse { trimmed }
            }
            return trimmed
        }
        if (base == null) return trimmed
        return runCatching { base.resolve(trimmed).toString() }.getOrElse { trimmed }
    }
}

internal fun buildDisconnectedState(current: BoothUiState): BoothUiState {
    return current.copy(
        authToken = "",
        isStationConnected = false,
        session = null,
        voucher = null,
        quote = null,
        paymentStatus = null,
        uploadedSessionPhotosBySlot = emptyMap(),
        eventStatusMessage = "Disconnected dari Photobooth Station.",
        errorMessage = null,
        step = BoothStep.Settings,
    )
}

private data class TemplateAssetReadiness(
    val thumbnailReady: Boolean,
    val previewReady: Boolean,
    val overlayReady: Boolean,
)

private fun BoothUiState.toDeviceConfig() = DeviceConfig(
    deviceId = deviceId,
    token = token,
    authToken = authToken,
    stationIp = stationIp,
    customerId = customerId,
    customerWhatsapp = customerWhatsapp,
    launchEventName = launchEventName,
    launchSelectedEventId = launchSelectedEventId,
    launchAllowedTemplateIds = launchAllowedTemplateIds,
    launchAdditionalPrintCount = launchAdditionalPrintCount,
    voucherCode = voucherCode,
    voucherType = voucherType,
    sessionType = sessionType,
    paymentMethod = paymentMethod,
    kioskExitCode = kioskExitCode,
    cameraSource = cameraSource.name,
    externalCameraStatus = externalCameraStatus.name,
    externalPreviewFps = externalPreviewFps,
    mirrorLiveView = mirrorLiveView,
    mirrorCapture = mirrorCapture,
    imageQuality = imageQuality.name,
    useBackCamera = useBackCamera,
    useFrontCamera = useFrontCamera,
    denoisePhoto = denoisePhoto,
    countdownSeconds = countdownSeconds,
    countdownAudio = countdownAudio,
    shutterSound = shutterSound,
    defaultPrinting = defaultPrinting,
    printUsePhotoboothStation = printUsePhotoboothStation,
    welcomeBgUri = welcomeBgUri,
    welcomeBgIsVideo = welcomeBgIsVideo,
    printerMarginTop = printerMarginTop,
    printerMarginBottom = printerMarginBottom,
    printerMarginLeft = printerMarginLeft,
    printerMarginRight = printerMarginRight,
    printerScaleMode = printerScaleMode,
)

private fun LaunchSession.toBoothSession() = BoothSession(
    sessionId = sessionId,
    sessionCode = sessionCode,
    uploadUrl = uploadUrl,
    paymentStatus = paymentStatus,
    paymentRequired = paymentRequired,
    unlockPhoto = unlockPhoto,
)

private fun com.errymaricha.dafydiobooth.data.session.SessionState.toBoothSession(): BoothSession? {
    val id = sessionId ?: return null
    return BoothSession(
        sessionId = id,
        sessionCode = sessionCode,
        uploadUrl = uploadUrl,
        paymentStatus = paymentStatus ?: "pending",
        paymentRequired = paymentRequired ?: true,
        unlockPhoto = unlockPhoto ?: false,
    )
}

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T {
    return try {
        java.lang.Enum.valueOf(T::class.java, this)
    } catch (e: Exception) {
        default
    }
}
