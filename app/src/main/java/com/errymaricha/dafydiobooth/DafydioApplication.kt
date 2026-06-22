package com.errymaricha.dafydiobooth

import android.app.Application
import androidx.work.Configuration
import com.errymaricha.dafydiobooth.station.StationClientBootstrap
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class DafydioApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {
    val stationBootstrap by lazy { StationClientBootstrap(this) }

    override val workManagerConfiguration: Configuration
        get() = stationBootstrap.workerConfiguration()

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val cacheSize = 10L * 1024 * 1024 // 10 MiB
        val cacheDir = File(context.cacheDir, "coil_cache")
        val cache = Cache(cacheDir, cacheSize)
        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient))
            }
            .build()
    }
}
