package com.errymaricha.dafydiobooth.ui.booth

import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification

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
    val selectedTemplate: String? = null,
    val capturedPhotoName: String? = null,
    val cameraSource: CameraSource = CameraSource.AndroidDefault,
    val externalCameraStatus: ExternalCameraStatus = ExternalCameraStatus.Disconnected,
    val mirrorLiveView: Boolean = false,
    val mirrorCapture: Boolean = false,
    val imageQuality: ImageQuality = ImageQuality.High,
    val useBackCamera: Boolean = true,
    val useFrontCamera: Boolean = false,
    val denoisePhoto: Boolean = false,
    val countdownSeconds: Int = 3,
    val countdownAudio: Boolean = true,
    val shutterSound: Boolean = true,
    val defaultPrinting: Boolean = true,
    val printUsePhotoboothStation: Boolean = false,
    val voucher: VoucherVerification? = null,
    val quote: PaymentQuote? = null,
    val session: BoothSession? = null,
    val paymentStatus: PaymentStatus? = null,
    val errorMessage: String? = null,
)

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
