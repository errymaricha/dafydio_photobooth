package com.errymaricha.dafydiobooth.station.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatusStore
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository

class AppWorkerFactory(
    private val deviceRepository: DeviceRepository,
    private val statusStore: HeartbeatStatusStore,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            HeartbeatWorker::class.java.name -> HeartbeatWorker(appContext, workerParameters, deviceRepository, statusStore)
            OfflineQueueWorker::class.java.name -> OfflineQueueWorker(appContext, workerParameters, deviceRepository)
            else -> null
        }
    }
}
