package com.errymaricha.dafydiobooth.ui.booth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.errymaricha.dafydiobooth.data.local.DeviceConfig
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoothViewModel(
    private val useCases: PhotoboothUseCases,
    private val configStore: DeviceConfigStore,
    private val stationConnectionChecker: StationConnectionChecker,
) : ViewModel() {
    private val _state = MutableStateFlow(BoothUiState())
    val state: StateFlow<BoothUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            configStore.config.collect { config ->
                _state.update {
                    it.copy(
                        deviceId = config.deviceId,
                        token = config.token,
                        authToken = config.authToken,
                        stationIp = config.stationIp,
                        isStationConnected = config.authToken.isNotBlank(),
                        cameraSource = config.cameraSource.toEnum(CameraSource.AndroidDefault),
                        externalCameraStatus = config.externalCameraStatus.toEnum(ExternalCameraStatus.Disconnected),
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
                    )
                }
            }
        }
    }

    val defaultTemplates = listOf("Classic 2 Slot", "Clean Portrait", "Event Strip", "Square Party")

    fun continueFromSplash() = _state.update { it.copy(step = BoothStep.Dashboard, errorMessage = null) }

    fun openDashboard() = _state.update { it.copy(step = BoothStep.Dashboard, errorMessage = null) }

    fun startNowPhoto() = _state.update { it.copy(step = BoothStep.TemplatePicker, errorMessage = null) }

    fun openCustomTemplate() = _state.update { it.copy(step = BoothStep.CustomTemplate, errorMessage = null) }

    fun openSettings() = _state.update { it.copy(step = BoothStep.Settings, errorMessage = null) }

    fun openLaunchEvent() = _state.update {
        if (it.isStationConnected) {
            it.copy(step = BoothStep.LaunchEvent, errorMessage = null)
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun openSettingEvent() = _state.update {
        if (it.isStationConnected) {
            it.copy(step = BoothStep.SettingEvent, errorMessage = null)
        } else {
            it.copy(errorMessage = "Connect Photobooth Station dulu")
        }
    }

    fun selectTemplate(template: String) = _state.update {
        it.copy(selectedTemplate = template, step = BoothStep.Camera, errorMessage = null)
    }

    fun saveCustomTemplate(name: String) = _state.update {
        it.copy(selectedTemplate = name.ifBlank { "Custom Template" }, step = BoothStep.Camera, errorMessage = null)
    }

    fun capturePhoto() = _state.update {
        it.copy(capturedPhotoName = "capture-${System.currentTimeMillis()}.jpg", step = BoothStep.CapturePreview)
    }

    fun retakePhoto() = _state.update { it.copy(step = BoothStep.Camera, capturedPhotoName = null) }

    fun acceptCapturePreview() = _state.update { it.copy(step = BoothStep.TemplatePreview) }

    fun finishSession() = _state.update { it.copy(step = BoothStep.Finish) }

    fun newSession() = _state.update {
        it.copy(
            step = BoothStep.Dashboard,
            selectedTemplate = null,
            capturedPhotoName = null,
            voucher = null,
            quote = null,
            session = null,
            paymentStatus = null,
            errorMessage = null,
        )
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

    fun updateVoucherCode(value: String) = _state.update { it.copy(voucherCode = value, errorMessage = null) }

    fun updateVoucherType(value: String) = _state.update { it.copy(voucherType = value, errorMessage = null) }

    fun updateSessionType(value: String) = _state.update { it.copy(sessionType = value, errorMessage = null) }

    fun setCameraSource(value: CameraSource) = updateAndPersistConfig { it.copy(cameraSource = value) }

    fun scanExternalCamera() = updateAndPersistConfig {
        it.copy(cameraSource = CameraSource.ExternalCanon, externalCameraStatus = ExternalCameraStatus.Scanning)
    }

    fun pairExternalCamera() = updateAndPersistConfig { it.copy(externalCameraStatus = ExternalCameraStatus.Pairing) }

    fun markExternalCameraConnected() = updateAndPersistConfig { it.copy(externalCameraStatus = ExternalCameraStatus.Connected) }

    fun setMirrorLiveView(value: Boolean) = updateAndPersistConfig { it.copy(mirrorLiveView = value) }

    fun setMirrorCapture(value: Boolean) = updateAndPersistConfig { it.copy(mirrorCapture = value) }

    fun setImageQuality(value: ImageQuality) = updateAndPersistConfig { it.copy(imageQuality = value) }

    fun setUseBackCamera(value: Boolean) = updateAndPersistConfig {
        it.copy(useBackCamera = value, useFrontCamera = !value)
    }

    fun setUseFrontCamera(value: Boolean) = updateAndPersistConfig {
        it.copy(useFrontCamera = value, useBackCamera = !value)
    }

    fun setDenoisePhoto(value: Boolean) = updateAndPersistConfig { it.copy(denoisePhoto = value) }

    fun setCountdownSeconds(value: Int) = updateAndPersistConfig { it.copy(countdownSeconds = value.coerceIn(0, 10)) }

    fun setCountdownAudio(value: Boolean) = updateAndPersistConfig { it.copy(countdownAudio = value) }

    fun setShutterSound(value: Boolean) = updateAndPersistConfig { it.copy(shutterSound = value) }

    fun setDefaultPrinting(value: Boolean) = updateAndPersistConfig { it.copy(defaultPrinting = value) }

    fun setPrintUsePhotoboothStation(value: Boolean) = updateAndPersistConfig {
        it.copy(printUsePhotoboothStation = value)
    }

    fun loginDevice() = launchRequest {
        val current = state.value
        if (current.stationIp.isBlank()) {
            _state.update { it.copy(errorMessage = "Station IP wajib diisi") }
            return@launchRequest
        }
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
                val connectedState = current.copy(
                    stationIp = result.value.baseUrl,
                    deviceId = current.deviceId.trim(),
                    token = current.token.trim(),
                    authToken = result.value.bearerToken,
                    isStationConnected = true,
                    errorMessage = null,
                )
                configStore.save(connectedState.toDeviceConfig())
                _state.update { connectedState }
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun verifyVoucher() = launchRequest {
        val current = state.value
        when (val result = useCases.verifyVoucher(current.deviceId, current.voucherCode, current.voucherType)) {
            is BoothResult.Success -> {
                if (result.value.isValid) {
                    _state.update { it.copy(voucher = result.value, step = BoothStep.TemplatePicker, errorMessage = null) }
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
            )
        ) {
            is BoothResult.Success -> _state.update {
                it.copy(quote = result.value, errorMessage = null)
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
                current.voucherCode,
                current.voucherType,
                quoteId,
                current.sessionType,
            )
        ) {
            is BoothResult.Success -> _state.update {
                it.copy(session = result.value, step = BoothStep.Camera, errorMessage = null)
            }
            is BoothResult.Failure -> showError(result.error)
        }
    }

    fun checkPayment() = launchRequest {
        val sessionId = state.value.session?.sessionId.orEmpty()
        when (val result = useCases.checkPayment(sessionId)) {
            is BoothResult.Success -> _state.update { it.copy(paymentStatus = result.value, errorMessage = null) }
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
        val message = when (error) {
            BoothError.Unauthorized -> "401: Device tidak terotorisasi. Login ulang."
            BoothError.Forbidden -> "403: Device tidak punya akses."
            is BoothError.Validation -> "422: ${error.message}"
            is BoothError.Network -> "Network: ${error.message}"
            is BoothError.Unknown -> error.message
        }
        _state.update { it.copy(errorMessage = message) }
    }

    private fun updateAndPersistConfig(transform: (BoothUiState) -> BoothUiState) {
        val updated = transform(state.value)
        _state.value = updated
        viewModelScope.launch {
            configStore.save(updated.toDeviceConfig())
        }
    }
}

private fun BoothUiState.toDeviceConfig() = DeviceConfig(
    deviceId = deviceId,
    token = token,
    authToken = authToken,
    stationIp = stationIp,
    cameraSource = cameraSource.name,
    externalCameraStatus = externalCameraStatus.name,
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
)

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T {
    return enumValues<T>().firstOrNull { it.name == this } ?: default
}
