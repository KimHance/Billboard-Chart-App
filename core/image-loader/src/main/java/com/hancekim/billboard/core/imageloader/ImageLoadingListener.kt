package com.hancekim.billboard.core.imageloader

import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import timber.log.Timber

object ImageLoadingListener {
    fun createListener() = object : ImageRequest.Listener {
        override fun onStart(request: ImageRequest) {
            Timber.d("⏳ Loading: ${request.data}")
        }

        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            val cacheInfo = when {
                result.dataSource.name.contains("MEMORY", ignoreCase = true) -> "Memory Cache"
                result.dataSource.name.contains("DISK", ignoreCase = true) -> "Disk Cache"
                else -> "Network"
            }
            Timber.d("✅ Success [$cacheInfo]: ${request.data}")
        }

        override fun onError(request: ImageRequest, result: ErrorResult) {
            Timber.e(result.throwable, "❌ Failed: ${request.data}")
        }

        override fun onCancel(request: ImageRequest) {
            Timber.w("⚠️ Cancelled: ${request.data}")
        }
    }
}