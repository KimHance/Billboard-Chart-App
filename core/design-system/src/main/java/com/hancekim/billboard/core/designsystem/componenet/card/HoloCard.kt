package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
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

    // 3D 회전 + 셰이더를 하나의 graphicsLayer 에서 처리
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
                // 3D 회전
                rotationY = currentAngle
                cameraDistance = 12f * density.density

                // 앞면일 때만 셰이더 적용
                if (!showBack && size.width > 0f && size.height > 0f) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iAngle", currentAngle)
                    shader.setFloatUniform("iInteractive", if (interactive) 1f else 0f)
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "inputShader")
                        .asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
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
            // 앨범 아트 직접 렌더 — 중간 Box/레이어 없음
            BillboardAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = albumArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
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
