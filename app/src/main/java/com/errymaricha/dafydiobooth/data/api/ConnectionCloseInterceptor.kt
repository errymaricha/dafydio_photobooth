package com.errymaricha.dafydiobooth.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds "Connection: close" header to every request, preventing OkHttp from
 * reusing connections. This is necessary because the Laravel PHP dev server
 * is single-threaded: reused connections can become stale and cause
 * "unexpected end of stream" errors when the server closes its side.
 */
class ConnectionCloseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Connection", "close")
            .build()
        return chain.proceed(request)
    }
}
