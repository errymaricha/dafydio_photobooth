package com.errymaricha.dafydiobooth.domain.repository

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification

interface LaunchRepository {
    suspend fun login(deviceCode: String, apiKey: String): String

    suspend fun syncPricing(token: String): LaunchPricing

    suspend fun openSessionManual(
        token: String,
        customerWhatsapp: String,
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession

    suspend fun verifyVoucher(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification

    suspend fun requestPaymentQuote(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote

    suspend fun checkPayment(
        token: String,
        sessionId: String,
    ): LaunchPaymentStatus
}
