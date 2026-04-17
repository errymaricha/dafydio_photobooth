package com.errymaricha.dafydiobooth.data.api

import okhttp3.Interceptor
import okhttp3.Response

class DeviceAuthInterceptor(
    private val tokenProvider: () -> String,
    private val deviceIdProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider().trim()
        val deviceId = deviceIdProvider().trim()
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .apply {
                if (token.isNotBlank() && chain.request().header("Authorization").isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
                if (deviceId.isNotBlank()) {
                    header("X-Device-Id", deviceId)
                }
            }
            .build()

        return chain.proceed(request)
    }
}
