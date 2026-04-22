package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RenderEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage
import kotlinx.coroutines.launch

@Composable
fun HoloCard(
    albumArtUrl: String,
    cardSize: Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    autoSpeed: Float = 24f,
    title: String = "",
    artist: String = "",
) {
    val scope = rememberCoroutineScope()
    val shader = remember { HoloCardShader.create() }

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
            .clip(CircleShape)
            .graphicsLayer {
                // Z축 회전 (턴테이블 스핀)
                rotationZ = currentAngle
            },
        contentAlignment = Alignment.Center,
    ) {
        // 레이어 1: 앨범 아트 + AGSL 홈 질감 (RenderEffect)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (size.width > 0f && size.height > 0f) {
                        shader.setFloatUniform("iResolution", size.width, size.height)
                        shader.setFloatUniform("iAngle", currentAngle)
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "inputShader")
                            .asComposeRenderEffect()
                    }
                },
        ) {
            // 레코드판 베이스 (검정)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A)),
            )
            // 앨범 아트
            BillboardAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = albumArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }

        // 레이어 2: 중앙 라벨
        Box(
            modifier = Modifier
                .size(cardSize * 0.30f)
                .clip(CircleShape)
                .background(Color(0xFFC62828)), // 빨간 라벨
            contentAlignment = Alignment.Center,
        ) {
            // 스핀들 홀
            Box(
                modifier = Modifier
                    .size(cardSize * 0.04f)
                    .clip(CircleShape)
                    .background(BillboardColor.Black),
            )
        }
    }
}
