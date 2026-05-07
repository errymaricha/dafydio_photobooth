package com.errymaricha.dafydiobooth.station.network

import com.errymaricha.dafydiobooth.station.model.ApiErrorBody
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URI

sealed class AppError {
    data object Unauthorized : AppError()
    data object Forbidden : AppError()
    data class Validation(val message: String) : AppError()
    data class Server(val message: String) : AppError()
    data class Network(val message: String) : AppError()
    data class Unknown(val message: String) : AppError()
}

sealed class AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
}

suspend fun <T> safeApiCall(json: Json = Json { ignoreUnknownKeys = true }, block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val parsed = runCatching { json.decodeFromString(ApiErrorBody.serializer(), body) }.getOrNull()
        val msg = parsed?.message ?: e.message()
        val error = when (e.code()) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            422 -> AppError.Validation(msg)
            in 500..599 -> AppError.Server(msg)
            else -> AppError.Unknown(msg)
        }
        AppResult.Failure(error)
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network(e.message ?: "Network error"))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e.message ?: "Unknown error"))
    }
}

object DeviceApiFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun create(baseUrlProvider: () -> String): DeviceApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val currentBase = runCatching { URI(baseUrlProvider()) }.getOrNull()
                val request = chain.request()
                if (currentBase == null) {
                    chain.proceed(
                        request.newBuilder()
                            .header("Accept", "application/json")
                            .header("X-Requested-With", "XMLHttpRequest")
                            .build(),
                    )
                } else {
                    val updatedUrl = request.url.newBuilder()
                        .scheme(currentBase.scheme ?: request.url.scheme)
                        .host(currentBase.host ?: request.url.host)
                        .port(if (currentBase.port != -1) currentBase.port else request.url.port)
                        .build()
                    chain.proceed(
                        request.newBuilder()
                            .url(updatedUrl)
                            .header("Accept", "application/json")
                            .header("X-Requested-With", "XMLHttpRequest")
                            .build(),
                    )
                }
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DeviceApiService::class.java)
    }
}
