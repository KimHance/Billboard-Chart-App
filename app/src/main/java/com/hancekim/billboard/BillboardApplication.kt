package com.hancekim.billboard

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.appfunctions.service.AppFunctionConfiguration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.hancekim.billboard.appfunctions.BillboardFunctions
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BillboardApplication :
    Application(),
    SingletonImageLoader.Factory,
    AppFunctionConfiguration.Provider {

    @Inject
    lateinit var imageLoader: ImageLoader

    // 콜드부팅 시 Hilt 가 BillboardFunctions 를 주입 → AppFunctions 시스템에 노출.
    @Inject
    lateinit var billboardFunctions: BillboardFunctions

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

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(BillboardFunctions::class.java) { billboardFunctions }
            .build()
}