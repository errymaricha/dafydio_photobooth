package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.BoothTemplate
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.RenderItem
import com.errymaricha.dafydiobooth.domain.model.UploadCaptureResult
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import java.io.File
import com.errymaricha.dafydiobooth.domain.repository.PhotoboothRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoboothFlowIntegrationTest {
    @Test
    fun `valid voucher proceeds to payment quote and create session`() = runTest {
        val repository = FakePhotoboothRepository()
        val useCases = PhotoboothUseCases(
            verifyVoucher = VerifyVoucherUseCase(repository),
            requestPaymentQuote = RequestPaymentQuoteUseCase(repository),
            createSession = CreateSessionUseCase(repository),
            checkPayment = CheckPaymentUseCase(repository),
            openManualSession = OpenBoothManualSessionUseCase(repository),
            confirmPayment = ConfirmPaymentUseCase(repository),
            uploadCapture = UploadCaptureUseCase(repository),
            refreshTemplates = RefreshTemplatesUseCase(repository),
            completeSession = CompleteSessionUseCase(repository),
            renderSession = RenderSessionUseCase(repository),
            createEditJob = CreateEditJobUseCase(repository),
            uploadRenderedOutput = UploadRenderedOutputUseCase(repository),
        )

        val voucher = useCases.verifyVoucher("device-1", "VCH-001", "regular")
        assertTrue(voucher is BoothResult.Success)
        assertTrue((voucher as BoothResult.Success).value.isValid)

        val quote = useCases.requestPaymentQuote("device-1", "VCH-001", "regular", "photo", "628123456789")
        assertTrue(quote is BoothResult.Success)
        assertEquals("quote-1", (quote as BoothResult.Success).value.quoteId)

        val session = useCases.createSession(
            "device-1",
            "event-1",
            "VCH-001",
            "regular",
            quote.value.quoteId,
            "photo",
            "628123456789",
        )
        assertTrue(session is BoothResult.Success)
        assertEquals("session-1", (session as BoothResult.Success).value.sessionId)
        assertEquals("628123456789", repository.lastCustomerId)

        assertEquals(
            listOf("verifyVoucher", "paymentQuote", "createSession"),
            repository.calls,
        )
    }
}

private class FakePhotoboothRepository : PhotoboothRepository {
    val calls = mutableListOf<String>()
    var lastCustomerId: String? = null

    override suspend fun verifyVoucher(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
    ): BoothResult<VoucherVerification> {
        calls += "verifyVoucher"
        return BoothResult.Success(
            VoucherVerification(
                code = voucherCode,
                type = voucherType,
                isValid = true,
                status = "valid",
                message = null,
                customerName = "Customer",
                remainingUses = 1,
                paymentRequired = true,
                unlockPhoto = false,
            ),
        )
    }

    override suspend fun paymentQuote(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<PaymentQuote> {
        calls += "paymentQuote"
        lastCustomerId = customerId
        return BoothResult.Success(
            PaymentQuote(
                quoteId = "quote-1",
                amount = 35000,
                currency = "IDR",
                paymentRequired = true,
                paymentUrl = "https://pay.example/quote-1",
                expiresAt = null,
                subtotalAmount = 35000,
                discountAmount = 0,
                unlockPhoto = false,
                discountReason = null,
            ),
        )
    }

    override suspend fun createSession(
        deviceId: String,
        eventId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<BoothSession> {
        calls += "createSession"
        lastCustomerId = customerId
        return BoothResult.Success(
            BoothSession(
                sessionId = "session-1",
                sessionCode = "SES-001",
                uploadUrl = "https://upload.example/session-1",
                paymentStatus = "unpaid",
                paymentRequired = true,
                unlockPhoto = false,
            ),
        )
    }

    override suspend fun paymentCheck(sessionId: String): BoothResult<PaymentStatus> {
        calls += "paymentCheck"
        return BoothResult.Success(
            PaymentStatus(
                sessionId = sessionId,
                sessionCode = "SES-001",
                paymentStatus = "paid",
                canUpload = true,
                paymentRequired = false,
                unlockPhoto = true,
            ),
        )
    }

    override suspend fun openManualSession(
        eventId: String,
        customerWhatsapp: String?,
        voucherCode: String,
        paymentMethod: String,
        additionalPrintCount: Int,
    ): BoothResult<BoothSession> {
        calls += "openManualSession"
        return BoothResult.Success(
            BoothSession(
                sessionId = "session-manual-1",
                sessionCode = "SES-MAN-001",
                uploadUrl = "https://upload.example/session-manual-1",
                paymentStatus = "pending",
                paymentRequired = true,
                unlockPhoto = false,
            ),
        )
    }

    override suspend fun confirmPayment(
        deviceId: String,
        sessionId: String,
    ): BoothResult<PaymentStatus> {
        calls += "confirmPayment"
        return BoothResult.Success(
            PaymentStatus(
                sessionId = sessionId,
                sessionCode = "SES-001",
                paymentStatus = "paid",
                canUpload = true,
                paymentRequired = false,
                unlockPhoto = true,
            ),
        )
    }

    override suspend fun uploadCapture(
        authToken: String,
        deviceId: String,
        sessionId: String,
        captureIndex: Int,
        slotIndex: Int?,
        photoFile: File,
    ): BoothResult<UploadCaptureResult> {
        calls += "uploadCapture"
        return BoothResult.Success(UploadCaptureResult(sessionPhotoId = "sp-$slotIndex"))
    }

    override suspend fun fetchTemplates(authToken: String): BoothResult<List<BoothTemplate>> {
        calls += "fetchTemplates"
        return BoothResult.Success(
            listOf(
                BoothTemplate(
                    templateId = "template-1",
                    templateCode = "CLASSIC-2",
                    templateName = "Classic 2 Slot",
                    category = "classic",
                    paperSize = "4R",
                    canvasWidth = 1200,
                    canvasHeight = 1800,
                    thumbnailUrl = null,
                    previewUrl = null,
                    overlayUrl = null,
                    configJson = null,
                    slotsJson = "[]",
                ),
            ),
        )
    }

    override suspend fun completeSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
    ): BoothResult<Unit> {
        calls += "completeSession"
        return BoothResult.Success(Unit)
    }

    override suspend fun renderSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<Unit> {
        calls += "renderSession"
        return BoothResult.Success(Unit)
    }

    override suspend fun uploadRenderedOutput(
        authToken: String,
        deviceId: String,
        sessionId: String,
        editJobId: String,
        photoFile: File,
        width: Int?,
        height: Int?,
        dpi: Int?,
        force: Boolean,
    ): BoothResult<Unit> {
        calls += "uploadRenderedOutput"
        return BoothResult.Success(Unit)
    }

    override suspend fun createEditJob(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<String> {
        calls += "createEditJob"
        return BoothResult.Success("edit-job-1")
    }
}
