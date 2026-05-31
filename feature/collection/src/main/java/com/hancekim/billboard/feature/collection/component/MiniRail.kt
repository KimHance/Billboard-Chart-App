package com.hancekim.billboard.feature.collection.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoClose
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.core.resource.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val CARD_WIDTH = 100.dp
private val THUMB_SIZE = 100.dp
private val BADGE_SIZE = 24.dp

@Composable
fun MiniRail(
    cards: ImmutableList<CollectedCard>,
    activeKey: String?,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
    ) {
        MiniRailHeader(cardCount = cards.size)
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(cards, key = { it.key }) { card ->
                MiniRailCard(
                    card = card,
                    isActive = card.key == activeKey,
                    onSelect = onSelect,
                    onRemove = onRemove,
                )
            }
        }
    }
}

@Composable
fun MiniRailEmpty(
    group: Group,
    modifier: Modifier = Modifier,
) {
    val groupColor = Color(group.colorArgb)
    val infinite = rememberInfiniteTransition(label = "miniRailEmpty")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(4) { index ->
                val staggered by infinite.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "pulse$index",
                )
                val alpha = 0.35f + 0.30f * staggered
                val translateY = -2f * staggered
                EmptySlot(
                    modifier = Modifier
                        .graphicsLayer {
                            this.alpha = alpha
                            translationY = translateY * density
                        },
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.collection_empty_rail_title, group.name),
            color = BillboardTheme.colorScheme.textOnDark,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            lineHeight = 18.2.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.collection_empty_rail_subtitle, group.name),
            color = BillboardColor.Grey400,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 16.5.sp,
            modifier = Modifier.width(260.dp),
            // 본문 가운데 정렬은 의도적으로 사용하지 않음 — 디자인 스펙상 가운데 정렬 컬럼 안에서 자체 left-align
        )
    }
}

@Composable
private fun MiniRailHeader(cardCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (cardCount == 1) {
                stringResource(R.string.collection_cards_count_one)
            } else {
                stringResource(R.string.collection_cards_count, cardCount)
            },
            color = BillboardTheme.colorScheme.textOnDark,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
        )
        Text(
            text = stringResource(R.string.collection_rail_hint),
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
        )
    }
}

@Composable
private fun MiniRailCard(
    card: CollectedCard,
    isActive: Boolean,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val accent = BillboardTheme.colorScheme.accent
    val bgColor = BillboardTheme.colorScheme.bgApp
    val removeLabel = stringResource(R.string.cd_remove_card, card.title)
    val thumbDesc = stringResource(R.string.cd_card_title_by_artist, card.title, card.artist)
    val throttledRemove = throttledProcess(id = "miniRailRemove-${card.key}") { onRemove(card.key) }

    Column(
        modifier = Modifier.width(CARD_WIDTH),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.size(THUMB_SIZE),
        ) {
            // 썸네일 + 활성 글로우
            AsyncImage(
                model = card.albumArtUrl,
                contentDescription = thumbDesc,
                modifier = Modifier
                    .size(THUMB_SIZE)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BillboardColor.Slate800, BillboardColor.Slate900),
                        ),
                    )
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) accent else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .graphicsLayer { alpha = if (isActive) 1f else 0.7f }
                    .noRippleClickable { onSelect(card.key) },
            )
            // ✕ 삭제 배지 — top -7dp, right -7dp, 배경색과 같은 보더로 컷아웃 느낌
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 7.dp, y = (-7).dp)
                    .size(BADGE_SIZE)
                    .background(BillboardColor.BadgeDark, CircleShape)
                    .border(2.dp, bgColor, CircleShape)
                    .noRippleClickable(onClick = throttledRemove)
                    .semantics {
                        role = Role.Button
                        contentDescription = removeLabel
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BillboardIcons.IcoClose,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(
            text = card.title,
            color = BillboardTheme.colorScheme.textOnDark,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 13.75.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(CARD_WIDTH),
        )
        Text(
            text = card.artist,
            color = Color.White.copy(alpha = 0.55f),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 2.dp)
                .width(CARD_WIDTH),
        )
    }
}

@Composable
private fun EmptySlot(modifier: Modifier = Modifier) {
    val border = Color.White.copy(alpha = 0.14f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    Box(
        modifier = modifier
            .size(THUMB_SIZE)
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
            .drawWithCache {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = dash,
                )
                onDrawWithContent {
                    drawContent()
                    drawRoundRect(
                        color = border,
                        style = stroke,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            14.dp.toPx(), 14.dp.toPx(),
                        ),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BillboardIcons.Album,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.28f),
            modifier = Modifier.size(28.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun MiniRailPreview() {
    BillboardTheme {
        MiniRail(
            cards = persistentListOf(
                CollectedCard("a", "Song A", "Artist A", "", 0L, 1, 1, 1),
                CollectedCard("b", "Song B", "Artist B", "", 0L, 2, 1, 2),
                CollectedCard("c", "Song C", "Artist C", "", 0L, 3, 1, 3),
            ),
            activeKey = "a",
            onSelect = {},
            onRemove = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MiniRailEmptyPreview() {
    BillboardTheme {
        MiniRailEmpty(group = Group(1L, "Starred", 0xFF00FF85.toInt(), 0L))
    }
}
