package com.hancekim.billboard.core.designsystem.componenet.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.indication.OffscreenIndication
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.list.ChartStatus.Moved
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage

@Stable
sealed interface ChartStatus {

    @Stable
    sealed interface Entrance : ChartStatus {
        val description: String

        data object New : Entrance {
            override val description: String = "NEW"
        }

        data object ReEntry : Entrance {
            override val description: String = "RE-ENTRY"
        }
    }

    sealed interface Moved : ChartStatus {
        data object Up : Moved
        data object Down : Moved
        data object Steady : Moved
    }
}

fun String.toStatus(): ChartStatus =
    when (this) {
        "New" -> ChartStatus.Entrance.New
        "Re-Entry" -> ChartStatus.Entrance.ReEntry
        "Rising" -> Moved.Up
        "Falling" -> Moved.Down
        else -> Moved.Steady
    }

@Composable
fun RankingItem(
    rank: Int,
    imgUrl: String,
    status: ChartStatus,
    title: String,
    artist: String,
    lastWeek: Int,
    peak: Int,
    onWeeks: Int,
    expand: Boolean,
    debut: Int,
    debutDate: String,
    peakDate: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onExpandButtonClick: () -> Unit,
    onItemClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(14.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    offset = DpOffset(0.dp, 1.dp),
                    spread = 0.dp,
                    color = Color.Black.copy(.15f)
                )
            )
            .fillMaxWidth()
            .background(
                color = BillboardTheme.colorScheme.bgCard,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                indication = OffscreenIndication(Color.LightGray.copy(.1f)),
                onClick = onItemClick,
            ),
    ) {
        DetailInfo(
            rank = rank,
            imgUrl = imgUrl,
            status = status,
            title = title,
            artist = artist,
            lastWeek = lastWeek,
            peak = peak,
            onWeeks = onWeeks,
            expand = expand,
            onExpandButtonClick = onExpandButtonClick
        )
        AnimatedVisibility(
            visible = expand
        ) {
            ExpandInfo(
                debut = debut,
                debutDate = debutDate,
                peak = peak,
                peakDate = peakDate
            )
        }
    }
}

@Composable
private fun DetailInfo(
    rank: Int,
    imgUrl: String,
    status: ChartStatus,
    title: String,
    artist: String,
    lastWeek: Int,
    peak: Int,
    onWeeks: Int,
    expand: Boolean,
    modifier: Modifier = Modifier,
    onExpandButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(99.dp)
            .background(
                color = BillboardTheme.colorScheme.bgCard,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = rank.toString(),
            style = BillboardTheme.typography.titleMd(),
            color = BillboardTheme.colorScheme.textPrimary,
        )
        BillboardAsyncImage(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BillboardTheme.colorScheme.bgImageFallback),
            placeholder = {
                Icon(
                    imageVector = BillboardIcons.Album,
                    tint = BillboardTheme.colorScheme.borderButton,
                    contentDescription = null
                )
            },
            model = imgUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        TrendingIndicator(
            status = status,
        )
        CenterInfo(
            modifier = Modifier.weight(1f),
            title = title,
            artist = artist
        )
        RankingInfo(
            lastWeek = lastWeek,
            peak = peak,
            onWeeks = onWeeks,
            expand = expand,
            onExpandButtonClick = onExpandButtonClick,
        )
    }
}

internal class ChartStatusParameterProvider : PreviewParameterProvider<ChartStatus> {
    override val values: Sequence<ChartStatus>
        get() = sequenceOf(
            ChartStatus.Entrance.New,
            ChartStatus.Entrance.ReEntry,
            Moved.Up,
            Moved.Down,
            Moved.Steady
        )
}

@ThemePreviews
@Composable
private fun RankingItemPreview(
    @PreviewParameter(ChartStatusParameterProvider::class) status: ChartStatus
) {
    BillboardTheme {
        var isExpand by remember { mutableStateOf(false) }
        Box(Modifier.padding(20.dp)) {
            RankingItem(
                rank = 1,
                imgUrl = "",
                status = status,
                title = "Peaches",
                artist = "Justin Bieber ft. Daniel Caesar & Giveon",
                lastWeek = 6,
                peak = 1,
                onWeeks = 12,
                debut = 8,
                debutDate = "08/22/24",
                peakDate = "12/25/24",
                expand = isExpand,
                onExpandButtonClick = { isExpand = !isExpand },
            ) {

            }
        }
    }
}