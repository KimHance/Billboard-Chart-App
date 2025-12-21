package com.hancekim.billboard.core.designfoundation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Float.pixelToDp(): Dp {
    val density = LocalDensity.current
    return density.run {
        this@pixelToDp.toDp()
    }
}

@Composable
fun Int.pixelToDp(): Dp {
    val density = LocalDensity.current
    return density.run {
        this@pixelToDp.toDp()
    }
}

@Composable
fun Dp.dpToPixel(): Float {
    val density = LocalDensity.current
    return density.run {
        this@dpToPixel.toPx()
    }
}
