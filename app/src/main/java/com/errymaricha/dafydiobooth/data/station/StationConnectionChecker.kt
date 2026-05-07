package com.errymaricha.dafydiobooth.data.station

import android.util.Log
import com.errymaricha.dafydiobooth.data.api.ApiErrorBody
import com.errymaricha.dafydiobooth.data.api.DeviceAuthRequest
import com.errymaricha.dafydiobooth.data.api.PhotoboothApi
import com.errymaricha.dafydiobooth.domain.model.BoothError
import com.errymaricha.dafydiobooth.domain.model.BoothResult
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
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
        if (deviceId.isBlank()) {
            return BoothResult.Failure(BoothError.Validation("Device ID wajib diisi"))
        }
        if (token.isBlank()) {
            return BoothResult.Failure(BoothError.Validation("Token wajib diisi"))
        }
        val normalizedDeviceId = deviceId.trim()
        val normalizedToken = token.trim()
        val baseUrl = stationIp.toBaseUrl()
        if (baseUrl.isBlank()) {
            Log.i(TAG, "Station IP kosong, mulai auto-discovery")
            return discoverAndConnect(normalizedDeviceId, normalizedToken)
        }

        return tryConnect(baseUrl, normalizedDeviceId, normalizedToken)
    }

    private suspend fun discoverAndConnect(
        deviceId: String,
        token: String,
    ): BoothResult<StationConnection> = coroutineScope {
        val candidates = discoverBaseUrlCandidates()
        if (candidates.isEmpty()) {
            return@coroutineScope BoothResult.Failure(
                BoothError.Network("Auto detect gagal: tidak ada kandidat IP di jaringan lokal"),
            )
        }

        Log.i(TAG, "Auto-discovery mulai, total kandidat=${candidates.size}")
        val limiter = Semaphore(20)
        val jobs = candidates.map { candidate ->
            async {
                limiter.withPermit {
                    when (val result = tryConnect(candidate, deviceId, token, fastTimeout = true)) {
                        is BoothResult.Success -> result.value
                        is BoothResult.Failure -> null
                    }
                }
            }
        }

        for (job in jobs) {
            val found = runCatching { job.await() }.getOrNull()
            if (found != null) {
                jobs.forEach { it.cancel() }
                Log.i(TAG, "Auto-discovery sukses: ${found.baseUrl}")
                return@coroutineScope BoothResult.Success(found)
            }
        }

        BoothResult.Failure(
            BoothError.Network("Auto detect station gagal. Pastikan Android dan station ada di jaringan yang sama."),
        )
    }

    private suspend fun discoverBaseUrlCandidates(): List<String> = withContext(Dispatchers.IO) {
        val prefixes = linkedSetOf<String>()
        val interfaces = runCatching { NetworkInterface.getNetworkInterfaces()?.toList().orEmpty() }.getOrDefault(emptyList())
        interfaces.forEach { network ->
            val addresses = runCatching { network.inetAddresses?.toList().orEmpty() }.getOrDefault(emptyList())
            addresses.forEach { address ->
                if (address is Inet4Address && !address.isLoopbackAddress && address.isSiteLocalAddress) {
                    val host = address.hostAddress ?: return@forEach
                    val parts = host.split(".")
                    if (parts.size == 4) {
                        prefixes += "${parts[0]}.${parts[1]}.${parts[2]}"
                    }
                }
            }
        }

        val candidates = linkedSetOf<String>()
        prefixes.forEach { prefix ->
            (1..254).forEach { last ->
                candidates += "http://$prefix.$last:8000/"
            }
        }
        candidates.toList()
    }

    private suspend fun tryConnect(
        baseUrl: String,
        deviceId: String,
        token: String,
        fastTimeout: Boolean = false,
    ): BoothResult<StationConnection> {
        return try {
            Log.i(TAG, "Connecting station auth: $baseUrl api/device/auth device=$deviceId")
            val api = createUnauthenticatedApi(baseUrl, fastTimeout)
            val response = api.auth(
                DeviceAuthRequest(
                    deviceCode = deviceId,
                    apiKey = token,
                ),
            )
            val bearerToken = response.bearerToken
            if (bearerToken.isBlank()) {
                Log.w(TAG, "Station auth succeeded but token is empty")
                BoothResult.Failure(BoothError.Validation("Response auth tidak berisi token"))
            } else {
                BoothResult.Success(
                    StationConnection(
                        baseUrl = baseUrl,
                        deviceId = response.deviceCode.ifBlank { deviceId },
                        bearerToken = bearerToken,
                    ),
                )
            }
        } catch (error: HttpException) {
            Log.w(TAG, "Station auth HTTP ${error.code()}", error)
            BoothResult.Failure(error.toBoothError())
        } catch (error: IOException) {
            BoothResult.Failure(BoothError.Network(error.message ?: "Station tidak bisa dijangkau"))
        } catch (error: IllegalArgumentException) {
            BoothResult.Failure(BoothError.Validation("Format Station IP tidak valid"))
        }
    }

    private fun createUnauthenticatedApi(baseUrl: String, fastTimeout: Boolean = false): PhotoboothApi {
        return com.errymaricha.dafydiobooth.data.api.ApiClient.create(
            baseUrl = baseUrl,
            tokenProvider = { "" },
            deviceIdProvider = { "" },
            connectTimeoutMillis = if (fastTimeout) 500L else 10_000L,
            readTimeoutMillis = if (fastTimeout) 800L else 10_000L,
            writeTimeoutMillis = if (fastTimeout) 800L else 10_000L,
        )
    }

    private fun HttpException.toBoothError(): BoothError {
        val rawBody = response()?.errorBody()?.string().orEmpty()
        val apiMessage = runCatching { json.decodeFromString<ApiErrorBody>(rawBody).message }.getOrNull()
        val detail = apiMessage?.ifBlank { null }
            ?: rawBody.take(500).ifBlank { null }
            ?: "HTTP ${code()}"
        return when (code()) {
            401 -> BoothError.Unauthorized("Station auth gagal. Detail: $detail")
            403 -> BoothError.Forbidden("Station menolak akses. Detail: $detail")
            422 -> BoothError.Validation("Device credential tidak valid. Detail: $detail")
            else -> BoothError.Unknown("Station HTTP ${code()}: $detail")
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
