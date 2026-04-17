package com.errymaricha.dafydiobooth.data.station

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
            val api = createUnauthenticatedApi(baseUrl)
            val response = api.auth(
                DeviceAuthRequest(
                    deviceId = deviceId.trim(),
                    token = token.trim(),
                ),
            )
            val bearerToken = response.bearerToken
            if (bearerToken.isBlank()) {
                BoothResult.Failure(BoothError.Validation("Response auth tidak berisi token"))
            } else {
                BoothResult.Success(
                    StationConnection(
                        baseUrl = baseUrl,
                        deviceId = response.deviceCode ?: response.deviceId ?: deviceId.trim(),
                        bearerToken = bearerToken,
                    ),
                )
            }
        } catch (error: HttpException) {
            BoothResult.Failure(error.toBoothError())
        } catch (error: IOException) {
            BoothResult.Failure(BoothError.Network(error.message ?: "Station tidak bisa dijangkau"))
        } catch (error: IllegalArgumentException) {
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
