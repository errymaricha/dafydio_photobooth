package com.errymaricha.dafydiobooth.data.api

import com.errymaricha.dafydiobooth.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun create(
        baseUrl: String = BuildConfig.BASE_URL,
        stationBaseUrlProvider: () -> String = { "" },
        tokenProvider: () -> String = { "" },
        deviceIdProvider: () -> String = { "" },
        connectTimeoutMillis: Long = 10_000L,
        readTimeoutMillis: Long = 10_000L,
        writeTimeoutMillis: Long = 10_000L,
    ): PhotoboothApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS)
            .addInterceptor(
                StationBaseUrlInterceptor(
                    stationBaseUrlProvider = stationBaseUrlProvider,
                ),
            )
            .addInterceptor(
                DeviceAuthInterceptor(
                    tokenProvider = tokenProvider,
                    deviceIdProvider = deviceIdProvider,
                ),
            )
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PhotoboothApi::class.java)
    }
}
