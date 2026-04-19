package com.errymaricha.dafydiobooth.domain.model

data class LaunchPricing(
    val photoboothPrice: Double,
    val additionalPrintPrice: Double,
    val currencyCode: String,
)

data class LaunchContext(
    val customerWhatsapp: String,
    val pricing: LaunchPricing,
)

data class LaunchSession(
    val sessionId: String,
    val sessionCode: String?,
    val paymentStatus: String,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
)

data class LaunchPaymentStatus(
    val sessionId: String,
    val sessionCode: String?,
    val paymentStatus: String,
    val reviewStatus: String? = null,
    val canUpload: Boolean,
    val paymentRequired: Boolean,
    val unlockPhoto: Boolean,
    val rejectionReason: String? = null,
    val reviewer: String? = null,
    val reviewedAt: String? = null,
) {
    val displayStatus: String
        get() = reviewStatus?.takeIf { it.isNotBlank() } ?: paymentStatus

    val isApproved: Boolean
        get() = canUpload ||
            unlockPhoto ||
            paymentStatus.lowercase() in setOf("paid", "approved", "unlocked") ||
            reviewStatus?.lowercase() in setOf("paid", "approved", "unlocked")

    val isRejected: Boolean
        get() = paymentStatus.isRejectedPaymentStatus() ||
            reviewStatus.isRejectedPaymentStatus() ||
            reviewedAt.isNullOrBlank().not() && rejectionReason.isNullOrBlank().not()
}

fun String?.isRejectedPaymentStatus(): Boolean {
    val status = this?.lowercase().orEmpty()
    return listOf(
        "reject",
        "failed",
        "expire",
        "cancel",
        "decline",
        "denied",
        "void",
    ).any { keyword -> status.contains(keyword) }
}
