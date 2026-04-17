package com.errymaricha.dafydiobooth.data.station

import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class StationConnection(
    val baseUrl: String,
)

class StationConnectionChecker(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun connect(
        stationIp: String,
        deviceId: String,
        token: String,
    ): BoothResult<StationConnection> = withContext(Dispatchers.IO) {
        val baseUrl = stationIp.toBaseUrl()
        if (baseUrl.isBlank()) {
            return@withContext BoothResult.Failure(BoothError.Validation("Station IP wajib diisi"))
        }
        if (deviceId.isBlank()) {
            return@withContext BoothResult.Failure(BoothError.Validation("Device ID wajib diisi"))
        }
        if (token.isBlank()) {
            return@withContext BoothResult.Failure(BoothError.Validation("Token wajib diisi"))
        }

        val request = Request.Builder()
            .url(baseUrl)
            .header("Accept", "application/json")
            .header("Authorization", "Bearer ${token.trim()}")
            .header("X-Device-Id", deviceId.trim())
            .build()

        try {
            client.newCall(request).execute().use { response ->
                when (response.code) {
                    in 200..299 -> BoothResult.Success(StationConnection(baseUrl))
                    401 -> BoothResult.Failure(BoothError.Unauthorized)
                    403 -> BoothResult.Failure(BoothError.Forbidden)
                    422 -> BoothResult.Failure(BoothError.Validation("Device credential tidak valid"))
                    else -> BoothResult.Failure(BoothError.Unknown("Station HTTP ${response.code}"))
                }
            }
        } catch (error: IOException) {
            BoothResult.Failure(BoothError.Network(error.message ?: "Station tidak bisa dijangkau"))
        } catch (error: IllegalArgumentException) {
            BoothResult.Failure(BoothError.Validation("Format Station IP tidak valid"))
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
    return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
}
