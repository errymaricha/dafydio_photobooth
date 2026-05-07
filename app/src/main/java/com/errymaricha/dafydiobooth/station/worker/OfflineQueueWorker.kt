package com.errymaricha.dafydiobooth.station.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.errymaricha.dafydiobooth.DafydioApplication
import com.errymaricha.dafydiobooth.station.network.AppResult
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository
import java.util.concurrent.TimeUnit

class OfflineQueueWorker(
    appContext: Context,
    params: WorkerParameters,
    private val deviceRepository: DeviceRepository,
) : CoroutineWorker(appContext, params) {
    constructor(
        appContext: Context,
        params: WorkerParameters,
    ) : this(
        appContext = appContext,
        params = params,
        deviceRepository = (appContext.applicationContext as DafydioApplication).stationBootstrap.deviceRepository,
    )

    override suspend fun doWork(): Result {
        return when (deviceRepository.flushOfflineQueue()) {
            is AppResult.Success -> Result.success()
            is AppResult.Failure -> Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "offline_queue_flush_work"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<OfflineQueueWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
