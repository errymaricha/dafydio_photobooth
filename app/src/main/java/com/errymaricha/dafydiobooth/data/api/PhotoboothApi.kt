package com.errymaricha.dafydiobooth.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PhotoboothApi {
    @POST("api/device/auth")
    suspend fun auth(@Body request: DeviceAuthRequest): DeviceAuthResponse

    @POST("api/device/vouchers/verify")
    suspend fun verifyVoucher(@Body request: VerifyVoucherRequest): VerifyVoucherResponse

    @POST("api/device/payment-quote")
    suspend fun paymentQuote(@Body request: PaymentQuoteRequest): PaymentQuoteResponse

    @POST("api/device/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): CreateSessionResponse

    @GET("api/device/sessions/{id}/payment-check")
    suspend fun paymentCheck(@Path("id") sessionId: String): PaymentCheckResponse

    @POST("api/device/sessions/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") sessionId: String,
        @Body request: ConfirmPaymentRequest,
    ): ConfirmPaymentResponse
}
