package com.errymaricha.dafydiobooth.station.local

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.heartbeatStatusDataStore by preferencesDataStore(name = "heartbeat_status")

@Serializable
data class HeartbeatStatus(
    val localIp: String = "-",
    val appVersion: String = "-",
    val os: String = "-",
    val capabilities: String = "-",
    val lastHeartbeatAt: String = "-",
    val lastSyncAt: String = "-",
    val lastResult: String = "-",
    val lastSuccessAt: String = "-",
    val consecutiveFailures: Int = 0,
)

class HeartbeatStatusStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("heartbeat_status_json")

    val status: Flow<HeartbeatStatus> = context.heartbeatStatusDataStore.data.map { pref ->
        val raw = pref[key].orEmpty()
        if (raw.isBlank()) HeartbeatStatus() else runCatching { json.decodeFromString(HeartbeatStatus.serializer(), raw) }.getOrElse { HeartbeatStatus() }
    }

    suspend fun save(value: HeartbeatStatus) {
        context.heartbeatStatusDataStore.edit { pref ->
            pref[key] = json.encodeToString(HeartbeatStatus.serializer(), value)
        }
    }

    suspend fun snapshot(): HeartbeatStatus = status.first()
}
