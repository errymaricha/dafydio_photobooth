package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.DeviceAuthRequest
import com.errymaricha.dafydiobooth.data.api.OpenManualSessionRequest
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherRequest
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
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
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        val response = api.openSession(
            bearerToken = "Bearer $token",
            request = OpenManualSessionRequest(
                customerWhatsapp = customerWhatsapp.ifBlank { null },
                voucherCode = voucherCode.ifBlank { null },
                paymentMethod = "manual",
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

    override suspend fun verifyVoucher(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification {
        return api.verifyVoucher(
            request = VerifyVoucherRequest(
                contractVersion = CONTRACT_VERSION,
                deviceId = "",
                voucherCode = voucherCode,
                voucherType = "",
                subtotalAmount = subtotalAmount,
            ),
            bearerToken = "Bearer $token",
        ).toDomain()
    }

    override suspend fun requestPaymentQuote(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote {
        return api.paymentQuote(
            request = PaymentQuoteRequest(
                contractVersion = CONTRACT_VERSION,
                deviceId = "",
                voucherCode = voucherCode.ifBlank { "" },
                voucherType = "",
                sessionType = "photo",
                subtotalAmount = subtotalAmount,
            ),
            bearerToken = "Bearer $token",
        ).toDomain()
    }

    override suspend fun checkPayment(
        token: String,
        sessionId: String,
    ): LaunchPaymentStatus {
        val response = api.paymentCheck(
            sessionId = sessionId,
            bearerToken = "Bearer $token",
        )
        val reviewStatus = listOf(
            response.manualReviewStatus,
            response.approvalStatus,
            response.reviewStatus,
            response.manualPaymentStatus,
            response.paymentApprovalStatus,
            response.status,
        ).firstOrNull { it.isNullOrBlank().not() }
        return LaunchPaymentStatus(
            sessionId = response.sessionId,
            sessionCode = response.sessionCode,
            paymentStatus = response.paymentStatus,
            reviewStatus = reviewStatus,
            canUpload = response.canUpload == true || response.paymentUnlocked == true,
            paymentRequired = response.paymentRequired ?: response.paymentStatus != "paid",
            unlockPhoto = response.unlockPhoto == true || response.paymentUnlocked == true,
            rejectionReason = listOf(
                response.rejectionReason,
                response.rejectReason,
                response.skipReason,
                response.reviewNotes,
                response.notes,
                response.reason,
                response.message,
            ).firstOrNull { it.isNullOrBlank().not() },
            reviewer = listOf(
                response.reviewedByName,
                response.reviewerName,
                response.reviewer,
                response.reviewedBy,
            ).firstOrNull { it.isNullOrBlank().not() },
            reviewedAt = response.reviewedAt,
        )
    }

    private companion object {
        const val CONTRACT_VERSION = "2026-04-17"
    }
}
