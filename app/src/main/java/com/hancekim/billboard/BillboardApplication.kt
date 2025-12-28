package com.hancekim.billboard

import android.app.Application
import android.content.pm.ApplicationInfo
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BillboardApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var imageLoader: ImageLoader

    val isDebuggable: Boolean
        get() {
            return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

    override fun onCreate() {
        super.onCreate()
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
    }


    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader
}