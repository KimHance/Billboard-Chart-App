package com.hancekim.billboard.feature.collection.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun EmptySlot(
    size: Dp,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_$index")
    // 0.46 (= 0.3/0.65) ↔ 1.0 사이로 layer alpha 펄스 — graphicsLayer 람다에서 read 하여 draw phase 로 격리
    val pulseAlpha = infiniteTransition.animateFloat(
        initialValue = 0.46f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = index * 180),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha_$index",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { alpha = pulseAlpha.value }
            .border(
                1.5.dp,
                colorScheme.textPrimary.copy(alpha = 0.18f),
                RoundedCornerShape(14.dp),
            )
            .background(
                colorScheme.textPrimary.copy(alpha = 0.02f),
                RoundedCornerShape(14.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BillboardIcons.Album,
            contentDescription = null,
            tint = colorScheme.textPrimary.copy(alpha = 0.25f),
            modifier = Modifier.size(size * 0.35f),
        )
    }
}

@ThemePreviews
@Composable
private fun EmptySlotPreview() {
    BillboardTheme {
        EmptySlot(size = 82.dp, index = 0)
    }
}
