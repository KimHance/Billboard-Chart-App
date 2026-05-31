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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
import com.hancekim.billboard.core.domain.model.Group
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
            .padding(top = 14.dp, bottom = 20.dp),
    ) {
        MiniRailHeader(
            cardCount = cards.size,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(10.dp))
        // LazyRow 자체는 화면 끝까지 — 양옆 16dp 는 contentPadding 으로 처리해 ✕ 뱃지가 잘리지 않도록.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                // animateFloat 결과 State 자체를 들고, graphicsLayer 람다(draw phase) 안에서 .value read
                // → 매 프레임 컴포지션을 일으키지 않는다 (03-compose-state deferred read).
                val staggered = infinite.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "pulse$index",
                )
                EmptySlot(
                    modifier = Modifier
                        .graphicsLayer {
                            val v = staggered.value
                            alpha = 0.35f + 0.30f * v
                            translationY = -2f * v * density
                        },
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.collection_empty_rail_title, group.name),
            color = BillboardTheme.colorScheme.textPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            lineHeight = 18.2.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.collection_empty_rail_subtitle, group.name),
            color = BillboardTheme.colorScheme.textSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 16.5.sp,
            modifier = Modifier.width(260.dp),
            // 본문 가운데 정렬은 의도적으로 사용하지 않음 — 디자인 스펙상 가운데 정렬 컬럼 안에서 자체 left-align
        )
    }
}

@Composable
private fun MiniRailHeader(cardCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (cardCount == 1) {
                stringResource(R.string.collection_cards_count_one)
            } else {
                stringResource(R.string.collection_cards_count, cardCount)
            },
            color = BillboardTheme.colorScheme.textPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
        )
        Text(
            text = stringResource(R.string.collection_rail_hint),
            color = BillboardTheme.colorScheme.textSecondary,
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
    val onSurface = BillboardTheme.colorScheme.textPrimary
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
                    .background(BillboardTheme.colorScheme.bgImageFallback, RoundedCornerShape(14.dp))
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) accent else onSurface.copy(alpha = 0.10f),
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
                    .background(onSurface.copy(alpha = 0.92f), CircleShape)
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
                    tint = bgColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(
            text = card.title,
            color = onSurface,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 13.75.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(CARD_WIDTH),
        )
        Text(
            text = card.artist,
            color = BillboardTheme.colorScheme.textSecondary,
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
    val onSurface = BillboardTheme.colorScheme.textPrimary
    val border = onSurface.copy(alpha = 0.18f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    Box(
        modifier = modifier
            .size(THUMB_SIZE)
            .background(onSurface.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
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
            tint = onSurface.copy(alpha = 0.32f),
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
        MiniRailEmpty(group = Group(Group.DEFAULT_ID, Group.DEFAULT_NAME, Group.DEFAULT_COLOR_ARGB, 0L))
    }
}
