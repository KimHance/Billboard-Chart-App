package com.hancekim.billboard.core.imageloader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
@NonRestartableComposable
fun BillboardAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {},
    onState: ((State) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    key: String? = null,
    diskCachePolicy: CachePolicy = CachePolicy.DISABLED,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    enableLogging: Boolean = true,
) {
    val context = LocalContext.current

    val imageRequestBuilder = remember(model, key) {
        val builder = when (model) {
            is ImageRequest -> model.newBuilder()
            else -> ImageRequest.Builder(context).data(model)
        }

        builder
            .memoryCachePolicy(memoryCachePolicy)
            .diskCachePolicy(diskCachePolicy)
            .memoryCacheKey(key)
            .diskCacheKey(key)
            .crossfade(true)
            .apply {
                if (enableLogging) {
                    listener(ImageLoadingListener.createListener())
                }
            }
            .build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var imageState by remember { mutableStateOf<State?>(null) }
        if (imageState == null || imageState is State.Loading || imageState is State.Error) {
            placeholder()
        }
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageRequestBuilder,
            contentDescription = contentDescription,
            onState = { state ->
                imageState = state
                onState?.invoke(state)
            },
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }
}