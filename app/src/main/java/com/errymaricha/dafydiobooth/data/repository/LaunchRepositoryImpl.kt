package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.DeviceAuthRequest
import com.errymaricha.dafydiobooth.data.api.OpenManualSessionRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.repository.LaunchRepository

class LaunchRepositoryImpl(
    private val api: PhotoboothApi,
) : LaunchRepository {
    override suspend fun login(deviceCode: String, apiKey: String): String {
        return api.auth(
            DeviceAuthRequest(
                deviceCode = deviceCode,
                apiKey = apiKey,
            ),
        ).token
    }

    override suspend fun syncPricing(token: String): LaunchPricing {
        val response = api.getMasterData("Bearer $token")
        return LaunchPricing(
            photoboothPrice = response.pricing.photoboothPrice,
            additionalPrintPrice = response.pricing.additionalPrintPrice,
            currencyCode = response.pricing.currencyCode,
        )
    }

    override suspend fun openSessionManual(
        token: String,
        customerWhatsapp: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        val response = api.openSession(
            bearerToken = "Bearer $token",
            request = OpenManualSessionRequest(
                customerWhatsapp = customerWhatsapp.ifBlank { null },
                additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
            ),
        )
        return LaunchSession(
            sessionId = response.sessionId,
            sessionCode = response.sessionCode,
            paymentStatus = response.paymentStatus,
            paymentRequired = response.paymentRequired ?: response.paymentStatus != "paid",
            unlockPhoto = response.unlockPhoto ?: response.paymentStatus == "paid",
        )
    }
}
