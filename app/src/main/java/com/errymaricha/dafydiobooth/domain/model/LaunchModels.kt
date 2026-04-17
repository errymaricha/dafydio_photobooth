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
