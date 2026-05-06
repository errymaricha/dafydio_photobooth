package com.errymaricha.dafydiobooth.station.repository

import com.errymaricha.dafydiobooth.station.model.CreateSessionRequest
import com.errymaricha.dafydiobooth.station.model.CreateSessionResponse
import com.errymaricha.dafydiobooth.station.model.DeviceAuthRequest
import com.errymaricha.dafydiobooth.station.model.DeviceAuthResponse
import com.errymaricha.dafydiobooth.station.model.HeartbeatRequest
import com.errymaricha.dafydiobooth.station.model.HeartbeatResponse
import com.errymaricha.dafydiobooth.station.model.PaymentCheckResponse
import com.errymaricha.dafydiobooth.station.model.PaymentQuoteRequest
import com.errymaricha.dafydiobooth.station.model.PaymentQuoteResponse
import com.errymaricha.dafydiobooth.station.model.SessionCompleteResponse
import com.errymaricha.dafydiobooth.station.model.TemplateDto
import com.errymaricha.dafydiobooth.station.model.VerifyVoucherRequest
import com.errymaricha.dafydiobooth.station.model.VerifyVoucherResponse
import com.errymaricha.dafydiobooth.station.network.AppResult
import com.errymaricha.dafydiobooth.station.network.DeviceApiService
import com.errymaricha.dafydiobooth.station.security.TokenStore
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    @Test
    fun `login stores token when auth success`() = runTest {
        val tokenStore = InMemoryTokenStore()
        val api = FakeApiService()
        val repo = AuthRepositoryImpl(api, tokenStore)

        val result = repo.login("DEV-001", "api-key")

        assertTrue(result is AppResult.Success)
        assertEquals("sanctum-token", tokenStore.getToken())
    }
}

private class InMemoryTokenStore : TokenStore {
    private var token: String? = null
    override fun saveToken(token: String) { this.token = token }
    override fun getToken(): String? = token
    override fun clear() { token = null }
}

private class FakeApiService : DeviceApiService {
    override suspend fun auth(request: DeviceAuthRequest): DeviceAuthResponse {
        return DeviceAuthResponse(token = "sanctum-token", deviceId = "1", stationId = "2")
    }

    override suspend fun heartbeat(bearerToken: String, request: HeartbeatRequest): HeartbeatResponse = notUsed()
    override suspend fun getTemplates(bearerToken: String): List<TemplateDto> = notUsed()
    override suspend fun verifyVoucher(bearerToken: String, request: VerifyVoucherRequest): VerifyVoucherResponse = notUsed()
    override suspend fun paymentQuote(bearerToken: String, request: PaymentQuoteRequest): PaymentQuoteResponse = notUsed()
    override suspend fun createSession(bearerToken: String, request: CreateSessionRequest): CreateSessionResponse = notUsed()
    override suspend fun paymentCheck(bearerToken: String, sessionId: String): PaymentCheckResponse = notUsed()
    override suspend fun uploadPhoto(bearerToken: String, sessionId: String, photo: MultipartBody.Part, captureIndex: RequestBody, slotIndex: RequestBody?) = Unit
    override suspend fun completeSession(bearerToken: String, sessionId: String): SessionCompleteResponse = notUsed()
    override suspend fun uploadRenderedOutput(bearerToken: String, sessionId: String, renderedImage: MultipartBody.Part, editJobId: RequestBody) = Unit

    private fun <T> notUsed(): T = throw UnsupportedOperationException("Not used in this test")
}
