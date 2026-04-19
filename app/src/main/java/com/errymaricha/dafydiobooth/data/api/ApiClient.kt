package com.errymaricha.dafydiobooth.data.api

import com.errymaricha.dafydiobooth.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

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
    ): PhotoboothApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
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
