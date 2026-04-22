package com.hancekim.billboard.core.designsystem.componenet.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Logo
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun HoloCard(
    albumArtUrl: String,
    cardSize: Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    autoSpeed: Float = 24f,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val angle = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            while (true) {
                angle.animateTo(
                    targetValue = angle.value + 360f,
                    animationSpec = tween(
                        durationMillis = (360_000 / autoSpeed).toInt(),
                        easing = LinearEasing,
                    ),
                )
            }
        }
    }

    val currentAngle = angle.value
    val normalizedAngle = ((currentAngle % 360f) + 360f) % 360f
    val showBack = normalizedAngle in 90f..270f

    Box(
        modifier = modifier
            .size(cardSize)
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                scope.launch { angle.snapTo(angle.value + amount * 0.6f) }
                            },
                        )
                    }
                } else Modifier
            )
            .graphicsLayer {
                rotationY = currentAngle
                cameraDistance = 12f * density.density
                clip = true
                shape = RoundedCornerShape(14.dp)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showBack) {
            CardBackFace(
                cardSize = cardSize,
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            )
        } else {
            // 앨범 아트 (바닥)
            BillboardAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = albumArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            // 메탈릭 효과 오버레이들 (위에 쌓기)
            MetallicOverlay(
                currentAngle = currentAngle,
                interactive = interactive,
            )
        }
    }
}

@Composable
private fun MetallicOverlay(
    currentAngle: Float,
    interactive: Boolean,
) {
    val rad = Math.toRadians(currentAngle.toDouble())
    val lightOffset = sin(rad).toFloat()
    val strength = if (interactive) 1f else 0.4f

    // 빛 스윕 위치 계산
    val sweepStart = 0.35f + lightOffset * 0.3f
    val sweepEnd = 0.65f + lightOffset * 0.3f

    // 빛 스윕 — 사선으로 이동하는 밝은 줄
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        sweepStart to Color.Transparent,
                        (sweepStart + sweepEnd) / 2f to BillboardColor.White.copy(alpha = 0.3f * strength),
                        sweepEnd to Color.Transparent,
                        1f to Color.Transparent,
                    ),
                ),
            ),
    )

    // 가장자리 비네팅
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        BillboardColor.Black.copy(alpha = 0.12f * strength),
                    ),
                ),
            ),
    )
}

@Composable
private fun CardBackFace(
    cardSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BillboardColor.Grey700,
                        BillboardColor.Grey900,
                        BillboardColor.Grey800,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.02f),
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.02f),
                        ),
                    ),
                ),
        )
        Icon(
            imageVector = BillboardIcons.Logo,
            contentDescription = null,
            tint = BillboardColor.Grey300.copy(alpha = 0.7f),
            modifier = Modifier.size(cardSize * 0.35f),
        )
    }
}
