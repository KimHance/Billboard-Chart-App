package com.hancekim.billboard.core.imageloader

import coil3.util.Logger
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilLogger @Inject constructor() : Logger {

    override var minLevel: Logger.Level = Logger.Level.Debug

    override fun log(
        tag: String,
        level: Logger.Level,
        message: String?,
        throwable: Throwable?
    ) {
        val tag = "BillboardImageLoader"
        val message = buildString {
            when {
                message?.contains("CACHED") == true ||
                        message?.contains("MemoryCache") == true ||
                        message?.contains("DiskCache") == true -> {
                    append("✅ CACHE HIT: ")
                }

                message?.contains("Loading") == true ||
                        message?.contains("Fetching") == true -> {
                    append("⏳ LOADING: ")
                }

                message?.contains("Successful") == true ||
                        message?.contains("Success") == true -> {
                    append("✅ SUCCESS: ")
                }

                throwable != null -> {
                    append("❌ FAILED: ")
                }
            }

            append(message)
        }
        when (level) {
            Logger.Level.Verbose -> Timber.tag(tag).v(throwable, message)
            Logger.Level.Debug -> Timber.tag(tag).d(throwable, message)
            Logger.Level.Info -> Timber.tag(tag).i(throwable, message)
            Logger.Level.Warn -> Timber.tag(tag).w(throwable, message)
            Logger.Level.Error -> Timber.tag(tag).e(throwable, message)
        }
    }
}