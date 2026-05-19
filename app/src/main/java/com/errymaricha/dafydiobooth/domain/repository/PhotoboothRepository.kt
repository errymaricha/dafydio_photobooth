package com.errymaricha.dafydiobooth.domain.repository

import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.BoothTemplate
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.RenderItem
import com.errymaricha.dafydiobooth.domain.model.UploadCaptureResult
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import java.io.File

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
        eventId: String,
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

    suspend fun uploadCapture(
        authToken: String,
        deviceId: String,
        sessionId: String,
        captureIndex: Int,
        slotIndex: Int?,
        photoFile: File,
    ): BoothResult<UploadCaptureResult>

    suspend fun completeSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
    ): BoothResult<Unit>

    suspend fun renderSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<Unit>

    suspend fun createEditJob(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<String>

    suspend fun uploadRenderedOutput(
        authToken: String,
        deviceId: String,
        sessionId: String,
        editJobId: String,
        photoFile: File,
        width: Int? = null,
        height: Int? = null,
        dpi: Int? = null,
        force: Boolean = true,
    ): BoothResult<Unit>

    suspend fun fetchTemplates(authToken: String): BoothResult<List<BoothTemplate>>
}
