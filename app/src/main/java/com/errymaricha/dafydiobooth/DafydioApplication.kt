package com.errymaricha.dafydiobooth

import android.app.Application
import androidx.work.Configuration
import com.errymaricha.dafydiobooth.station.StationClientBootstrap

class DafydioApplication : Application(), Configuration.Provider {
    val stationBootstrap by lazy { StationClientBootstrap(this) }

    override val workManagerConfiguration: Configuration
        get() = stationBootstrap.workerConfiguration()
}
