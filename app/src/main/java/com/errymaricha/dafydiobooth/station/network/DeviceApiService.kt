package com.errymaricha.dafydiobooth.station.network

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DeviceApiService {
    @POST("api/device/auth")
    suspend fun auth(@Body request: DeviceAuthRequest): DeviceAuthResponse

    @POST("api/device/heartbeat")
    suspend fun heartbeat(
        @Header("Authorization") bearerToken: String,
        @Body request: HeartbeatRequest,
    ): HeartbeatResponse

    @GET("api/device/templates")
    suspend fun getTemplates(
        @Header("Authorization") bearerToken: String,
    ): List<TemplateDto>

    @POST("api/device/vouchers/verify")
    suspend fun verifyVoucher(
        @Header("Authorization") bearerToken: String,
        @Body request: VerifyVoucherRequest,
    ): VerifyVoucherResponse

    @POST("api/device/payment-quote")
    suspend fun paymentQuote(
        @Header("Authorization") bearerToken: String,
        @Body request: PaymentQuoteRequest,
    ): PaymentQuoteResponse

    @POST("api/device/sessions")
    suspend fun createSession(
        @Header("Authorization") bearerToken: String,
        @Body request: CreateSessionRequest,
    ): CreateSessionResponse

    @GET("api/device/sessions/{session}/payment-check")
    suspend fun paymentCheck(
        @Header("Authorization") bearerToken: String,
        @Path("session") sessionId: String,
    ): PaymentCheckResponse

    @Multipart
    @POST("api/device/sessions/{session}/photos")
    suspend fun uploadPhoto(
        @Header("Authorization") bearerToken: String,
        @Path("session") sessionId: String,
        @Part photo: MultipartBody.Part,
        @Part("capture_index") captureIndex: RequestBody,
        @Part("slot_index") slotIndex: RequestBody? = null,
    )

    @POST("api/device/sessions/{session}/complete")
    suspend fun completeSession(
        @Header("Authorization") bearerToken: String,
        @Path("session") sessionId: String,
    ): SessionCompleteResponse

    @Multipart
    @POST("api/device/sessions/{session}/rendered-output")
    suspend fun uploadRenderedOutput(
        @Header("Authorization") bearerToken: String,
        @Path("session") sessionId: String,
        @Part renderedImage: MultipartBody.Part,
        @Part("edit_job_id") editJobId: RequestBody,
    )
}
