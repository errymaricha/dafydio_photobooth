package com.errymaricha.dafydiobooth.station.repository

import com.errymaricha.dafydiobooth.station.local.OfflineQueueDao
import com.errymaricha.dafydiobooth.station.local.OfflineQueueEntity
import com.errymaricha.dafydiobooth.station.local.TemplateDao
import com.errymaricha.dafydiobooth.station.local.TemplateEntity
import com.errymaricha.dafydiobooth.station.model.CreateSessionRequest
import com.errymaricha.dafydiobooth.station.model.CreateSessionResponse
import com.errymaricha.dafydiobooth.station.model.DeviceAuthRequest
import com.errymaricha.dafydiobooth.station.model.DeviceAuthResponse
import com.errymaricha.dafydiobooth.station.model.HeartbeatRequest
import com.errymaricha.dafydiobooth.station.model.HeartbeatResponse
import com.errymaricha.dafydiobooth.station.model.PaymentCheckResponse
import com.errymaricha.dafydiobooth.station.model.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.station.model.PaymentQuoteResponse
import com.errymaricha.dafydiobooth.station.model.VerifyVoucherRequest
import com.errymaricha.dafydiobooth.station.model.VerifyVoucherResponse
import com.errymaricha.dafydiobooth.station.network.AppResult
import com.errymaricha.dafydiobooth.station.network.DeviceApiService
import com.errymaricha.dafydiobooth.station.network.safeApiCall
import com.errymaricha.dafydiobooth.station.security.TokenStore
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface AuthRepository {
    suspend fun login(deviceCode: String, apiKey: String): AppResult<DeviceAuthResponse>
    fun token(): String?
    fun clearToken()
}

class AuthRepositoryImpl(
    private val api: DeviceApiService,
    private val tokenStore: TokenStore,
) : AuthRepository {
    override suspend fun login(deviceCode: String, apiKey: String): AppResult<DeviceAuthResponse> {
        val result = safeApiCall { api.auth(DeviceAuthRequest(deviceCode = deviceCode, apiKey = apiKey)) }
        if (result is AppResult.Success) {
            tokenStore.saveToken(result.value.token)
        }
        return result
    }

    override fun token(): String? = tokenStore.getToken()

    override fun clearToken() = tokenStore.clear()
}

interface DeviceRepository {
    suspend fun sendHeartbeat(request: HeartbeatRequest): AppResult<HeartbeatResponse>
    suspend fun flushOfflineQueue(): AppResult<Unit>
}

class DeviceRepositoryImpl(
    private val api: DeviceApiService,
    private val authRepository: AuthRepository,
    private val offlineQueueDao: OfflineQueueDao,
) : DeviceRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sendHeartbeat(request: HeartbeatRequest): AppResult<HeartbeatResponse> {
        val token = authRepository.token() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        val result = safeApiCall { api.heartbeat("Bearer $token", request) }
        if (result is AppResult.Failure) {
            offlineQueueDao.enqueue(
                OfflineQueueEntity(
                    endpoint = "api/device/heartbeat",
                    method = "POST",
                    payload = json.encodeToString(OfflineQueuePayload.Heartbeat(request)),
                ),
            )
        }
        return result
    }

    override suspend fun flushOfflineQueue(): AppResult<Unit> {
        val token = authRepository.token() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        val pending = offlineQueueDao.pending()
        for (item in pending) {
            val payload = runCatching { json.decodeFromString<OfflineQueuePayload>(item.payload) }.getOrNull() ?: continue
            val result = when (payload) {
                is OfflineQueuePayload.Heartbeat -> safeApiCall { api.heartbeat("Bearer $token", payload.request) }
                is OfflineQueuePayload.SessionComplete -> safeApiCall {
                    api.completeSession("Bearer $token", payload.sessionId)
                }
                is OfflineQueuePayload.SessionPhotoUpload -> {
                    val photoFile = File(payload.photoPath)
                    if (!photoFile.exists()) {
                        offlineQueueDao.delete(item.id)
                        continue
                    }
                    val photoPart = MultipartBody.Part.createFormData(
                        "photo",
                        photoFile.name,
                        photoFile.asRequestBody("image/jpeg".toMediaType()),
                    )
                    safeApiCall {
                        api.uploadPhoto(
                            bearerToken = "Bearer $token",
                            sessionId = payload.sessionId,
                            photo = photoPart,
                            captureIndex = payload.captureIndex.toString().toRequestBody("text/plain".toMediaType()),
                            slotIndex = payload.slotIndex?.toString()?.toRequestBody("text/plain".toMediaType()),
                        )
                    }
                }
            }
            if (result is AppResult.Success) {
                offlineQueueDao.delete(item.id)
            } else {
                return AppResult.Failure((result as AppResult.Failure).error)
            }
        }
        return AppResult.Success(Unit)
    }
}

