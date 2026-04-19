package com.errymaricha.dafydiobooth.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PhotoboothApi {
    @POST("api/device/auth")
    suspend fun auth(@Body request: DeviceAuthRequest): DeviceAuthResponse

    @GET("api/device/master-data")
    suspend fun getMasterData(
        @Header("Authorization") bearerToken: String,
    ): DeviceMasterDataResponse

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

    @GET("api/device/sessions/{id}/payment-check")
    suspend fun paymentCheck(
        @Path("id") sessionId: String,
        @Header("Authorization") bearerToken: String? = null,
    ): PaymentCheckResponse

    @POST("api/device/sessions/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") sessionId: String,
        @Body request: ConfirmPaymentRequest,
    ): ConfirmPaymentResponse
}
