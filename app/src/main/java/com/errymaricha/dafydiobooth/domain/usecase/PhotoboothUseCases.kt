package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.PhotoboothRepository

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
    ): BoothResult<PaymentQuote> {
        if (sessionType.isBlank()) return BoothResult.Failure(BoothError.Validation("Session type wajib diisi"))
        return repository.paymentQuote(deviceId, voucherCode, voucherType, sessionType)
    }
}

class CreateSessionUseCase(private val repository: PhotoboothRepository) {
    suspend operator fun invoke(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
    ): BoothResult<BoothSession> {
        if (quoteId.isBlank()) return BoothResult.Failure(BoothError.Validation("Quote belum tersedia"))
        return repository.createSession(deviceId, voucherCode, voucherType, quoteId, sessionType)
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

data class PhotoboothUseCases(
    val verifyVoucher: VerifyVoucherUseCase,
    val requestPaymentQuote: RequestPaymentQuoteUseCase,
    val createSession: CreateSessionUseCase,
    val checkPayment: CheckPaymentUseCase,
    val confirmPayment: ConfirmPaymentUseCase,
)
