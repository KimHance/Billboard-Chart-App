package com.hancekim.feature.splash

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashUi(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        contentColor = Color.Unspecified,
        color = BillboardTheme.colorScheme.bgApp
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)),
                exit = ExitTransition.None
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "background")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .sizeIn(maxWidth = 120.dp, maxHeight = 120.dp)
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val gradientBrush = remember {
                            Brush.radialGradient(
                                colors = listOf(
                                    BillboardColor.Green400,
                                    BillboardColor.Green400.copy(alpha = 0.5f),
                                    BillboardColor.Green400.copy(alpha = 0f)
                                )
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    this.alpha = alpha
                                    renderEffect = BlurEffect(50.dp.toPx(), 50.dp.toPx())
                                }
                        ) {
                            drawCircle(
                                brush = gradientBrush,
                            )
                        }
                        ShimmeringLogoIcon(modifier = Modifier.fillMaxSize())
                    }
                    Text(
                        text = "BILLBOARD",
                        style = BillboardTheme.typography.heading2Xl(),
                        color = BillboardTheme.colorScheme.textPrimary,
                    )

                }
            }
        }
    }
}

@Composable
fun ShimmeringLogoIcon(
    modifier: Modifier = Modifier
) {
    val initialColor = BillboardColor.Green400.copy(.45f)
    val barColors = remember {
        listOf(
            Animatable(initialColor),
            Animatable(initialColor),
            Animatable(initialColor)
        )
    }
    LaunchedEffect(Unit) {
        barColors.forEachIndexed { index, animatable ->
            launch {
                delay(index * 200L)
                animatable.animateTo(
                    targetValue = BillboardColor.Green400,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 500),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidthRatio = 0.179f
        val barWidth = size.width * barWidthRatio
        val cornerRadius = CornerRadius(barWidth * 0.30f, barWidth * 0.30f)

        val heights = listOf(0.45f, 0.75f, 1f)
        val gap = (size.width - barWidth * 3) / 4

        val contentHeight = size.height - gap * 2

        heights.forEachIndexed { index, heightRatio ->
            val barHeight = contentHeight * heightRatio
            val x = gap + index * (barWidth + gap)
            val y = gap + contentHeight - barHeight

            drawRoundRect(
                color = barColors[index].value,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Preview
@Composable
private fun ShimmeringLogoIconPreview() {
    ShimmeringLogoIcon(
        modifier = Modifier.size(110.dp)
    )
}


@ThemePreviews
@Composable
private fun SplashUiPreview() {
    BillboardTheme {
        SplashUi()
    }
}