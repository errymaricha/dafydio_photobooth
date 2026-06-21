package com.errymaricha.dafydiobooth.station

import android.content.Context
import androidx.work.Configuration
import com.errymaricha.dafydiobooth.BuildConfig
import com.errymaricha.dafydiobooth.station.local.InMemoryOfflineQueueDao
import com.errymaricha.dafydiobooth.station.local.InMemoryTemplateDao
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatusStore
import com.errymaricha.dafydiobooth.station.network.DeviceApiFactory
import com.errymaricha.dafydiobooth.station.repository.AuthRepository
import com.errymaricha.dafydiobooth.station.repository.AuthRepositoryImpl
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository
import com.errymaricha.dafydiobooth.station.repository.DeviceRepositoryImpl
import com.errymaricha.dafydiobooth.station.repository.SessionRepository
import com.errymaricha.dafydiobooth.station.repository.SessionRepositoryImpl
import com.errymaricha.dafydiobooth.station.repository.TemplateRepository
import com.errymaricha.dafydiobooth.station.repository.TemplateRepositoryImpl
import com.errymaricha.dafydiobooth.station.security.TokenStore
import com.errymaricha.dafydiobooth.station.security.SecureTokenStore
import com.errymaricha.dafydiobooth.station.worker.AppWorkerFactory
import com.errymaricha.dafydiobooth.station.worker.HeartbeatWorker
import com.errymaricha.dafydiobooth.station.worker.OfflineQueueWorker

class StationClientBootstrap(private val context: Context) {

    @Volatile
    private var stationBaseUrl: String = BuildConfig.BASE_URL
    private val tokenStore = SecureTokenStore(context)
    val stationTokenStore: TokenStore
        get() = tokenStore
    val heartbeatStatusStore = HeartbeatStatusStore(context)

    private val templateDao = InMemoryTemplateDao()
    private val offlineQueueDao = InMemoryOfflineQueueDao()

    private val api by lazy {
        DeviceApiFactory.create(
            baseUrlProvider = {
                stationBaseUrl.ifBlank { BuildConfig.BASE_URL }
            },
        )
    }

    fun setStationBaseUrl(url: String) {
        stationBaseUrl = url
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(api = api, tokenStore = tokenStore)
    }

    val templateRepository: TemplateRepository by lazy {
        TemplateRepositoryImpl(api = api, authRepository = authRepository, templateDao = templateDao)
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(
            api = api,
            authRepository = authRepository,
            offlineQueueDao = offlineQueueDao,
        )
    }

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(
            api = api,
            authRepository = authRepository,
            offlineQueueDao = offlineQueueDao,
        )
    }

    fun workerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(
                AppWorkerFactory(
                    deviceRepository = deviceRepository,
                    statusStore = heartbeatStatusStore,
                ),
            )
            .build()
    }

    fun startHeartbeat() {
        HeartbeatWorker.ensurePeriodic(context)
        OfflineQueueWorker.enqueue(context)
    }
}
