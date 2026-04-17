package com.errymaricha.dafydiobooth.domain.repository

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession

interface LaunchRepository {
    suspend fun login(deviceCode: String, apiKey: String): String

    suspend fun syncPricing(token: String): LaunchPricing

    suspend fun openSessionManual(
        token: String,
        customerWhatsapp: String,
        additionalPrintCount: Int,
    ): LaunchSession
}
