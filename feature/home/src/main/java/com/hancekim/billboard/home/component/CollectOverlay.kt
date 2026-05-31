package com.hancekim.billboard.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.hancekim.billboard.core.designsystem.componenet.card.SparkleEffect
import com.hancekim.billboard.core.designsystem.componenet.group.GroupBadge
import com.hancekim.billboard.core.designsystem.componenet.group.GroupDropdown
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.Group
import com.hancekim.billboard.core.resource.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OverlayEasing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

@Composable
fun CollectOverlay(
    visible: Boolean,
    chart: Chart?,
    overlayState: OverlayCollectState,
    groups: ImmutableList<Group>,
    selectedGroupId: Long,
    modifier: Modifier = Modifier,
    onSelectGroup: (Long) -> Unit,
    onCommit: () -> Unit,
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

        // 카드 entry 애니메이션
        val cardScale = remember { Animatable(0.21f) }
        val cardTranslationY = remember { Animatable(400f) }
        val contentAlpha = remember { Animatable(0f) }

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

        // 선택된 그룹 색으로 글로우 색을 구동 — animateColorAsState 결과 State 자체를 들고
        // drawBehind 람다(draw phase) 안에서 .value 를 read 해 매 보간 프레임 recompose 회피.
        // targetGlowColor lookup 도 remember 로 캐시해 spring 재시작 / 반복 탐색을 막는다.
        val targetGlowColor = remember(groups, selectedGroupId) {
            groups.firstOrNull { it.id == selectedGroupId }
                ?.colorArgb?.let(::Color) ?: Color.White
        }
        val animatedGlowColor = animateColorAsState(
            targetValue = targetGlowColor,
            animationSpec = tween(300),
            label = "overlay-glow",
        )

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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = cardScale.value
                        scaleY = cardScale.value
                        translationY = cardTranslationY.value
                    },
                ) {
                    // 글로우: drawBehind 내부에서만 색 read → draw phase 격리
                    Box(
                        modifier = Modifier
                            .size(360.dp)
                            .blur(12.dp)
                            .drawBehind {
                                val c = animatedGlowColor.value
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            c.copy(alpha = 0.3f),
                                            Color.Transparent,
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                    ),
                                )
                            },
                    )
                    HoloCard(
                        albumArtUrl = chart.image,
                        cardSize = 240.dp,
                        interactive = true,
                    )
                }

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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val badges = remember(groups) {
                                groups.map { GroupBadge(it.id, it.name, it.colorArgb) }.toImmutableList()
                            }
                            GroupDropdown(
                                items = badges,
                                selectedId = selectedGroupId,
                                onSelect = onSelectGroup,
                            )
                            Spacer(Modifier.height(16.dp))
                            val selected = groups.firstOrNull { it.id == selectedGroupId }
                            if (selected != null) {
                                val (label, bgColor, borderColor) = when (overlayState) {
                                    OverlayCollectState.Uncollected ->
                                        Triple(
                                            stringResource(R.string.home_overlay_add_to, selected.name.uppercase()),
                                            Color(selected.colorArgb),
                                            Color.Transparent,
                                        )

                                    is OverlayCollectState.Collected ->
                                        if (overlayState.groupId == selectedGroupId) {
                                            Triple(
                                                stringResource(R.string.home_overlay_remove),
                                                Color.Transparent,
                                                BillboardTheme.colorScheme.textOnDark,
                                            )
                                        } else {
                                            Triple(
                                                stringResource(R.string.home_overlay_move_to, selected.name.uppercase()),
                                                Color(selected.colorArgb),
                                                Color.Transparent,
                                            )
                                        }
                                }
                                OverlayActionButton(
                                    label = label,
                                    bg = bgColor,
                                    border = borderColor,
                                    onClick = {
                                        // Sparkle 은 add/move 같은 commit 성공시에만 트리거
                                        if (overlayState !is OverlayCollectState.Collected ||
                                            overlayState.groupId != selectedGroupId
                                        ) {
                                            sparkleKey++
                                        }
                                        onCommit()
                                    },
                                )
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
                        text = stringResource(R.string.home_overlay_hint),
                        style = BillboardTheme.typography.labelMd(),
                        color = colorScheme.textOnDarkDisabled,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayActionButton(
    label: String,
    bg: Color,
    border: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .widthIn(min = 260.dp)
            .height(48.dp)
            .background(bg, RoundedCornerShape(24.dp))
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = BillboardTheme.typography.buttonMd(),
            color = BillboardTheme.colorScheme.textOnDark,
        )
    }
}

@ThemePreviews
@Composable
private fun CollectOverlayPreview() {
    BillboardTheme {
        CollectOverlay(
            visible = true,
            chart = Chart(title = "Preview Title", artist = "Preview Artist", rank = 1),
            overlayState = OverlayCollectState.Uncollected,
            groups = persistentListOf(),
            selectedGroupId = Group.DEFAULT_ID,
            onSelectGroup = {},
            onCommit = {},
            onDismiss = {},
        )
    }
}
