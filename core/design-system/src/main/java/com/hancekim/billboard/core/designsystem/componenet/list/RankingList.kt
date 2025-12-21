package com.hancekim.billboard.core.designsystem.componenet.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.image.BillboardAsyncImage

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
    onExpandButtonClick: () -> Unit,
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
            .padding(start = 19.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rank.toString(),
            style = BillboardTheme.typography.headingLg(),
            color = BillboardTheme.colorScheme.textPrimary,
        )
        BillboardAsyncImage(
            modifier = Modifier
                .padding(start = 18.dp)
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BillboardTheme.colorScheme.bgImageFallback),
            placeHolder = {
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
            modifier = Modifier.padding(start = 12.dp),
            status = status,
        )
        CenterInfo(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp),
            title = title,
            artist = artist
        )
        RankingInfo(
            modifier = Modifier.padding(end = 16.dp),
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
            ChartStatus.Moved.Up,
            ChartStatus.Moved.Down,
            ChartStatus.Moved.Steady
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
                expand = isExpand
            ) {
                isExpand = !isExpand
            }
        }
    }
}