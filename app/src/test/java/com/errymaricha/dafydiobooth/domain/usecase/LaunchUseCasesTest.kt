package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.LaunchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchUseCasesTest {
    @Test
    fun `prepare launch logs in and syncs pricing`() = runTest {
        val repository = FakeLaunchRepository()
        val useCase = PrepareLaunchUseCase(repository)

        val (token, pricing) = useCase(" PB-DEVICE-01 ", " secret-device-key ")

        assertEquals("station-token", token)
        assertEquals(50000.0, pricing.photoboothPrice, 0.0)
        assertEquals(listOf("login", "syncPricing"), repository.calls)
    }

    @Test
    fun `open manual session coerces additional print count`() = runTest {
        val repository = FakeLaunchRepository()
        val useCase = OpenManualSessionUseCase(repository)

        val session = useCase("station-token", "628123456789", "DISC10", -2)

        assertEquals("session-1", session.sessionId)
        assertEquals(0, repository.lastAdditionalPrintCount)
    }

    @Test
    fun `check launch payment uses station token and session id`() = runTest {
        val repository = FakeLaunchRepository()
        val useCase = CheckLaunchPaymentUseCase(repository)

        val status = useCase("station-token", "session-1")

        assertEquals("paid", status.paymentStatus)
        assertEquals(listOf("checkPayment"), repository.calls)
    }
}

private class FakeLaunchRepository : LaunchRepository {
    val calls = mutableListOf<String>()
    var lastAdditionalPrintCount: Int? = null

    override suspend fun login(deviceCode: String, apiKey: String): String {
        calls += "login"
        assertEquals("PB-DEVICE-01", deviceCode)
        assertEquals("secret-device-key", apiKey)
        return "station-token"
    }

    override suspend fun syncPricing(token: String): LaunchPricing {
        calls += "syncPricing"
        assertEquals("station-token", token)
        return LaunchPricing(
            photoboothPrice = 50000.0,
            additionalPrintPrice = 10000.0,
            currencyCode = "IDR",
        )
    }

    override suspend fun openSessionManual(
        token: String,
        customerWhatsapp: String,
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        calls += "openSessionManual"
        lastAdditionalPrintCount = additionalPrintCount
        return LaunchSession(
            sessionId = "session-1",
            sessionCode = "SES-001",
            paymentStatus = "unpaid",
            paymentRequired = true,
            unlockPhoto = false,
        )
    }

    override suspend fun verifyVoucher(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification {
        calls += "verifyVoucher"
        return VoucherVerification(
            code = voucherCode,
            type = "regular",
            isValid = true,
            status = "valid",
            message = null,
            customerName = null,
            remainingUses = 1,
            paymentRequired = true,
            unlockPhoto = false,
        )
    }

    override suspend fun requestPaymentQuote(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote {
        calls += "requestPaymentQuote"
        return PaymentQuote(
            quoteId = "quote-1",
            amount = subtotalAmount,
            currency = "IDR",
            paymentRequired = true,
            paymentUrl = null,
            expiresAt = null,
            subtotalAmount = subtotalAmount,
            discountAmount = 0,
            unlockPhoto = false,
            discountReason = null,
        )
    }

    override suspend fun checkPayment(
        token: String,
        sessionId: String,
    ): LaunchPaymentStatus {
        calls += "checkPayment"
        assertEquals("station-token", token)
        assertEquals("session-1", sessionId)
        return LaunchPaymentStatus(
            sessionId = "session-1",
            sessionCode = "SES-001",
            paymentStatus = "paid",
            canUpload = true,
            paymentRequired = false,
            unlockPhoto = true,
        )
    }
}
