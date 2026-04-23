package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RenderEffect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Logo
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HoloCard(
    albumArtUrl: String,
    cardSize: Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    autoSpeed: Float = 24f,
    initialAngle: Float = 0f,
    borderRadius: Dp = 14.dp,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val shape = remember(borderRadius) { RoundedCornerShape(borderRadius) }

    val angle = remember { Animatable(initialAngle) }
    var isDragging by remember { mutableStateOf(false) }
    var lastDragVelocity by remember { mutableFloatStateOf(0f) }

    // 자동 회전 + 드래그 후 관성 감쇠 → 자동 회전 재개
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            if (abs(lastDragVelocity) > 0.01f) {
                angle.animateDecay(
                    initialVelocity = lastDragVelocity * 60f,
                    animationSpec = exponentialDecay(frictionMultiplier = 3f),
                )
                lastDragVelocity = 0f
            }
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
    val showFront = normalizedAngle < 90f || normalizedAngle > 270f

    Box(
        modifier = modifier
            .size(cardSize)
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                lastDragVelocity = 0f
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                val delta = amount * 0.6f
                                lastDragVelocity = delta.coerceIn(-8f, 8f)
                                scope.launch { angle.snapTo(angle.value + delta) }
                            },
                        )
                    }
                } else Modifier,
            )
            .graphicsLayer {
                rotationY = currentAngle
                cameraDistance = 12f * density.density
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showFront) {
            FrontFace(
                albumArtUrl = albumArtUrl,
                currentAngle = currentAngle,
                interactive = interactive,
                shape = shape,
            )
        } else {
            BackFace(cardSize = cardSize, shape = shape)
        }
    }
}

// === 앞면: 앨범 아트 + 메탈릭 효과 ===
@Composable
private fun FrontFace(
    albumArtUrl: String,
    currentAngle: Float,
    interactive: Boolean,
    shape: RoundedCornerShape,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AgslFrontFace(albumArtUrl, currentAngle, interactive, shape)
    } else {
        FallbackFrontFace(albumArtUrl, currentAngle, interactive, shape)
    }
}

// API 33+: AGSL RuntimeShader
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslFrontFace(
    albumArtUrl: String,
    currentAngle: Float,
    interactive: Boolean,
    shape: RoundedCornerShape,
) {
    val shader = remember { HoloCardShader.create() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .graphicsLayer {
                if (size.width > 0f && size.height > 0f) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iAngle", currentAngle)
                    shader.setFloatUniform(
                        "iInteractive",
                        if (interactive) 1f else 0f,
                    )
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "inputShader")
                        .asComposeRenderEffect()
                }
            }
            .innerRim(shape),
    ) {
        BillboardAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = albumArtUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

// API 32: Compose Canvas 폴백 (AGSL 없이 근사)
@Composable
private fun FallbackFrontFace(
    albumArtUrl: String,
    currentAngle: Float,
    interactive: Boolean,
    shape: RoundedCornerShape,
) {
    val normAngle = ((currentAngle % 360f) + 360f) % 360f
    val sheenOpacity = if (interactive) 0.45f else 0.25f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .innerRim(shape),
    ) {
        // 앨범 아트
        BillboardAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = albumArtUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        // Conic sheen 근사: SweepGradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = normAngle }
                .background(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            40f / 360f to Color(0x73DCE6F0),
                            80f / 360f to Color(0x8CB4C8DC),
                            130f / 360f to Color(0xD9F0F4FA),
                            180f / 360f to Color(0x738CAAC8),
                            230f / 360f to Color(0x8CD2DCEB),
                            280f / 360f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                    ),
                    alpha = sheenOpacity,
                ),
        )
        // 브러시드 스트릭 + 하이라이트 근사
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    // 브러시드 세로 홈
                    val lineSpacing = 3.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0x0FFFFFFF),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                        x += lineSpacing
                    }
                    // 스펙큘러 하이라이트
                    val rad = Math.toRadians(normAngle.toDouble())
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val hx = cx + (cos(rad) * cx * 0.4f).toFloat()
                    val hy = cy + (sin(rad) * cy * 0.4f).toFloat()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color(0x30FFFFFF),
                                1f to Color.Transparent,
                            ),
                            center = Offset(hx, hy),
                            radius = size.minDimension * 0.4f,
                        ),
                        radius = size.minDimension * 0.4f,
                        center = Offset(hx, hy),
                    )
                },
        )
    }
}

// 내부 비네팅 + 테두리
private fun Modifier.innerRim(shape: RoundedCornerShape): Modifier = this
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.6f to Color.Transparent,
                    1.0f to Color(0x40000000),
                ),
            ),
        )
    }
    .border(1.dp, Color(0x80DCE1F5), shape)

// === 뒷면: 브러시드 스틸 + 로고 ===
@Composable
private fun BackFace(
    cardSize: Dp,
    shape: RoundedCornerShape,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3A4052),
                        0.5f to Color(0xFF1A1F2B),
                        1f to Color(0xFF2B3244),
                    ),
                ),
            )
            .drawWithContent {
                drawContent()
                val lineSpacing = 3.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color(0x0DFFFFFF),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                    x += lineSpacing
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val logoSize = cardSize * 0.35f
        Image(
            imageVector = BillboardIcons.Logo,
            contentDescription = null,
            modifier = Modifier.size(logoSize),
            colorFilter = ColorFilter.tint(Color(0xB3DCE6F5)),
        )
    }
}
