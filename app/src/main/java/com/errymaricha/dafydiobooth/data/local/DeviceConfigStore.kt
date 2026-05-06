package com.errymaricha.dafydiobooth.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.errymaricha.dafydiobooth.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.deviceConfigDataStore by preferencesDataStore(name = "device_config")

data class DeviceConfig(
    val deviceId: String = "",
    val token: String = "",
    val authToken: String = "",
    val stationIp: String = BuildConfig.BASE_URL,
    val cameraSource: String = "AndroidDefault",
    val externalCameraStatus: String = "Disconnected",
    val mirrorLiveView: Boolean = false,
    val mirrorCapture: Boolean = false,
    val imageQuality: String = "High",
    val useBackCamera: Boolean = true,
    val useFrontCamera: Boolean = false,
    val denoisePhoto: Boolean = false,
    val countdownSeconds: Int = 3,
    val countdownAudio: Boolean = true,
    val shutterSound: Boolean = true,
    val defaultPrinting: Boolean = true,
    val printUsePhotoboothStation: Boolean = false,
)

class DeviceConfigStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val tokenKey = stringPreferencesKey("token")
    private val authTokenKey = stringPreferencesKey("auth_token")
    private val stationIpKey = stringPreferencesKey("station_ip")
    private val cameraSourceKey = stringPreferencesKey("camera_source")
    private val externalCameraStatusKey = stringPreferencesKey("external_camera_status")
    private val mirrorLiveViewKey = booleanPreferencesKey("mirror_live_view")
    private val mirrorCaptureKey = booleanPreferencesKey("mirror_capture")
    private val imageQualityKey = stringPreferencesKey("image_quality")
    private val useBackCameraKey = booleanPreferencesKey("use_back_camera")
    private val useFrontCameraKey = booleanPreferencesKey("use_front_camera")
    private val denoisePhotoKey = booleanPreferencesKey("denoise_photo")
    private val countdownSecondsKey = intPreferencesKey("countdown_seconds")
    private val countdownAudioKey = booleanPreferencesKey("countdown_audio")
    private val shutterSoundKey = booleanPreferencesKey("shutter_sound")
    private val defaultPrintingKey = booleanPreferencesKey("default_printing")
    private val printUsePhotoboothStationKey = booleanPreferencesKey("print_use_photobooth_station")
    private val templatesJsonKey = stringPreferencesKey("templates_json")

    val config: Flow<DeviceConfig> = context.deviceConfigDataStore.data.map { preferences ->
        DeviceConfig(
            deviceId = preferences[deviceIdKey].orEmpty(),
            token = preferences[tokenKey].orEmpty(),
            authToken = preferences[authTokenKey].orEmpty(),
            stationIp = preferences[stationIpKey] ?: BuildConfig.BASE_URL,
            cameraSource = preferences[cameraSourceKey] ?: "AndroidDefault",
            externalCameraStatus = preferences[externalCameraStatusKey] ?: "Disconnected",
            mirrorLiveView = preferences[mirrorLiveViewKey] ?: false,
            mirrorCapture = preferences[mirrorCaptureKey] ?: false,
            imageQuality = preferences[imageQualityKey] ?: "High",
            useBackCamera = preferences[useBackCameraKey] ?: true,
            useFrontCamera = preferences[useFrontCameraKey] ?: false,
            denoisePhoto = preferences[denoisePhotoKey] ?: false,
            countdownSeconds = preferences[countdownSecondsKey] ?: 3,
            countdownAudio = preferences[countdownAudioKey] ?: true,
            shutterSound = preferences[shutterSoundKey] ?: true,
            defaultPrinting = preferences[defaultPrintingKey] ?: true,
            printUsePhotoboothStation = preferences[printUsePhotoboothStationKey] ?: false,
        )
    }

    val templates: Flow<List<StoredTemplate>> = context.deviceConfigDataStore.data
        .map { preferences -> preferences[templatesJsonKey].orEmpty() }
        .distinctUntilChanged()
        .map { value ->
            if (value.isBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString(ListSerializer(StoredTemplate.serializer()), value)
                }.getOrDefault(emptyList())
            }
        }
        .flowOn(Dispatchers.Default)

    suspend fun save(deviceId: String, token: String) {
        context.deviceConfigDataStore.edit { preferences ->
            preferences[deviceIdKey] = deviceId
            preferences[tokenKey] = token
            preferences[authTokenKey] = ""
        }
    }

    suspend fun save(config: DeviceConfig) {
        context.deviceConfigDataStore.edit { preferences ->
            preferences[deviceIdKey] = config.deviceId
            preferences[tokenKey] = config.token
            preferences[authTokenKey] = config.authToken
            preferences[stationIpKey] = config.stationIp
            preferences[cameraSourceKey] = config.cameraSource
            preferences[externalCameraStatusKey] = config.externalCameraStatus
            preferences[mirrorLiveViewKey] = config.mirrorLiveView
            preferences[mirrorCaptureKey] = config.mirrorCapture
            preferences[imageQualityKey] = config.imageQuality
            preferences[useBackCameraKey] = config.useBackCamera
            preferences[useFrontCameraKey] = config.useFrontCamera
            preferences[denoisePhotoKey] = config.denoisePhoto
            preferences[countdownSecondsKey] = config.countdownSeconds
            preferences[countdownAudioKey] = config.countdownAudio
            preferences[shutterSoundKey] = config.shutterSound
            preferences[defaultPrintingKey] = config.defaultPrinting
            preferences[printUsePhotoboothStationKey] = config.printUsePhotoboothStation
        }
    }

    suspend fun saveTemplates(templates: List<StoredTemplate>) {
        context.deviceConfigDataStore.edit { preferences ->
            preferences[templatesJsonKey] = json.encodeToString(
                ListSerializer(StoredTemplate.serializer()),
                templates,
            )
        }
    }
}

@Serializable
data class StoredTemplate(
    val templateId: String,
    val templateCode: String,
    val templateName: String,
    val category: String? = null,
    val paperSize: String? = null,
    val canvasWidth: Int = 0,
    val canvasHeight: Int = 0,
    val thumbnailUrl: String? = null,
    val thumbnailLocalPath: String? = null,
    val previewUrl: String? = null,
    val previewLocalPath: String? = null,
    val overlayUrl: String? = null,
    val overlayLocalPath: String? = null,
    val configJson: String? = null,
    val slotsJson: String = "[]",
)
