package com.hancekim.billboard.core.designsystem.componenet.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun ExpandInfo(
    modifier: Modifier = Modifier,
    debut: Int,
    debutDate: String,
    peak: Int,
    peakDate: String,
) {
    Row(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 14.dp,
                    bottomEnd = 14.dp
                )
            )
            .fillMaxWidth()
            .height(175.dp)
            .background(BillboardTheme.colorScheme.bgExpanded)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpandInfoDetail(
            title = "Debut Position",
            number = debut,
            date = debutDate,
            modifier = Modifier.weight(1f),
        )
        ExpandInfoDetail(
            title = "Peak Position",
            number = peak,
            date = peakDate,
            modifier = Modifier.weight(1f),
        )
        ExpandInfoDetail(
            title = "Chart History",
            number = debut,
            date = debutDate,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ExpandInfoDetail(
    title: String,
    number: Int,
    date: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = BillboardTheme.typography.bodyBold(),
            color = BillboardTheme.colorScheme.textOnDark
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .fillMaxWidth()
                .aspectRatio(1f / 1f, matchHeightConstraintsFirst = true)
                .border(
                    width = 1.1.dp,
                    color = BillboardTheme.colorScheme.accent,
                    shape = RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = number.toString(),
                    style = BillboardTheme.typography.heading2Xl(),
                    color = BillboardTheme.colorScheme.accent
                )
                Text(
                    text = date,
                    style = BillboardTheme.typography.dateSm(),
                    color = BillboardTheme.colorScheme.textOnDark
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExpandInfoDetailPreview() {
    BillboardTheme(true) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .background(Color.Black)
        ) {
            ExpandInfoDetail(
                title = "Debut Position",
                number = 8,
                date = "08/22/24",
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ExpandInfoPreview() {
    BillboardTheme {
        ExpandInfo(
            debut = 8,
            debutDate = "08/22/24",
            peak = 1,
            peakDate = "12/25/24"
        )
    }
}