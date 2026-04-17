package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.ApiErrorBody
import com.errymaricha.dafydiobooth.data.api.ConfirmPaymentRequest
import com.errymaricha.dafydiobooth.data.api.CreateSessionRequest
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherRequest
import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.PhotoboothRepository
import java.io.IOException
import java.util.UUID
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class ApiPhotoboothRepository(
    private val api: PhotoboothApi,
    private val contractVersion: String = "2026-04-15",
) : PhotoboothRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun verifyVoucher(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
    ): BoothResult<VoucherVerification> = safeApiCall {
        api.verifyVoucher(
            VerifyVoucherRequest(
                contractVersion = contractVersion,
                deviceId = deviceId,
                voucherCode = voucherCode,
                voucherType = voucherType,
            ),
        ).toDomain()
    }

    override suspend fun paymentQuote(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<PaymentQuote> = safeApiCall {
        api.paymentQuote(
            PaymentQuoteRequest(
                contractVersion = contractVersion,
                deviceId = deviceId,
                voucherCode = voucherCode,
                voucherType = voucherType,
                sessionType = sessionType,
                customerId = customerId?.ifBlank { null },
            ),
        ).toDomain()
    }

    override suspend fun createSession(
        deviceId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<BoothSession> = safeApiCall {
        api.createSession(
            CreateSessionRequest(
                contractVersion = contractVersion,
                deviceId = deviceId,
                voucherCode = voucherCode,
                voucherType = voucherType,
                quoteId = quoteId,
                sessionType = sessionType,
                customerId = customerId?.ifBlank { null },
            ),
        ).toDomain()
    }

    override suspend fun paymentCheck(sessionId: String): BoothResult<PaymentStatus> = safeApiCall {
        api.paymentCheck(sessionId).toDomain()
    }

    override suspend fun confirmPayment(
        deviceId: String,
        sessionId: String,
    ): BoothResult<PaymentStatus> = safeApiCall {
        api.confirmPayment(
            sessionId = sessionId,
            request = ConfirmPaymentRequest(
                contractVersion = contractVersion,
                deviceId = deviceId,
                paymentRef = "device-${sessionId}-${UUID.randomUUID()}",
                paymentMethod = "manual",
                amount = 0L,
                currency = "IDR",
            ),
        ).let {
            PaymentStatus(
                sessionId = it.sessionId,
                sessionCode = it.sessionCode,
                paymentStatus = it.paymentStatus,
                canUpload = it.canUpload ?: it.unlockPhoto ?: it.paymentRequired == false,
                paymentRequired = it.paymentRequired ?: it.paymentStatus != "paid",
                unlockPhoto = it.unlockPhoto ?: it.paymentRequired == false,
            )
        }
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): BoothResult<T> {
        return try {
            BoothResult.Success(block())
        } catch (error: HttpException) {
            BoothResult.Failure(error.toBoothError())
        } catch (error: IOException) {
            BoothResult.Failure(BoothError.Network(error.message ?: "Network request failed"))
        } catch (error: Exception) {
            BoothResult.Failure(BoothError.Unknown(error.message ?: "Unexpected error"))
        }
    }

    private fun HttpException.toBoothError(): BoothError {
        val apiMessage = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ApiErrorBody>(body).message }.getOrNull()
        }
        return when (code()) {
            401 -> BoothError.Unauthorized
            403 -> BoothError.Forbidden
            422 -> BoothError.Validation(apiMessage ?: "Data tidak valid")
            else -> BoothError.Unknown(apiMessage ?: "HTTP ${code()}")
        }
    }
}
