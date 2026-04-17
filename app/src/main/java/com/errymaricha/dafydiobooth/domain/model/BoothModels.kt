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
    val uploadUrl: String?,
    val paymentStatus: String,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
)

data class PaymentStatus(
    val sessionId: String,
    val sessionCode: String?,
    val paymentStatus: String,
    val canUpload: Boolean,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
)

sealed interface BoothError {
    data object Unauthorized : BoothError
    data object Forbidden : BoothError
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
