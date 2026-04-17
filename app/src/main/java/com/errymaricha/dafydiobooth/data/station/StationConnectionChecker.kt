package com.errymaricha.dafydiobooth.data.station

import android.util.Log
import com.errymaricha.dafydiobooth.data.api.ApiErrorBody
import com.errymaricha.dafydiobooth.data.api.DeviceAuthRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import java.io.IOException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

data class StationConnection(
    val baseUrl: String,
    val deviceId: String,
    val bearerToken: String,
)

class StationConnectionChecker {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun connect(
        stationIp: String,
        deviceId: String,
        token: String,
    ): BoothResult<StationConnection> {
        val baseUrl = stationIp.toBaseUrl()
        if (baseUrl.isBlank()) {
            return BoothResult.Failure(BoothError.Validation("Station IP wajib diisi"))
        }
        if (deviceId.isBlank()) {
            return BoothResult.Failure(BoothError.Validation("Device ID wajib diisi"))
        }
        if (token.isBlank()) {
            return BoothResult.Failure(BoothError.Validation("Token wajib diisi"))
        }

        return try {
            Log.i(TAG, "Connecting station auth: $baseUrl api/device/auth device=$deviceId")
            val api = createUnauthenticatedApi(baseUrl)
            val response = api.auth(
                DeviceAuthRequest(
                    deviceCode = deviceId.trim(),
                    apiKey = token.trim(),
                ),
            )
            val bearerToken = response.bearerToken
            if (bearerToken.isBlank()) {
                Log.w(TAG, "Station auth succeeded but token is empty")
                BoothResult.Failure(BoothError.Validation("Response auth tidak berisi token"))
            } else {
                Log.i(TAG, "Station auth succeeded: $baseUrl")
                BoothResult.Success(
                    StationConnection(
                        baseUrl = baseUrl,
                        deviceId = response.deviceCode.ifBlank { deviceId.trim() },
                        bearerToken = bearerToken,
                    ),
                )
            }
        } catch (error: HttpException) {
            Log.w(TAG, "Station auth HTTP ${error.code()}", error)
            BoothResult.Failure(error.toBoothError())
        } catch (error: IOException) {
            Log.w(TAG, "Station auth network failed", error)
            BoothResult.Failure(BoothError.Network(error.message ?: "Station tidak bisa dijangkau"))
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "Station auth invalid URL", error)
            BoothResult.Failure(BoothError.Validation("Format Station IP tidak valid"))
        }
    }

    private fun createUnauthenticatedApi(baseUrl: String): PhotoboothApi {
        return com.errymaricha.dafydiobooth.data.api.ApiClient.create(
            baseUrl = baseUrl,
            tokenProvider = { "" },
            deviceIdProvider = { "" },
        )
    }

    private fun HttpException.toBoothError(): BoothError {
        val apiMessage = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ApiErrorBody>(body).message }.getOrNull()
        }
        return when (code()) {
            401 -> BoothError.Unauthorized
            403 -> BoothError.Forbidden
            422 -> BoothError.Validation(apiMessage ?: "Device credential tidak valid")
            else -> BoothError.Unknown(apiMessage ?: "Station HTTP ${code()}")
        }
    }

    private companion object {
        const val TAG = "DafydioStation"
    }
}

private fun String.toBaseUrl(): String {
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
