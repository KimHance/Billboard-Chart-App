package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Logo
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage

@Composable
fun HoloCard(
    albumArtUrl: String,
    cardSize: Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    autoSpeed: Float = 24f,
) {
    val density = LocalDensity.current
    val shader = remember { HoloCardShader.create() }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var heightPx by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "holo_rotate")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (360_000 / autoSpeed).toInt(),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "auto_angle",
    )

    var dragAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val currentAngle = if (isDragging) dragAngle else autoAngle

    val normalizedAngle = ((currentAngle % 360f) + 360f) % 360f
    val showBack = normalizedAngle in 90f..270f

    Box(
        modifier = modifier
            .size(cardSize)
            .onSizeChanged {
                widthPx = it.width.toFloat()
                heightPx = it.height.toFloat()
            }
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragAngle = currentAngle
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                dragAngle += amount * 0.6f
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
                shader = shader,
                currentAngle = currentAngle,
                interactive = interactive,
                widthPx = widthPx,
                heightPx = heightPx,
            )
        }
    }
}

@Composable
private fun CardFrontFace(
    albumArtUrl: String,
    shader: RuntimeShader,
    currentAngle: Float,
    interactive: Boolean,
    widthPx: Float,
    heightPx: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                // 앨범 아트 먼저 그리기
                drawContent()

                // AGSL 셰이더 오버레이
                if (widthPx > 0f && heightPx > 0f) {
                    shader.setFloatUniform("iResolution", widthPx, heightPx)
                    shader.setFloatUniform("iAngle", currentAngle)
                    shader.setFloatUniform("iInteractive", if (interactive) 1f else 0f)
                    drawRect(
                        brush = ShaderBrush(shader),
                        blendMode = BlendMode.Overlay,
                        alpha = 0.7f,
                    )
                }
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
        // 브러시드 메탈 오버레이
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
