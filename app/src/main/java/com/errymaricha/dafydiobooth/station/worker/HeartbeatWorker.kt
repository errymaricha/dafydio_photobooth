package com.errymaricha.dafydiobooth.station.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.errymaricha.dafydiobooth.BuildConfig
import com.errymaricha.dafydiobooth.DafydioApplication
import com.errymaricha.dafydiobooth.station.model.HeartbeatCapabilities
import com.errymaricha.dafydiobooth.station.model.HeartbeatRequest
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatus
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatusStore
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository
import com.errymaricha.dafydiobooth.station.network.AppResult
import java.net.NetworkInterface
import java.time.Instant
import java.util.Collections
import java.util.concurrent.TimeUnit

class HeartbeatWorker(
    appContext: Context,
    params: WorkerParameters,
    private val deviceRepository: DeviceRepository,
    private val statusStore: HeartbeatStatusStore,
) : CoroutineWorker(appContext, params) {
    constructor(
        appContext: Context,
        params: WorkerParameters,
    ) : this(
        appContext = appContext,
        params = params,
        deviceRepository = (appContext.applicationContext as DafydioApplication).stationBootstrap.deviceRepository,
        statusStore = (appContext.applicationContext as DafydioApplication).stationBootstrap.heartbeatStatusStore,
    )

    override suspend fun doWork(): Result {
        val previous = statusStore.snapshot()
        val resolvedLocalIp = resolveLocalIp()
        val fallbackLocalIp = previous.localIp.takeIf(::isValidIpv4)
        val localIp = resolvedLocalIp.takeIf(::isValidIpv4) ?: fallbackLocalIp.orEmpty()
        if (!isValidIpv4(localIp)) {
            Log.w(
                TAG,
                "HeartbeatWorker retry: localIp tidak valid resolved=$resolvedLocalIp previous=${previous.localIp}",
            )
            statusStore.save(
                previous.copy(
                    localIp = resolvedLocalIp,
                    appVersion = BuildConfig.VERSION_NAME,
                    os = "Android ${Build.VERSION.RELEASE}",
                    capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                ),
            )
            return Result.retry()
        }
        val battery = readBatteryPercent()
        val networkStrength = readNetworkStrength()
        val capabilities = HeartbeatCapabilities(
            camera = true,
            printer = false,
            offlineQueue = true,
            localRender = true,
        )
        val lastSync = Instant.now().toString()
        val payload = HeartbeatRequest(
            deviceType = "android",
            localIp = localIp,
            batteryPercent = battery,
            networkStrength = networkStrength,
            appVersion = BuildConfig.VERSION_NAME,
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            capabilities = capabilities,
            metrics = emptyMap(),
            lastSyncAt = lastSync,
        )
        return when (val result = deviceRepository.sendHeartbeat(payload)) {
            is AppResult.Success -> {
                Log.i(TAG, "HeartbeatWorker success localIp=$localIp")
                statusStore.save(
                    HeartbeatStatus(
                        localIp = localIp,
                        appVersion = BuildConfig.VERSION_NAME,
                        os = "Android ${Build.VERSION.RELEASE}",
                        capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                        lastHeartbeatAt = Instant.now().toString(),
                        lastSyncAt = lastSync,
                        lastResult = "success",
                        lastSuccessAt = Instant.now().toString(),
                        consecutiveFailures = 0,
                    ),
                )
                Result.success()
            }
            is AppResult.Failure -> {
                Log.w(TAG, "HeartbeatWorker failure localIp=$localIp error=${result.error}")
                statusStore.save(
                    HeartbeatStatus(
                        localIp = localIp,
                        appVersion = BuildConfig.VERSION_NAME,
                        os = "Android ${Build.VERSION.RELEASE}",
                        capabilities = "camera=true, printer=false, offline_queue=true, local_render=true",
                        lastHeartbeatAt = Instant.now().toString(),
                        lastSyncAt = lastSync,
                        lastResult = "failed",
                        lastSuccessAt = previous.lastSuccessAt,
                        consecutiveFailures = previous.consecutiveFailures + 1,
                    ),
                )
                Result.retry()
            }
        }
    }

    private fun isValidIpv4(ip: String): Boolean {
        val regex = Regex("""^((25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)$""")
        return regex.matches(ip.trim())
    }

    private fun resolveLocalIp(): String {
        val fromInterfaces = runCatching {
            Collections.list(NetworkInterface.getNetworkInterfaces())
                .flatMap { Collections.list(it.inetAddresses) }
                .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }
                ?.hostAddress
        }.getOrNull().orEmpty()
        if (fromInterfaces.isNotBlank()) return fromInterfaces

        val fromWifi = runCatching {
            val wifi = applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wifi.connectionInfo?.ipAddress ?: 0
            if (ip == 0) "" else {
                "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
            }
        }.getOrDefault("")
        return fromWifi.ifBlank { "-" }
    }

    private fun readBatteryPercent(): Int {
        return runCatching {
            val intent = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        }.getOrDefault(0)
    }

    private fun readNetworkStrength(): Int {
        return runCatching {
            val wifi = applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            WifiManager.calculateSignalLevel(wifi.connectionInfo?.rssi ?: -100, 100).coerceIn(0, 100)
        }.getOrDefault(0)
    }

    companion object {
        private const val TAG = "HeartbeatWorker"
        private const val WORK_NAME = "device_heartbeat_work"
        private const val IMMEDIATE_WORK_NAME = "${WORK_NAME}_immediate"

        fun enqueue(context: Context) {
            enqueueImmediate(context)
            ensurePeriodic(context)
        }

        fun enqueueImmediate(context: Context) {
            val oneTime = OneTimeWorkRequestBuilder<HeartbeatWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTime,
            )
        }

        fun ensurePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
