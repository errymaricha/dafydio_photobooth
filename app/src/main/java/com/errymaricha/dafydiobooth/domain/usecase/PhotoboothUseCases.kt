package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.BoothTemplate
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.RenderItem
import com.errymaricha.dafydiobooth.domain.model.UploadCaptureResult
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.PhotoboothRepository
import java.io.File

class VerifyVoucherUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
    ): BoothResult<VoucherVerification> {
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (voucherCode.isBlank()) return BoothResult.Failure(BoothError.Validation("Voucher wajib diisi"))
        return repository.verifyVoucher(deviceId, voucherCode.trim(), voucherType)
    }
}

class RequestPaymentQuoteUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        sessionType: String,
        customerId: String? = null,
    ): BoothResult<PaymentQuote> {
        if (sessionType.isBlank()) return BoothResult.Failure(BoothError.Validation("Session type wajib diisi"))
        return repository.paymentQuote(deviceId, voucherCode, voucherType, sessionType, customerId)
    }
}

class CreateSessionUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        deviceId: String,
        eventId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
        customerId: String? = null,
    ): BoothResult<BoothSession> {
        if (eventId.isBlank()) return BoothResult.Failure(BoothError.Validation("Event belum dipilih"))
        if (quoteId.isBlank()) return BoothResult.Failure(BoothError.Validation("Quote belum tersedia"))
        return repository.createSession(deviceId, eventId, voucherCode, voucherType, quoteId, sessionType, customerId)
    }
}

class CheckPaymentUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(sessionId: String): BoothResult<PaymentStatus> {
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        return repository.paymentCheck(sessionId)
    }
}

class ConfirmPaymentUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(deviceId: String, sessionId: String): BoothResult<PaymentStatus> {
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        return repository.confirmPayment(deviceId, sessionId)
    }
}

class UploadCaptureUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        authToken: String,
        deviceId: String,
        sessionId: String,
        captureIndex: Int,
        slotIndex: Int?,
        photoFile: File,
    ): BoothResult<UploadCaptureResult> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device ID belum tersedia"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        if (captureIndex < 1) return BoothResult.Failure(BoothError.Validation("Capture index tidak valid"))
        if (slotIndex != null && slotIndex < 1) return BoothResult.Failure(BoothError.Validation("Slot index tidak valid"))
        if (!photoFile.exists()) return BoothResult.Failure(BoothError.Validation("File capture tidak ditemukan"))
        return repository.uploadCapture(authToken, deviceId, sessionId, captureIndex, slotIndex, photoFile)
    }
}

class RefreshTemplatesUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(authToken: String): BoothResult<List<BoothTemplate>> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        return repository.fetchTemplates(authToken)
    }
}

class CompleteSessionUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        authToken: String,
        deviceId: String,
        sessionId: String,
    ): BoothResult<Unit> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device ID belum tersedia"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        return repository.completeSession(authToken, deviceId, sessionId)
    }
}

class RenderSessionUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<Unit> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device ID belum tersedia"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        if (templateId.isBlank()) return BoothResult.Failure(BoothError.Validation("Template belum dipilih"))
        if (items.isEmpty()) return BoothResult.Failure(BoothError.Validation("No renderable items found for this edit job."))
        return repository.renderSession(authToken, deviceId, sessionId, templateId, items)
    }
}

class CreateEditJobUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<String> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device ID belum tersedia"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        if (templateId.isBlank()) return BoothResult.Failure(BoothError.Validation("Template belum dipilih"))
        if (items.isEmpty()) return BoothResult.Failure(BoothError.Validation("Belum ada foto upload untuk membuat edit job."))
        return repository.createEditJob(authToken, deviceId, sessionId, templateId, items)
    }
}

class UploadRenderedOutputUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        authToken: String,
        deviceId: String,
        sessionId: String,
        editJobId: String,
        photoFile: File,
        width: Int? = null,
        height: Int? = null,
        dpi: Int? = null,
        force: Boolean = true,
    ): BoothResult<Unit> {
        if (authToken.isBlank()) return BoothResult.Failure(BoothError.Validation("Device belum login"))
        if (deviceId.isBlank()) return BoothResult.Failure(BoothError.Validation("Device ID belum tersedia"))
        if (sessionId.isBlank()) return BoothResult.Failure(BoothError.Validation("Session belum dibuat"))
        if (editJobId.isBlank()) return BoothResult.Failure(BoothError.Validation("Edit job ID belum tersedia"))
        if (!photoFile.exists()) return BoothResult.Failure(BoothError.Validation("File render tidak ditemukan"))
        return repository.uploadRenderedOutput(
            authToken = authToken,
            deviceId = deviceId,
            sessionId = sessionId,
            editJobId = editJobId,
            photoFile = photoFile,
            width = width,
            height = height,
            dpi = dpi,
            force = force,
        )
    }
}

data class PhotoboothUseCases(
    val verifyVoucher: VerifyVoucherUseCase,
    val requestPaymentQuote: RequestPaymentQuoteUseCase,
    val createSession: CreateSessionUseCase,
    val checkPayment: CheckPaymentUseCase,
    val confirmPayment: ConfirmPaymentUseCase,
    val uploadCapture: UploadCaptureUseCase,
    val refreshTemplates: RefreshTemplatesUseCase,
    val completeSession: CompleteSessionUseCase,
    val renderSession: RenderSessionUseCase,
    val createEditJob: CreateEditJobUseCase,
    val uploadRenderedOutput: UploadRenderedOutputUseCase,
)
