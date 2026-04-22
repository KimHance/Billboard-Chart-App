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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
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
            }
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (showBack) {
            CardBackFace(
                cardSize = cardSize,
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            )
        } else {
            CardFrontFace(
                albumArtUrl = albumArtUrl,
                currentAngle = currentAngle,
                interactive = interactive,
            )
        }
    }
}

@Composable
private fun CardFrontFace(
    albumArtUrl: String,
    currentAngle: Float,
    interactive: Boolean,
) {
    val rad = Math.toRadians(currentAngle.toDouble())
    val lightOffset = sin(rad).toFloat()
    val strength = if (interactive) 1f else 0.4f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()

                // 1) 메탈릭 틴트 — 전체를 약간 은색으로
                drawRect(
                    color = Color(0xFFE0E4EC).copy(alpha = 0.12f * strength),
                    blendMode = BlendMode.SrcAtop,
                )

                // 2) 빛 스윕 — 회전에 따라 사선으로 이동하는 밝은 줄
                val sweepCenter = size.width * (0.5f + lightOffset * 0.4f)
                val sweepWidth = size.width * 0.35f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f * strength),
                            Color.White.copy(alpha = 0.35f * strength),
                            Color.White.copy(alpha = 0.08f * strength),
                            Color.Transparent,
                        ),
                        start = Offset(sweepCenter - sweepWidth, 0f),
                        end = Offset(sweepCenter + sweepWidth, size.height),
                    ),
                )

                // 3) 가장자리 비네팅 — 메탈 테두리
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f * strength),
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 0.7f,
                    ),
                )
            },
    ) {
        BillboardAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = albumArtUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
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
