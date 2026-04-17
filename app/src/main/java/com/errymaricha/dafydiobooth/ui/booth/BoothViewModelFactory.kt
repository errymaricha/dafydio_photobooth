package com.errymaricha.dafydiobooth.ui.booth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases

class BoothViewModelFactory(
    private val useCases: PhotoboothUseCases,
    private val configStore: DeviceConfigStore,
    private val stationConnectionChecker: StationConnectionChecker,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoothViewModel::class.java)) {
            return BoothViewModel(useCases, configStore, stationConnectionChecker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
