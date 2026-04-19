package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
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
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        require(token.isNotBlank()) { "Token station belum tersedia" }

        return repository.openSessionManual(
            token = token,
            customerWhatsapp = customerWhatsapp.trim(),
            voucherCode = voucherCode.trim(),
            additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
        )
    }
}

class VerifyLaunchVoucherUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(voucherCode.isNotBlank()) { "Kode voucher wajib diisi" }

        return repository.verifyVoucher(
            token = token,
            voucherCode = voucherCode.trim(),
            subtotalAmount = subtotalAmount.coerceAtLeast(0),
        )
    }
}

class RequestLaunchPaymentQuoteUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote {
        require(token.isNotBlank()) { "Token station belum tersedia" }

        return repository.requestPaymentQuote(
            token = token,
            voucherCode = voucherCode.trim(),
            subtotalAmount = subtotalAmount.coerceAtLeast(0),
        )
    }
}

class CheckLaunchPaymentUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        sessionId: String,
    ): LaunchPaymentStatus {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(sessionId.isNotBlank()) { "Session manual belum dibuat" }

        return repository.checkPayment(
            token = token,
            sessionId = sessionId,
        )
    }
}

data class LaunchUseCases(
    val prepareLaunch: PrepareLaunchUseCase,
    val openManualSession: OpenManualSessionUseCase,
    val verifyLaunchVoucher: VerifyLaunchVoucherUseCase,
    val requestLaunchPaymentQuote: RequestLaunchPaymentQuoteUseCase,
    val checkLaunchPayment: CheckLaunchPaymentUseCase,
    val calculateFinalAmount: CalculateFinalAmountUseCase,
)
