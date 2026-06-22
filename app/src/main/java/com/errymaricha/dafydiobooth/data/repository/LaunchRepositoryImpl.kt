package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.DeviceAuthRequest
import com.errymaricha.dafydiobooth.data.api.ApiErrorBody
import com.errymaricha.dafydiobooth.data.api.CreateDeviceEventRequest
import com.errymaricha.dafydiobooth.data.api.OpenManualSessionRequest
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.data.api.UpdateDeviceEventRequest
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherRequest
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.LaunchRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class LaunchRepositoryImpl(
    private val api: PhotoboothApi,
) : LaunchRepository {
    private val json = Json { ignoreUnknownKeys = true }

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

    override suspend fun listEvents(token: String): List<LaunchEvent> {
        val payload = api.listEvents(bearerToken = "Bearer $token")
        return decodeEventList(payload).map { it.toDomain() }
    }

    override suspend fun createEvent(
        token: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean,
        cloudUploadMode: String,
        cloudSyncTiming: String,
        cloudTemplateMarketplaceEnabled: Boolean,
    ): LaunchEvent {
        val payload = api.createEvent(
            bearerToken = "Bearer $token",
            request = CreateDeviceEventRequest(
                eventCode = eventCode,
                eventName = eventName,
                cloudEnabled = cloudEnabled,
                cloudUploadMode = cloudUploadMode,
                cloudSyncTiming = cloudSyncTiming,
                cloudTemplateMarketplaceEnabled = cloudTemplateMarketplaceEnabled,
            ),
        )
        return decodeSingleEvent(payload).toDomain()
    }

    override suspend fun updateEvent(
        token: String,
        eventId: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean,
        cloudUploadMode: String,
        cloudSyncTiming: String,
        cloudTemplateMarketplaceEnabled: Boolean,
    ): LaunchEvent {
        val payload = api.updateEvent(
            eventId = eventId,
            bearerToken = "Bearer $token",
            request = UpdateDeviceEventRequest(
                eventCode = eventCode,
                eventName = eventName,
                cloudEnabled = cloudEnabled,
                cloudUploadMode = cloudUploadMode,
                cloudSyncTiming = cloudSyncTiming,
                cloudTemplateMarketplaceEnabled = cloudTemplateMarketplaceEnabled,
            ),
        )
        return decodeSingleEvent(payload).toDomain()
    }

    override suspend fun openSessionManual(
        token: String,
        eventId: String,
        customerWhatsapp: String,
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        return try {
            val normalizedWhatsapp = normalizeWhatsapp(customerWhatsapp)
            val response = api.openSession(
                bearerToken = "Bearer $token",
                request = OpenManualSessionRequest(
                // Empty string signals "use station default customer WA" on station side.
                customerWhatsapp = normalizedWhatsapp ?: "",
                eventId = eventId.ifBlank { null },
                voucherCode = voucherCode.ifBlank { null },
                paymentMethod = "manual",
                additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
                ),
            )
            LaunchSession(
                sessionId = response.sessionId,
                sessionCode = response.sessionCode,
                customerId = response.customerId ?: buildCustomerId(normalizedWhatsapp.orEmpty()),
                uploadUrl = response.uploadUrl,
                paymentStatus = response.paymentStatus,
                paymentRequired = response.paymentRequired ?: response.paymentStatus != "paid",
                unlockPhoto = response.unlockPhoto ?: response.paymentStatus == "paid",
            )
        } catch (error: HttpException) {
            throw when (error.code()) {
                422 -> IllegalArgumentException("No WA atau voucher tidak valid")
                401 -> IllegalStateException("Device tidak terotorisasi")
                403 -> IllegalStateException("Akses ditolak")
                else -> IllegalStateException("Gagal request manual payment: ${mapHttpDetail(error)}")
            }
        }
    }

    override suspend fun verifyVoucher(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification {
        return mapVoucherErrors {
            api.verifyVoucher(
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
    }

    override suspend fun requestPaymentQuote(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote {
        return mapVoucherErrors {
            api.paymentQuote(
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
            customerId = response.customerId,
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

    override fun checkPaymentSse(
        token: String,
        sessionId: String,
    ): Flow<LaunchPaymentStatus> = flow {
        var shouldRetry = true
        var retryCount = 0
        while (shouldRetry) {
            try {
                val responseBody = api.paymentCheckSse(
                    sessionId = sessionId,
                    bearerToken = "Bearer $token"
                )
                
                val reader = responseBody.charStream().buffered()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line.orEmpty().trim()
                    if (currentLine.startsWith("data:")) {
                        val jsonString = currentLine.removePrefix("data:").trim()
                        if (jsonString.isNotEmpty()) {
                            val response = json.decodeFromString<com.errymaricha.dafydiobooth.data.api.PaymentCheckResponse>(jsonString)
                            
                            val reviewStatus = listOf(
                                response.manualReviewStatus,
                                response.approvalStatus,
                                response.reviewStatus,
                                response.manualPaymentStatus,
                                response.paymentApprovalStatus,
                                response.status,
                            ).firstOrNull { it.isNullOrBlank().not() }
                            
                            val status = LaunchPaymentStatus(
                                sessionId = response.sessionId,
                                sessionCode = response.sessionCode,
                                customerId = response.customerId,
                                paymentStatus = response.paymentStatus,
                                reviewStatus = reviewStatus,
                                canUpload = response.canUpload == true || response.paymentUnlocked == true,
                                paymentRequired = response.paymentRequired ?: (response.paymentStatus != "paid"),
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
                            emit(status)
                            
                            if (status.unlockPhoto || status.paymentStatus == "paid") {
                                shouldRetry = false
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                e.printStackTrace()
            }
            
            if (shouldRetry) {
                delay(2500)
                retryCount++
                if (retryCount > 100) {
                    shouldRetry = false
                }
            }
        }
    }

    private companion object {
        const val CONTRACT_VERSION = "2026-04-17"
    }

    private suspend fun <T> mapVoucherErrors(block: suspend () -> T): T {
        return try {
            block()
        } catch (error: HttpException) {
            throw when (error.code()) {
                422 -> IllegalArgumentException("Kode voucher tidak valid")
                401 -> IllegalStateException("Device tidak terotorisasi")
                403 -> IllegalStateException("Akses ditolak")
                else -> IllegalStateException(mapHttpDetail(error))
            }
        }
    }

    private fun mapHttpDetail(error: HttpException): String {
        val rawBody = error.response()?.errorBody()?.string().orEmpty()
        val decoded = runCatching { json.decodeFromString(ApiErrorBody.serializer(), rawBody) }.getOrNull()
        return decoded?.message?.takeIf { it.isNotBlank() }
            ?: rawBody.takeIf { it.isNotBlank() }
            ?: "HTTP ${error.code()}"
    }

    private fun buildCustomerId(customerWhatsapp: String): String {
        val digits = customerWhatsapp.filter(Char::isDigit)
        return when {
            digits.isNotBlank() -> "CUST-$digits"
            else -> "CUST-DEFAULT"
        }
    }

    private fun normalizeWhatsapp(raw: String): String? {
        val digits = raw.filter(Char::isDigit)
        if (digits.isBlank()) return null
        return when {
            digits.startsWith("62") -> digits
            digits.startsWith("0") && digits.length > 1 -> "62${digits.drop(1)}"
            digits.startsWith("8") -> "62$digits"
            else -> digits
        }
    }

    private fun com.errymaricha.dafydiobooth.data.api.DeviceEventDto.toDomain(): LaunchEvent {
        val resolvedEventId = id.ifBlank { eventIdAlias.orEmpty() }
        require(resolvedEventId.isNotBlank()) { "Event ID tidak ditemukan pada response station." }
        return LaunchEvent(
            eventId = resolvedEventId,
            eventCode = eventCode,
            eventName = eventName,
            cloudEnabled = cloudEnabled,
            cloudUploadMode = cloudUploadMode,
            cloudSyncTiming = cloudSyncTiming,
            cloudTemplateMarketplaceEnabled = cloudTemplateMarketplaceEnabled,
        )
    }

    private fun decodeEventList(payload: JsonElement): List<com.errymaricha.dafydiobooth.data.api.DeviceEventDto> {
        return when (payload) {
            is JsonArray -> payload.map { decodeSingleEvent(it) }
            is JsonObject -> {
                val candidates = listOf("data", "events", "items", "results", "result")
                val container = candidates.firstNotNullOfOrNull { key -> payload[key] }
                when (container) {
                    is JsonArray -> container.map { decodeSingleEvent(it) }
                    is JsonObject -> listOf(decodeSingleEvent(container))
                    null -> {
                        runCatching { listOf(decodeSingleEvent(payload)) }.getOrElse {
                            throw IllegalStateException("Format response event tidak dikenali (expected array/data/events/items/result).")
                        }
                    }
                    else -> throw IllegalStateException("Format data event tidak dikenali.")
                }
            }
            else -> throw IllegalStateException("Format response event tidak dikenali.")
        }
    }

    private fun decodeSingleEvent(payload: JsonElement): com.errymaricha.dafydiobooth.data.api.DeviceEventDto {
        return try {
            json.decodeFromJsonElement(payload)
        } catch (_: SerializationException) {
            val data = (payload as? JsonObject)?.get("data")
            if (data != null) {
                json.decodeFromJsonElement(data)
            } else {
                throw IllegalStateException("Format event tidak dikenali.")
            }
        }
    }
}
