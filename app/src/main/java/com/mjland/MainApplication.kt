package com.mjland

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

class MainApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) 
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.10) 
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(okhttp3.Cache(
                        directory = cacheDir.resolve("image_network_cache"),
                        maxSize = 100L * 1024L * 1024L 
                    ))
                    .build()
            }
            .allowHardware(true) 
            .crossfade(true) 
            .respectCacheHeaders(false) 
            .build()
    }
}
