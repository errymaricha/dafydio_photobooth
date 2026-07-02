package com.errymaricha.dafydiobooth.data.repository

import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.errymaricha.dafydiobooth.data.api.ApiErrorBody
import com.errymaricha.dafydiobooth.data.api.ConfirmPaymentRequest
import com.errymaricha.dafydiobooth.data.api.CreateEditJobRequest
import com.errymaricha.dafydiobooth.data.api.CreateSessionRequest
import com.errymaricha.dafydiobooth.data.api.EditJobItemRequest
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.data.api.OpenManualSessionRequest
import com.errymaricha.dafydiobooth.data.api.RenderEditJobRequest
import com.errymaricha.dafydiobooth.data.api.TemplateDto
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherRequest
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
import java.io.IOException
import java.util.UUID
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        eventId: String,
        voucherCode: String,
        voucherType: String,
        quoteId: String,
        sessionType: String,
        customerId: String?,
    ): BoothResult<BoothSession> = safeApiCall {
        val response = api.createSession(
            CreateSessionRequest(
                contractVersion = contractVersion,
                deviceId = deviceId,
                eventId = eventId.ifBlank { null },
                voucherCode = voucherCode,
                voucherType = voucherType,
                quoteId = quoteId,
                sessionType = sessionType,
                customerId = customerId?.ifBlank { null },
            ),
        )
        response.toDomain().copy(
            customerId = response.customerId ?: customerId?.ifBlank { null },
        )
    }

    override suspend fun paymentCheck(sessionId: String): BoothResult<PaymentStatus> = safeApiCall {
        api.paymentCheck(sessionId).toDomain()
    }

    override suspend fun openManualSession(
        eventId: String,
        customerWhatsapp: String?,
        voucherCode: String,
        paymentMethod: String,
        additionalPrintCount: Int,
    ): BoothResult<BoothSession> = safeApiCall {
        api.openSession(
            bearerToken = "",
            request = OpenManualSessionRequest(
                eventId = eventId.ifBlank { null },
                customerWhatsapp = customerWhatsapp?.ifBlank { null } ?: "",
                voucherCode = voucherCode.ifBlank { null },
                paymentMethod = paymentMethod.ifBlank { "manual" },
                additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
            ),
        ).toDomain()
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
                customerId = it.customerId,
                paymentStatus = it.paymentStatus,
                reviewStatus = null,
                approvalStatus = null,
                canUpload = it.canUpload ?: it.unlockPhoto ?: it.paymentRequired == false,
                paymentRequired = it.paymentRequired ?: it.paymentStatus != "paid",
                unlockPhoto = it.unlockPhoto ?: it.paymentRequired == false,
                rejectionReason = null,
            )
        }
    }

    override suspend fun uploadCapture(
        authToken: String,
        deviceId: String,
        sessionId: String,
        captureIndex: Int,
        slotIndex: Int?,
        photoFile: File,
    ): BoothResult<UploadCaptureResult> = safeApiCall {
        val compressedFile = compressImageFile(photoFile)
        try {
            val response = api.uploadCapture(
                bearerToken = "Bearer ${authToken.trim()}",
                deviceId = deviceId.trim(),
                sessionId = sessionId,
                photo = MultipartBody.Part.createFormData(
                    name = "photo",
                    filename = compressedFile.name,
                    body = compressedFile.asRequestBody("image/jpeg".toMediaType()),
                ),
                captureIndex = captureIndex.toString().toRequestBody("text/plain".toMediaType()),
                slotIndex = slotIndex?.toString()?.toRequestBody("text/plain".toMediaType()),
            )
            val payload = response.string()
            val sessionPhotoId = parseSessionPhotoId(payload)
            if (sessionPhotoId.isNullOrBlank()) {
                throw IllegalStateException("Upload berhasil tapi session_photo_id tidak ditemukan. payload=${payload.take(500)}")
            }
            UploadCaptureResult(sessionPhotoId = sessionPhotoId)
        } finally {
            if (compressedFile != photoFile && compressedFile.exists()) {
                compressedFile.delete()
            }
        }
    }

    private fun compressImageFile(originalFile: File): File {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(originalFile.absolutePath, options)
            
            val maxDimension = 2048
            var inSampleSize = 1
            if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
                val halfWidth = options.outWidth / 2
                val halfHeight = options.outHeight / 2
                while (halfWidth / inSampleSize >= maxDimension && halfHeight / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, decodeOptions) ?: return originalFile
            
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val newWidth = if (bitmap.width > bitmap.height) maxDimension else (maxDimension * ratio).toInt()
                val newHeight = if (bitmap.height > bitmap.width) maxDimension else (maxDimension / ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }
            
            val tempFile = File.createTempFile("upload_${System.currentTimeMillis()}", ".jpg", originalFile.parentFile)
            tempFile.outputStream().use { fos ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }
            scaledBitmap.recycle()
            tempFile
        } catch (t: Throwable) {
            t.printStackTrace()
            originalFile
        }
    }


    override suspend fun fetchTemplates(authToken: String): BoothResult<List<BoothTemplate>> = safeApiCall {
        val payload = api.listTemplates(
            bearerToken = "Bearer ${authToken.trim()}",
            includeSlots = true,
        )
        parseTemplatePayload(payload).map { it.toDomain() }
    }

    override suspend fun completeSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
    ): BoothResult<Unit> = safeApiCall {
        api.completeSession(
            bearerToken = "Bearer ${authToken.trim()}",
            deviceId = deviceId.trim(),
            sessionId = sessionId,
        )
        Unit
    }

    override suspend fun renderSession(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<Unit> = safeApiCall {
        val editJobPayload = api.createEditJob(
            bearerToken = "Bearer ${authToken.trim()}",
            deviceId = deviceId.trim(),
            sessionId = sessionId,
            request = CreateEditJobRequest(
                templateId = templateId,
                items = items.map {
                    EditJobItemRequest(
                        sessionPhotoId = it.sessionPhotoId,
                        slotIndex = it.slotIndex,
                    )
                },
            ),
        )
        val editJobId = extractEditJobId(editJobPayload)
            ?: throw IllegalStateException("Create edit job berhasil tapi edit_job_id tidak ditemukan")
        api.renderEditJob(
            bearerToken = "Bearer ${authToken.trim()}",
            deviceId = deviceId.trim(),
            editJobId = editJobId,
            request = RenderEditJobRequest(force = true),
        )
        Unit
    }

    override suspend fun createEditJob(
        authToken: String,
        deviceId: String,
        sessionId: String,
        templateId: String,
        items: List<RenderItem>,
    ): BoothResult<String> = safeApiCall {
        val payload = api.createEditJob(
            bearerToken = "Bearer ${authToken.trim()}",
            deviceId = deviceId.trim(),
            sessionId = sessionId,
            request = CreateEditJobRequest(
                templateId = templateId,
                items = items.map {
                    EditJobItemRequest(
                        sessionPhotoId = it.sessionPhotoId,
                        slotIndex = it.slotIndex,
                    )
                },
            ),
        )
        extractEditJobId(payload)
            ?: throw IllegalStateException("Create edit job berhasil tapi edit_job_id tidak ditemukan")
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
    ): BoothResult<Unit> = safeApiCall {
        val mediaType = when (photoFile.extension.lowercase()) {
            "png" -> "image/png".toMediaType()
            else -> "image/jpeg".toMediaType()
        }
        val textMediaType = "text/plain".toMediaType()
        api.uploadRenderedOutput(
            bearerToken = "Bearer ${authToken.trim()}",
            deviceId = deviceId.trim(),
            sessionId = sessionId,
            editJobId = editJobId.toRequestBody(textMediaType),
            renderedImage = MultipartBody.Part.createFormData(
                name = "rendered_image",
                filename = photoFile.name,
                body = photoFile.asRequestBody(mediaType),
            ),
            width = width?.toString()?.toRequestBody(textMediaType),
            height = height?.toString()?.toRequestBody(textMediaType),
            dpi = dpi?.toString()?.toRequestBody(textMediaType),
            force = if (force) "1".toRequestBody(textMediaType) else "0".toRequestBody(textMediaType),
        )
        Unit
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): BoothResult<T> = withContext(Dispatchers.IO) {
        try {
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
        val rawBody = response()?.errorBody()?.string().orEmpty()
        val requestUrl = response()?.raw()?.request?.url?.toString().orEmpty()
        val decoded = runCatching { json.decodeFromString<ApiErrorBody>(rawBody) }.getOrNull()
        val validationDetail = decoded?.errors
            ?.entries
            ?.firstOrNull()
            ?.let { (field, messages) ->
                val message = messages.firstOrNull()?.trim().orEmpty()
                if (message.isBlank()) field else "$field: $message"
            }
        val apiMessage = decoded?.message
        val detail = validationDetail?.ifBlank { null }
            ?: apiMessage?.ifBlank { null }
            ?: rawBody.take(500).ifBlank { null }
            ?: "HTTP ${code()}"
        if (code() == 422) {
            Log.e(
                "ApiPhotoboothRepository",
                "HTTP 422 url=$requestUrl detail=$detail raw=${rawBody.take(1000)}",
            )
        }
        return when (code()) {
            401 -> BoothError.Unauthorized("Device tidak terotorisasi. Detail: $detail")
            403 -> BoothError.Forbidden("Akses ditolak. Detail: $detail")
            409 -> BoothError.Validation("Capture duplikat (409). Detail: $detail")
            422 -> BoothError.Validation("Data tidak valid. Detail: $detail")
            else -> BoothError.Unknown("HTTP ${code()}: $detail")
        }
    }

    private fun parseTemplatePayload(payload: JsonElement): List<TemplateDto> {
        return when (payload) {
            is JsonArray -> json.decodeFromJsonElement(ListSerializer(TemplateDto.serializer()), payload)
            is JsonObject -> {
                val source = when {
                    payload["data"] is JsonArray -> payload.getValue("data")
                    payload["templates"] is JsonArray -> payload.getValue("templates")
                    else -> JsonArray(emptyList())
                }
                json.decodeFromJsonElement(ListSerializer(TemplateDto.serializer()), source)
            }
            else -> emptyList()
        }
    }

    private fun parseSessionPhotoId(payload: String): String? {
        val cleaned = payload.trim().removePrefix("\uFEFF")
        if (cleaned.isBlank()) return null
        val element = runCatching { json.parseToJsonElement(cleaned) }.getOrNull() as? JsonObject ?: return null
        val data = element["data"] as? JsonObject
        val sessionPhoto = element["session_photo"] as? JsonObject
        val dataSessionPhoto = data?.get("session_photo") as? JsonObject
        return element["session_photo_id"]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: data?.get("session_photo_id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: element["id"]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: data?.get("id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: sessionPhoto?.get("id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: dataSessionPhoto?.get("id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
    }

    private fun extractEditJobId(payload: JsonElement): String? {
        val root = payload as? JsonObject ?: return null
        return root["edit_job_id"]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: root["id"]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: (root["data"] as? JsonObject)?.get("edit_job_id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: (root["data"] as? JsonObject)?.get("id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
    }
}
