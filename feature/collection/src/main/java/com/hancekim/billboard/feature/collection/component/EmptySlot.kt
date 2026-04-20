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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons

@Composable
fun EmptySlot(
    size: Dp,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_$index")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = index * 180),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha_$index",
    )

    Box(
        modifier = modifier
            .size(size)
            .border(1.5.dp, Color.White.copy(alpha = 0.18f * alpha / 0.65f), RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BillboardIcons.Album,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f * alpha / 0.65f),
            modifier = Modifier.size(size * 0.35f),
        )
    }
}
