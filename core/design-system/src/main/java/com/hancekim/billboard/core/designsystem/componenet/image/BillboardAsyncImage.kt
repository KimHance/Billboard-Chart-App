package com.hancekim.billboard.core.designsystem.componenet.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.gif.AnimatedImageDecoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
@NonRestartableComposable
fun BillboardAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeHolder: @Composable () -> Unit = {},
    onState: ((State) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    key: String? = null,
    diskCachePolicy: CachePolicy = CachePolicy.DISABLED,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
) {
    val context = LocalContext.current
    val animatedImageDecoder = remember { AnimatedImageDecoder.Factory() }

    val imageRequestBuilder = remember(model, key) {
        if (model is ImageRequest) {
            model.newBuilder()
        } else {
            ImageRequest.Builder(context)
                .memoryCachePolicy(memoryCachePolicy)
                .diskCachePolicy(diskCachePolicy)
                .memoryCacheKey(key)
                .diskCacheKey(key)
                .crossfade(true)
                .data(model)
        }.decoderFactory(animatedImageDecoder).build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        placeHolder()
        AsyncImage(
            model = imageRequestBuilder,
            contentDescription = contentDescription,
            onState = onState,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }
}
