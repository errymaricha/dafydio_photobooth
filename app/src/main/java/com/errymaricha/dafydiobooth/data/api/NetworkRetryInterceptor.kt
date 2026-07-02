package com.errymaricha.dafydiobooth.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkRetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMillis: Long = 800L,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 1
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                if (attempt > 1) {
                    val backoff = baseDelayMillis * attempt
                    Log.d("NetworkRetryInterceptor", "Retrying request: ${request.method} ${request.url} (Attempt $attempt/$maxRetries, delay=${backoff}ms)")
                    try {
                        Thread.sleep(backoff)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Retry interrupted", e)
                    }
                }
                val response = chain.proceed(request)
                return response
            } catch (e: IOException) {
                lastException = e
                val message = e.message ?: ""
                Log.w(
                    "NetworkRetryInterceptor",
                    "Request failed on attempt $attempt/$maxRetries: ${request.method} ${request.url}. Error: $message"
                )

                val shouldRetry = isRecoverableNetworkError(e)
                if (!shouldRetry || attempt >= maxRetries) {
                    throw e
                }
            }
            attempt++
        }
        
        throw (lastException ?: IOException("Failed after $maxRetries retries"))
    }

    private fun isRecoverableNetworkError(e: IOException): Boolean {
        val msg = e.message?.lowercase() ?: ""
        // "canceled" means coroutine/call was cancelled intentionally — do NOT retry
        if (msg.contains("canceled") || msg.contains("cancelled")) return false
        return msg.contains("unexpected end of stream") ||
                msg.contains("timeout") ||
                msg.contains("connection reset") ||
                msg.contains("connection refused") ||
                msg.contains("broken pipe") ||
                msg.contains("stream was reset") ||
                e is java.net.SocketTimeoutException ||
                e is java.net.ConnectException
    }
}

