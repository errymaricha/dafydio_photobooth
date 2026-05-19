package com.errymaricha.dafydiobooth.ui.booth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.local.TemplateAssetStore
import com.errymaricha.dafydiobooth.data.local.TemplateSqliteStore
import com.errymaricha.dafydiobooth.data.session.SessionStateManager
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases
import com.errymaricha.dafydiobooth.station.local.HeartbeatStatusStore
import com.errymaricha.dafydiobooth.station.repository.DeviceRepository
import com.errymaricha.dafydiobooth.station.security.TokenStore

class BoothViewModelFactory(
    private val appContext: Context,
    private val useCases: PhotoboothUseCases,
    private val configStore: DeviceConfigStore,
    private val templateSqliteStore: TemplateSqliteStore,
    private val templateAssetStore: TemplateAssetStore,
    private val stationConnectionChecker: StationConnectionChecker,
    private val sessionStateManager: SessionStateManager,
    private val renderedOutputComposer: TemplateRenderedOutputComposer,
    private val heartbeatStatusStore: HeartbeatStatusStore,
    private val stationTokenStore: TokenStore,
    private val stationDeviceRepository: DeviceRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoothViewModel::class.java)) {
            return BoothViewModel(
                appContext,
                useCases,
                configStore,
                templateSqliteStore,
                templateAssetStore,
                stationConnectionChecker,
                sessionStateManager,
                renderedOutputComposer,
                heartbeatStatusStore,
                stationTokenStore,
                stationDeviceRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
