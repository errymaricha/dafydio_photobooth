package com.errymaricha.dafydiobooth.data.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class StationBaseUrlInterceptor(
    private val stationBaseUrlProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val stationBaseUrl = stationBaseUrlProvider().toNormalizedBaseUrl().toHttpUrlOrNull()
        if (stationBaseUrl == null) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val rewrittenUrl = request.url.newBuilder()
            .scheme(stationBaseUrl.scheme)
            .host(stationBaseUrl.host)
            .port(stationBaseUrl.port)
            .build()

        return chain.proceed(
            request.newBuilder()
                .url(rewrittenUrl)
                .build(),
        )
    }
}

private fun String.toNormalizedBaseUrl(): String {
    val value = trim()
    if (value.isBlank()) return ""
    val withScheme = if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "http://$value"
    }
    val withoutSlash = withScheme.trimEnd('/')
    val schemeEnd = withoutSlash.indexOf("://")
    val hostAndMaybePort = if (schemeEnd >= 0) {
        withoutSlash.substring(schemeEnd + 3)
    } else {
        withoutSlash
    }
    val hasPort = hostAndMaybePort.substringBefore('/').contains(":")
    val withDefaultPort = if (hasPort) withoutSlash else "$withoutSlash:8000"
    return "$withDefaultPort/"
}
