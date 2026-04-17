package com.errymaricha.dafydiobooth.domain.repository

import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification

interface PhotoboothRepository {
    suspend fun verifyVoucher(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
    ): BoothResult<VoucherVerification>

    suspend fun paymentQuote(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<PaymentQuote>

    suspend fun createSession(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<BoothSession>

    suspend fun paymentCheck(sessionId: String): BoothResult<PaymentStatus>

    suspend fun confirmPayment(
        deviceId: String,
        sessionId: String,
    ): BoothResult<PaymentStatus>
}
