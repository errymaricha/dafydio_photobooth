package com.errymaricha.dafydiobooth.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.errymaricha.dafydiobooth.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

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

    fun currentConfigBlocking(): DeviceConfig = runBlocking {
        config.first()
    }

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
}
