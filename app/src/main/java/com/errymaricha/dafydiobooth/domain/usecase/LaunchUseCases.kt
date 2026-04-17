package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.repository.LaunchRepository

class CalculateFinalAmountUseCase {
    operator fun invoke(
        photoboothPrice: Double,
        additionalPrintPrice: Double,
        additionalPrintCount: Int,
    ): Double {
        return calculateFinalAmount(
            photoboothPrice = photoboothPrice,
            additionalPrintPrice = additionalPrintPrice,
            additionalPrintCount = additionalPrintCount,
        )
    }
}

class PrepareLaunchUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        deviceCode: String,
        apiKey: String,
    ): Pair<String, LaunchPricing> {
        require(deviceCode.isNotBlank()) { "Device code wajib diisi" }
        require(apiKey.isNotBlank()) { "API key wajib diisi" }

        val token = repository.login(deviceCode.trim(), apiKey.trim())
        val pricing = repository.syncPricing(token)
        return token to pricing
    }
}

class OpenManualSessionUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        customerWhatsapp: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(customerWhatsapp.isNotBlank()) { "No WA wajib diisi" }

        return repository.openSessionManual(
            token = token,
            customerWhatsapp = customerWhatsapp.trim(),
            additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
        )
    }
}

data class LaunchUseCases(
    val prepareLaunch: PrepareLaunchUseCase,
    val openManualSession: OpenManualSessionUseCase,
    val calculateFinalAmount: CalculateFinalAmountUseCase,
)
