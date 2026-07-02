package com.errymaricha.dafydiobooth.domain.repository

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import kotlinx.coroutines.flow.Flow

interface LaunchRepository {
    suspend fun login(deviceCode: String, apiKey: String): String

    suspend fun syncPricing(token: String): LaunchPricing

    suspend fun listEvents(token: String): List<LaunchEvent>

    suspend fun createEvent(
        token: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean,
        cloudUploadMode: String,
        cloudSyncTiming: String,
        cloudTemplateMarketplaceEnabled: Boolean,
    ): LaunchEvent

    suspend fun updateEvent(
        token: String,
        eventId: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean? = null,
        cloudUploadMode: String? = null,
        cloudSyncTiming: String? = null,
        cloudTemplateMarketplaceEnabled: Boolean? = null,
    ): LaunchEvent

    suspend fun openSessionManual(
        token: String,
        eventId: String,
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

    fun checkPaymentSse(
        token: String,
        sessionId: String,
    ): Flow<LaunchPaymentStatus>
}
