package com.errymaricha.dafydiobooth.domain.model

data class VoucherVerification(
    val code: String,
    val type: String,
    val isValid: Boolean,
    val status: String,
    val message: String?,
    val customerName: String?,
    val remainingUses: Int?,
    val paymentRequired: Boolean?,
    val unlockPhoto: Boolean?,
)

data class PaymentQuote(
    val quoteId: String,
    val amount: Long,
    val currency: String,
    val paymentRequired: Boolean,
    val paymentUrl: String?,
    val expiresAt: String?,
    val subtotalAmount: Long?,
    val discountAmount: Long?,
    val unlockPhoto: Boolean,
    val discountReason: String?,
)

data class BoothSession(
    val sessionId: String,
    val sessionCode: String?,
    val customerId: String? = null,
    val uploadUrl: String?,
    val paymentStatus: String,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
)

data class BoothTemplate(
    val templateId: String,
    val templateCode: String,
    val templateName: String,
    val category: String?,
    val paperSize: String?,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val thumbnailUrl: String?,
    val previewUrl: String?,
    val overlayUrl: String?,
    val configJson: String?,
    val slotsJson: String,
)

data class RenderItem(
    val sessionPhotoId: String,
    val slotIndex: Int,
)

data class UploadCaptureResult(
    val sessionPhotoId: String?,
)

data class PaymentStatus(
    val sessionId: String,
    val sessionCode: String?,
    val customerId: String? = null,
    val paymentStatus: String,
    val canUpload: Boolean,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
)

sealed interface BoothError {
    data class Unauthorized(val message: String = "Device tidak terotorisasi. Login ulang.") : BoothError
    data class Forbidden(val message: String = "Device tidak punya akses.") : BoothError
    data class Validation(val message: String) : BoothError
    data class Network(val message: String) : BoothError
    data class Unknown(val message: String) : BoothError
}

sealed interface BoothResult<out T> {
    data class Success<T>(val value: T) : BoothResult<T>
    data class Failure(val error: BoothError) : BoothResult<Nothing>
}

inline fun <T, R> BoothResult<T>.map(transform: (T) -> R): BoothResult<R> = when (this) {
    is BoothResult.Success -> BoothResult.Success(transform(value))
    is BoothResult.Failure -> this
}
