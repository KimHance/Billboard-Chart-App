package com.hancekim.billboard.core.designsystem.componenet.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SparkleEffect(
    trigger: Int,
    modifier: Modifier = Modifier,
    particleCount: Int = 12,
    color: Color = BillboardTheme.colorScheme.accent,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(600))
        }
    }

    if (progress.value > 0f && progress.value < 1f) {
        Canvas(modifier = modifier) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = size.minDimension * 0.6f

            repeat(particleCount) { i ->
                val angle = (360f / particleCount) * i
                val rad = Math.toRadians(angle.toDouble())
                val radius = maxRadius * progress.value
                val alpha = 1f - progress.value

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = 4f * (1f - progress.value * 0.5f),
                    center = Offset(
                        centerX + cos(rad).toFloat() * radius,
                        centerY + sin(rad).toFloat() * radius,
                    ),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SparkleEffectPreview() {
    BillboardTheme {
        SparkleEffect(
            trigger = 1,
            modifier = Modifier.size(120.dp),
        )
    }
}
