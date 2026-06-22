package com.errymaricha.dafydiobooth.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PhotoboothApi {
    @POST("api/device/auth")
    suspend fun auth(@Body request: DeviceAuthRequest): DeviceAuthResponse

    @GET("api/device/master-data")
    suspend fun getMasterData(
        @Header("Authorization") bearerToken: String? = null,
    ): DeviceMasterDataResponse

    @GET("api/device/templates")
    suspend fun listTemplates(
        @Header("Authorization") bearerToken: String? = null,
        @Query("category") category: String? = null,
        @Query("paper_size") paperSize: String? = null,
        @Query("q") query: String? = null,
        @Query("updated_since") updatedSince: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("include_slots") includeSlots: Boolean? = null,
    ): JsonElement

    @GET("api/device/templates/{templateId}")
    suspend fun getTemplate(
        @Header("Authorization") bearerToken: String? = null,
        @Path("templateId") templateId: String,
        @Query("include_slots") includeSlots: Boolean? = null,
    ): TemplateDto

    @POST("api/device/vouchers/verify")
    suspend fun verifyVoucher(
        @Body request: VerifyVoucherRequest,
        @Header("Authorization") bearerToken: String? = null,
    ): VerifyVoucherResponse

    @POST("api/device/payment-quote")
    suspend fun paymentQuote(
        @Body request: PaymentQuoteRequest,
        @Header("Authorization") bearerToken: String? = null,
    ): PaymentQuoteResponse

    @POST("api/device/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): CreateSessionResponse

    @POST("api/device/sessions")
    suspend fun openSession(
        @Header("Authorization") bearerToken: String,
        @Body request: OpenManualSessionRequest,
    ): SessionCreateResponse

    @GET("api/device/events")
    suspend fun listEvents(
        @Header("Authorization") bearerToken: String? = null,
    ): JsonElement

    @POST("api/device/events")
    suspend fun createEvent(
        @Header("Authorization") bearerToken: String? = null,
        @Body request: CreateDeviceEventRequest,
    ): JsonElement

    @PATCH("api/device/events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Header("Authorization") bearerToken: String? = null,
        @Body request: UpdateDeviceEventRequest,
    ): JsonElement

    @GET("api/device/sessions/{id}/payment-check")
    suspend fun paymentCheck(
        @Path("id") sessionId: String,
        @Header("Authorization") bearerToken: String? = null,
    ): PaymentCheckResponse

    @Streaming
    @GET("api/device/sessions/{id}/payment-check/sse")
    suspend fun paymentCheckSse(
        @Path("id") sessionId: String,
        @Header("Authorization") bearerToken: String? = null,
        @Header("Accept") accept: String = "text/event-stream",
    ): ResponseBody

    @POST("api/device/sessions/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") sessionId: String,
        @Body request: ConfirmPaymentRequest,
    ): ConfirmPaymentResponse

    @Multipart
    @POST("api/device/sessions/{id}/photos")
    suspend fun uploadCapture(
        @Header("Authorization") bearerToken: String? = null,
        @Header("X-Device-Id") deviceId: String? = null,
        @Path("id") sessionId: String,
        @Part photo: MultipartBody.Part,
        @Part("capture_index") captureIndex: RequestBody,
        @Part("slot_index") slotIndex: RequestBody? = null,
    ): ResponseBody

    @POST("api/device/sessions/{id}/complete")
    suspend fun completeSession(
        @Header("Authorization") bearerToken: String? = null,
        @Header("X-Device-Id") deviceId: String? = null,
        @Path("id") sessionId: String,
    ): ResponseBody

    @POST("api/device/sessions/{id}/edit-jobs")
    suspend fun createEditJob(
        @Header("Authorization") bearerToken: String? = null,
        @Header("X-Device-Id") deviceId: String? = null,
        @Path("id") sessionId: String,
        @Body request: CreateEditJobRequest,
    ): JsonElement

    @POST("api/device/edit-jobs/{id}/render")
    suspend fun renderEditJob(
        @Header("Authorization") bearerToken: String? = null,
        @Header("X-Device-Id") deviceId: String? = null,
        @Path("id") editJobId: String,
        @Body request: RenderEditJobRequest = RenderEditJobRequest(force = true),
    ): ResponseBody

    @Multipart
    @POST("api/device/sessions/{id}/rendered-output")
    suspend fun uploadRenderedOutput(
        @Header("Authorization") bearerToken: String? = null,
        @Header("X-Device-Id") deviceId: String? = null,
        @Header("Accept") accept: String = "application/json",
        @Path("id") sessionId: String,
        @Part("edit_job_id") editJobId: RequestBody,
        @Part renderedImage: MultipartBody.Part,
        @Part("width") width: RequestBody? = null,
        @Part("height") height: RequestBody? = null,
        @Part("dpi") dpi: RequestBody? = null,
        @Part("force") force: RequestBody? = null,
    ): ResponseBody
}