@Serializable
sealed class OfflineQueuePayload {
    @Serializable
    data class Heartbeat(val request: HeartbeatRequest) : OfflineQueuePayload()
    @Serializable
    data class SessionComplete(val sessionId: String) : OfflineQueuePayload()
    @Serializable
    data class SessionPhotoUpload(
        val sessionId: String,
        val photoPath: String,
        val captureIndex: Int,
        val slotIndex: Int?,
    ) : OfflineQueuePayload()
}

interface TemplateRepository {
    fun observeTemplates(): Flow<List<TemplateEntity>>
    suspend fun refreshTemplates(): AppResult<Unit>
}

class TemplateRepositoryImpl(
    private val api: DeviceApiService,
    private val authRepository: AuthRepository,
    private val templateDao: TemplateDao,
) : TemplateRepository {
    override fun observeTemplates(): Flow<List<TemplateEntity>> = templateDao.observeTemplates()

    override suspend fun refreshTemplates(): AppResult<Unit> {
        val token = authRepository.token() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        return when (val result = safeApiCall { api.getTemplates("Bearer $token") }) {
            is AppResult.Success -> {
                templateDao.upsertAll(
                    result.value.map {
                        TemplateEntity(
                            id = it.id,
                            templateName = it.templateName,
                            templateCode = it.templateCode,
                            paperSize = it.paperSize,
                            previewUrl = it.previewUrl,
                        )
                    },
                )
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> result
        }
    }
}

interface SessionRepository {
    suspend fun verifyVoucher(request: VerifyVoucherRequest): AppResult<VerifyVoucherResponse>
    suspend fun paymentQuote(request: PaymentQuoteRequest): AppResult<PaymentQuoteResponse>
    suspend fun createSession(request: CreateSessionRequest): AppResult<CreateSessionResponse>
    suspend fun paymentCheck(sessionId: String): AppResult<PaymentCheckResponse>
    suspend fun uploadPhoto(sessionId: String, photoPath: String, captureIndex: Int, slotIndex: Int?): AppResult<Unit>
    suspend fun completeSession(sessionId: String): AppResult<Unit>
}

class SessionRepositoryImpl(
    private val api: DeviceApiService,
    private val authRepository: AuthRepository,
    private val offlineQueueDao: OfflineQueueDao,
) : SessionRepository {
    private fun bearer(): String? = authRepository.token()?.let { "Bearer $it" }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun verifyVoucher(request: VerifyVoucherRequest): AppResult<VerifyVoucherResponse> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        return safeApiCall { api.verifyVoucher(token, request) }
    }

    override suspend fun paymentQuote(request: PaymentQuoteRequest): AppResult<PaymentQuoteResponse> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        return safeApiCall { api.paymentQuote(token, request) }
    }

    override suspend fun createSession(request: CreateSessionRequest): AppResult<CreateSessionResponse> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        return safeApiCall { api.createSession(token, request) }
    }

    override suspend fun paymentCheck(sessionId: String): AppResult<PaymentCheckResponse> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        return safeApiCall { api.paymentCheck(token, sessionId) }
    }

    override suspend fun uploadPhoto(
        sessionId: String,
        photoPath: String,
        captureIndex: Int,
        slotIndex: Int?,
    ): AppResult<Unit> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        val photoFile = File(photoPath)
        if (!photoFile.exists()) {
            return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Validation("Photo file not found"))
        }
        val mediaType = "image/jpeg".toMediaType()
        val photoPart = MultipartBody.Part.createFormData(
            "photo",
            photoFile.name,
            photoFile.asRequestBody(mediaType),
        )
        val result = safeApiCall {
            api.uploadPhoto(
                bearerToken = token,
                sessionId = sessionId,
                photo = photoPart,
                captureIndex = captureIndex.toString().toRequestBody("text/plain".toMediaType()),
                slotIndex = slotIndex?.toString()?.toRequestBody("text/plain".toMediaType()),
            )
            Unit
        }
        if (result is AppResult.Failure) {
            offlineQueueDao.enqueue(
                OfflineQueueEntity(
                    endpoint = "api/device/sessions/$sessionId/photos",
                    method = "POST",
                    payload = json.encodeToString(
                        OfflineQueuePayload.SessionPhotoUpload(
                            sessionId = sessionId,
                            photoPath = photoPath,
                            captureIndex = captureIndex,
                            slotIndex = slotIndex,
                        ),
                    ),
                ),
            )
        }
        return result
    }

    override suspend fun completeSession(sessionId: String): AppResult<Unit> {
        val token = bearer() ?: return AppResult.Failure(com.errymaricha.dafydiobooth.station.network.AppError.Unauthorized)
        val result = safeApiCall {
            api.completeSession(token, sessionId)
            Unit
        }
        if (result is AppResult.Failure) {
            offlineQueueDao.enqueue(
                OfflineQueueEntity(
                    endpoint = "api/device/sessions/$sessionId/complete",
                    method = "POST",
                    payload = json.encodeToString(OfflineQueuePayload.SessionComplete(sessionId)),
                ),
            )
        }
        return result
    }
}
