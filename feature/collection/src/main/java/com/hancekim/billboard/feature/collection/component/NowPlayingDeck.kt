package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hancekim.billboard.core.domain.model.Group
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.core.resource.R

private val DECK_SIZE = 224.dp
private val GLOW_SIZE = 560.dp

@Composable
fun NowPlayingDeck(
    card: CollectedCard,
    group: Group,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupColor = Color(group.colorArgb)
    val inspectLabel = stringResource(R.string.cd_inspect_card)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 카드 + 배경 글로우 — 글로우는 unbounded 로 부모(=224dp) 밖으로 넘쳐 흐르게.
        Box(
            modifier = Modifier.size(DECK_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .size(GLOW_SIZE)
                    .blur(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to groupColor.copy(alpha = 0.32f),
                                0.35f to groupColor.copy(alpha = 0.16f),
                                0.65f to groupColor.copy(alpha = 0.05f),
                                1f to Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
            // 카드 키가 바뀌면 새 HoloCard 인스턴스 → angle Animatable 이 0 으로 재시작.
            key(card.key) {
                HoloCard(
                    albumArtUrl = card.albumArtUrl,
                    cardSize = DECK_SIZE,
                    interactive = true,
                    autoSpeed = 8f,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        GroupChipSm(group = group)
        Spacer(Modifier.height(10.dp))

        Text(
            text = card.title,
            color = BillboardTheme.colorScheme.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            lineHeight = 22.5.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = card.artist,
            color = BillboardTheme.colorScheme.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.8.sp,
        )
        Spacer(Modifier.height(4.dp))

        // INSPECT CARD pill — bg/border 모두 textPrimary 의 저알파로 라이트/다크 모두 대응.
        val pillSurface = BillboardTheme.colorScheme.textPrimary
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(pillSurface.copy(alpha = 0.08f))
                .drawBehind {
                    drawRoundRect(
                        color = pillSurface.copy(alpha = 0.15f),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f, size.height / 2f),
                    )
                }
                .noRippleClickable(onClick = onInspect)
                .semantics {
                    role = Role.Button
                    contentDescription = inspectLabel
                }
                .padding(horizontal = 16.dp, vertical = 7.dp),
        ) {
            Text(
                text = stringResource(R.string.collection_inspect_card),
                color = BillboardTheme.colorScheme.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.4.sp,
            )
        }
    }
}

@Composable
fun NowPlayingDeckEmpty(modifier: Modifier = Modifier) {
    val onSurface = BillboardTheme.colorScheme.textPrimary
    val borderColor = onSurface.copy(alpha = 0.20f)
    val surfaceColor = onSurface.copy(alpha = 0.04f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(DECK_SIZE)
                .background(
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp),
                )
                .drawWithCache {
                    val stroke = Stroke(width = 1.5.dp.toPx(), pathEffect = dash)
                    onDrawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = borderColor,
                            style = stroke,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                16.dp.toPx(), 16.dp.toPx(),
                            ),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = BillboardIcons.Album,
                    contentDescription = null,
                    tint = onSurface.copy(alpha = 0.28f),
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.collection_nothing_playing),
                    color = onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                )
            }
        }
    }
}

@Composable
private fun GroupChipSm(group: Group) {
    val color = Color(group.colorArgb)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.10f))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.33f),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f, size.height / 2f),
                )
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Text(
            text = group.name.uppercase(),
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 1.4.sp,
        )
    }
}

// 사용처: CollectionUi 의 그룹·카운트 서브라인 (top 10dp / left 24dp / right 16dp)
@Composable
fun CollectionSubline(
    totalCount: Int,
    inGroupCount: Int,
    group: Group,
    modifier: Modifier = Modifier,
) {
    val color = Color(group.colorArgb)
    Text(
        text = stringResource(
            R.string.collection_subline,
            totalCount,
            inGroupCount,
            group.name.uppercase(),
        ),
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 24.dp, end = 16.dp),
    )
}

@Composable
fun CollectionDivider(modifier: Modifier = Modifier) {
    val accent = BillboardTheme.colorScheme.textPrimary.copy(alpha = 0.16f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, accent, Color.Transparent),
                ),
            ),
    )
}

@ThemePreviews
@Composable
private fun NowPlayingDeckPreview() {
    BillboardTheme {
        NowPlayingDeck(
            card = CollectedCard("a", "Preview Title", "Preview Artist", "", 0L, 1, 1, 4),
            group = Group(Group.DEFAULT_ID, Group.DEFAULT_NAME, Group.DEFAULT_COLOR_ARGB, 0L),
            onInspect = {},
        )
    }
}

@ThemePreviews
@Composable
private fun NowPlayingDeckEmptyPreview() {
    BillboardTheme {
        NowPlayingDeckEmpty()
    }
}

