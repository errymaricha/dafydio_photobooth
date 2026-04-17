package com.errymaricha.dafydiobooth.ui.launch

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing

data class LaunchUiState(
    val loading: Boolean = false,
    val token: String? = null,
    val customerWhatsapp: String = "",
    val additionalPrintCount: Int = 0,
    val pricing: LaunchPricing? = null,
    val finalAmount: Double = 0.0,
    val message: String? = null,
    val error: String? = null,
)
