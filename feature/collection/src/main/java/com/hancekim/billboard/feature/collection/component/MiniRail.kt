package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MiniRail(
    cards: ImmutableList<CollectedCard>,
    activeKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(cards, key = { it.key }) { card ->
            val isActive = card.key == activeKey
            AsyncImage(
                model = card.albumArtUrl,
                contentDescription = "${card.title} by ${card.artist}",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { alpha = if (isActive) 1f else 0.5f }
                    .background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(8.dp))
                    .border(
                        width = if (isActive) 1.dp else 0.dp,
                        color = if (isActive) BillboardColor.HoloBlue else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .noRippleClickable { onSelect(card.key) },
            )
        }
    }
}
