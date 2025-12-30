package com.hancekim.billboard.core.designsystem.componenet.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoMinus
import com.hancekim.billboard.core.designfoundation.icon.IcoPlus
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
internal fun CenterInfo(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = BillboardTheme.typography.titleMd(),
                color = BillboardTheme.colorScheme.textPrimary,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = BillboardTheme.typography.bodyMd(),
                color = BillboardTheme.colorScheme.textSecondary,
            )
        }
    }
}

@Composable
internal fun RankingInfo(
    lastWeek: Int,
    peak: Int,
    onWeeks: Int,
    expand: Boolean,
    modifier: Modifier = Modifier,
    onExpandButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Info(
                text = "LW",
                number = lastWeek,
            )
            Info(
                text = "PEEK",
                number = peak,
            )
            Info(
                text = "WEEKS",
                number = onWeeks,
            )
        }

        Crossfade(
            modifier = Modifier
                .size(32.dp)
                .noRippleClickable(onClick = throttledProcess {
                    onExpandButtonClick()
                }),
            targetState = expand,
        ) { isExpand ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .then(
                        if (isExpand) {
                            Modifier.background(BillboardTheme.colorScheme.accent)
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = BillboardTheme.colorScheme.borderButton,
                                shape = CircleShape
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = if (isExpand) BillboardIcons.IcoMinus else BillboardIcons.IcoPlus,
                    tint = if (isExpand) BillboardColor.Black else BillboardTheme.colorScheme.borderButton,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
internal fun Info(
    text: String,
    number: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = text,
            style = BillboardTheme.typography.labelMd(),
            color = BillboardTheme.colorScheme.textTertiary,
        )
        Text(
            text = number.toString(),
            style = BillboardTheme.typography.labelBold(),
            color = BillboardTheme.colorScheme.textPrimary,
        )
    }
}

@ThemePreviews
@Composable
private fun CenterInfoPreview() {
    BillboardTheme {
        Box(modifier = Modifier.height(40.dp)) {
            CenterInfo(
                title = "Peaches",
                artist = "Justin Bieber ft. Daniel Caesar & Giveon",
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RankingInfoPreview() {
    BillboardTheme {
        Box(modifier = Modifier.height(40.dp)) {
            RankingInfo(
                lastWeek = 6,
                peak = 1,
                onWeeks = 12,
                expand = false,
            ) {

            }
        }
    }
}