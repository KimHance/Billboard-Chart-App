package com.hancekim.billboard.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.hancekim.billboard.core.designsystem.componenet.card.SparkleEffect
import com.hancekim.billboard.core.domain.model.Chart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OverlayEasing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

@Composable
fun CollectOverlay(
    visible: Boolean,
    chart: Chart?,
    isAlreadyCollected: Boolean,
    isCollectionFull: Boolean,
    modifier: Modifier = Modifier,
    onCollect: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && chart != null,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(250)),
    ) {
        chart ?: return@AnimatedVisibility
        val colorScheme = BillboardTheme.colorScheme
        var sparkleKey by remember { mutableIntStateOf(0) }

        // 카드 entry 애니메이션: scale 0.21 → 1.0 + translationY 아래→제자리
        val cardScale = remember { Animatable(0.21f) }
        val cardTranslationY = remember { Animatable(400f) }
        val contentAlpha = remember { Animatable(0f) }

        // chart 가 바뀌면 entry 애니메이션 재실행 (다른 곡 롱프레스 시 재진입 효과 보장)
        LaunchedEffect(chart.title, chart.artist) {
            cardScale.snapTo(0.21f)
            cardTranslationY.snapTo(400f)
            contentAlpha.snapTo(0f)
            coroutineScope {
                launch { cardScale.animateTo(1f, tween(380, easing = OverlayEasing)) }
                launch { cardTranslationY.animateTo(0f, tween(380, easing = OverlayEasing)) }
                launch {
                    delay(200)
                    contentAlpha.animateTo(1f, tween(250))
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.scrim.copy(alpha = 0.75f))
                .noRippleClickable(onClick = onDismiss),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 110.dp)
                    .noRippleClickable {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 카드 영역: scale + translation 애니메이션
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = cardScale.value
                        scaleY = cardScale.value
                        translationY = cardTranslationY.value
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .size(360.dp)
                            .blur(12.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colorScheme.holoGlow.copy(alpha = 0.3f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                    HoloCard(
                        albumArtUrl = chart.image,
                        cardSize = 240.dp,
                        interactive = true,
                    )
                }

                // 텍스트/버튼 영역: 지연 fade in
                Column(
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha.value },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = chart.title,
                        style = BillboardTheme.typography.titleMd(),
                        color = colorScheme.textOnDark,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = chart.artist,
                        style = BillboardTheme.typography.bodySm(),
                        color = colorScheme.textOnDarkMuted,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.graphicsLayer { clip = false },
                    ) {
                        Crossfade(
                            targetState = isAlreadyCollected,
                            animationSpec = tween(250),
                            label = "button_switch",
                        ) { collected ->
                            val buttonModifier = Modifier
                                .widthIn(min = 260.dp)
                                .height(48.dp)
                            when {
                                collected -> {
                                    Box(
                                        modifier = buttonModifier
                                            .border(
                                                1.dp,
                                                colorScheme.textOnDark.copy(alpha = 0.3f),
                                                RoundedCornerShape(24.dp),
                                            )
                                            .noRippleClickable { onRemove() },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "REMOVE FROM COLLECTION",
                                            style = BillboardTheme.typography.buttonMd(),
                                            color = colorScheme.textOnDark,
                                        )
                                    }
                                }
                                isCollectionFull -> {
                                    Box(
                                        modifier = buttonModifier
                                            .border(
                                                1.dp,
                                                colorScheme.textOnDarkDisabled.copy(alpha = 0.3f),
                                                RoundedCornerShape(24.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "COLLECTION FULL",
                                            style = BillboardTheme.typography.buttonMd(),
                                            color = colorScheme.textOnDarkDisabled,
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = buttonModifier
                                            .background(
                                                colorScheme.accent,
                                                RoundedCornerShape(24.dp),
                                            )
                                            .noRippleClickable {
                                                sparkleKey++
                                                onCollect()
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "ADD TO COLLECTION",
                                            style = BillboardTheme.typography.buttonMd(),
                                            color = colorScheme.onAccent,
                                        )
                                    }
                                }
                            }
                        }
                        SparkleEffect(
                            trigger = sparkleKey,
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer { clip = false },
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "drag card to rotate · tap backdrop to cancel",
                        style = BillboardTheme.typography.labelMd(),
                        color = colorScheme.textOnDarkDisabled,
                    )
                }
            }
        }
    }
}
