package com.errymaricha.dafydiobooth.ui.booth

import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import java.time.Duration
import java.time.Instant

enum class BoothStep {
    Splash,
    Dashboard,
    TemplatePicker,
    CustomTemplate,
    Camera,
    CapturePreview,
    TemplatePreview,
    Finish,
    Settings,
    LaunchEvent,
    SettingEvent,
    SettingAllowedTemplates,
    VoucherCheck,
    PaymentGate,
    WaitingApproval,
}

data class BoothUiState(
    val step: BoothStep = BoothStep.Splash,
    val isLoading: Boolean = false,
    val deviceId: String = "",
    val token: String = "",
    val authToken: String = "",
    val stationIp: String = "",
    val isStationConnected: Boolean = false,
    val voucherCode: String = "",
    val voucherType: String = "regular",
    val sessionType: String = "photo",
    val customerId: String = "",
    val customerWhatsapp: String = "",
    val launchEventName: String = "",
    val launchSelectedEventId: String = "",
    val launchAllowedTemplateIds: Set<String> = emptySet(),
    val launchTemplateSearchQuery: String = "",
    val launchAdditionalPrintCount: Int = 0,
    val paymentMethod: String = "manual",
    val kioskExitCode: String = "",
    val eventStatusMessage: String? = null,
    val selectedTemplateId: String? = null,
    val selectedTemplate: String? = null,
    val selectedTemplatePaperSize: String? = null,
    val selectedTemplatePreviewUrl: String? = null,
    val selectedTemplatePreviewLocalPath: String? = null,
    val selectedTemplateOverlayUrl: String? = null,
    val selectedTemplateOverlayLocalPath: String? = null,
    val selectedTemplateCanvasWidth: Int = 0,
    val selectedTemplateCanvasHeight: Int = 0,
    val selectedTemplateSlots: List<TemplateSlotLayout> = emptyList(),
    val capturedPhotoName: String? = null,
    val capturedPhotoPath: String? = null,
    val capturedPhotosBySlot: Map<Int, String> = emptyMap(),
    val uploadedSessionPhotosBySlot: Map<Int, String> = emptyMap(),
    val availableTemplates: List<String> = emptyList(),
    val availableTemplateItems: List<TemplateListItem> = emptyList(),
    val templatesUpdated: Boolean = false,
    val templateSlotCount: Int = 1,
    val nextCaptureIndex: Int = 1,
    val cameraSource: CameraSource = CameraSource.AndroidDefault,
    val externalCameraStatus: ExternalCameraStatus = ExternalCameraStatus.Disconnected,
    val externalCameraType: String = "-",
    val externalPreviewPath: String? = null,
    val externalPreviewBytes: ByteArray? = null,
    val externalPreviewFps: Int = 15,
    val mirrorLiveView: Boolean = false,
    val mirrorCapture: Boolean = false,
    val imageQuality: ImageQuality = ImageQuality.High,
    val hasBackCamera: Boolean = true,
    val hasFrontCamera: Boolean = false,
    val useBackCamera: Boolean = true,
    val useFrontCamera: Boolean = false,
    val denoisePhoto: Boolean = false,
    val countdownSeconds: Int = 3,
    val countdownAudio: Boolean = true,
    val shutterSound: Boolean = true,
    val defaultPrinting: Boolean = true,
    val printUsePhotoboothStation: Boolean = false,
    val welcomeBgUri: String = "",
    val welcomeBgIsVideo: Boolean = false,
    val heartbeatLocalIp: String = "-",
    val heartbeatAppVersion: String = "-",
    val heartbeatOsVersion: String = "-",
    val heartbeatCapabilities: String = "-",
    val heartbeatLastAt: String = "-",
    val heartbeatLastSyncAt: String = "-",
    val heartbeatLastResult: String = "-",
    val heartbeatLastSuccessAt: String = "-",
    val consecutiveHeartbeatFailures: Int = 0,
    val voucher: VoucherVerification? = null,
    val quote: PaymentQuote? = null,
    val session: BoothSession? = null,
    val paymentStatus: PaymentStatus? = null,
    val localOnlySession: Boolean = false,
    val mockPrintStatus: MockPrintStatus = MockPrintStatus.Idle,
    val mockPrintMessage: String? = null,
    val errorMessage: String? = null,
) {
    val isStationReachable: Boolean
        get() {
            if (!isStationConnected) return false
            return when (heartbeatLastResult.lowercase()) {
                "failed" -> {
                    val referenceSuccess = runCatching { Instant.parse(heartbeatLastSuccessAt) }.getOrNull()
                    consecutiveHeartbeatFailures < 2 &&
                        referenceSuccess != null &&
                        Duration.between(referenceSuccess, Instant.now()).abs() <= Duration.ofMinutes(10)
                }
                "success" -> {
                    val lastHeartbeat = runCatching { Instant.parse(heartbeatLastAt) }.getOrNull()
                    if (lastHeartbeat == null) true
                    else Duration.between(lastHeartbeat, Instant.now()).abs() <= Duration.ofMinutes(20)
                }
                else -> true
            }
        }
}

enum class MockPrintStatus {
    Idle,
    Queued,
    Sent,
    Failed,
}

enum class CameraSource {
    AndroidDefault,
    ExternalCanon,
}

enum class ExternalCameraStatus {
    Disconnected,
    Scanning,
    Pairing,
    Connected,
}

enum class ImageQuality {
    Standard,
    High,
    Maximum,
}

data class TemplateSlotLayout(
    val slotIndex: Int,
    val sourceSlotIndex: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val rotation: Double,
    val borderRadius: Int,
)

data class TemplateListItem(
    val templateId: String,
    val templateName: String,
    val templateCode: String,
    val category: String?,
    val paperSize: String?,
    val thumbnailUrl: String?,
    val thumbnailReady: Boolean,
    val previewReady: Boolean,
    val overlayReady: Boolean,
    val slotCount: Int,
)
