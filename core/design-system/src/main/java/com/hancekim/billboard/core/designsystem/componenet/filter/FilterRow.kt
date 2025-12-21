package com.hancekim.billboard.core.designsystem.componenet.filter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.modifier.OffscreenIndication
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme

enum class ChartFilter(val text: String) {
    BillboardHot100("BILLBOARD HOT 100™"),
    Billboard200("BILLBOARD 200™"),
    Global200("GLOBAL 200"),
    Artist100("ARTIST 100")
}

@Composable
fun FilterRow(
    modifier: Modifier = Modifier,
    currentIndex: Int = 0,
    onFilterClick: (ChartFilter) -> Unit,
) {
    val listState = rememberLazyListState(currentIndex)

    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem(currentIndex)
    }

    LazyRow(
        modifier = modifier
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 4.dp,
                    offset = DpOffset(0.dp, 2.dp),
                    color = Color.Black.copy(.1f)
                )
            )
            .height(60.dp)
            .fillMaxWidth()
            .background(BillboardTheme.colorScheme.bgFilter),
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        itemsIndexed(
            items = ChartFilter.entries
        ) { index, chart ->
            FilterItem(
                modifier = Modifier.padding(top = 24.dp),
                chart = chart,
                isSelect = index == currentIndex,
                onClick = throttledProcess { onFilterClick(chart) }
            )
        }
    }
}

@Composable
private fun FilterItem(
    chart: ChartFilter,
    isSelect: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val textColor by animateColorAsState(if (isSelect) BillboardTheme.colorScheme.accent else BillboardTheme.colorScheme.textOnDark)
    val glowAlpha by animateFloatAsState(if (isSelect) 0.7f else 0f)
    val style = BillboardTheme.typography.buttonMd()

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = OffscreenIndication(
                    color = BillboardColor.Grey500.copy(.5f),
                ),
                onClick = onClick
            )
            .padding(start = 6.dp, end = 6.dp),
    ) {
        Text(
            text = chart.text,
            style = style,
            color = textColor,
            modifier = Modifier
                .graphicsLayer {
                    alpha = glowAlpha * 0.8f
                    renderEffect = BlurEffect(
                        radiusX = 15f,
                        radiusY = 15f,
                        edgeTreatment = TileMode.Decal
                    )
                }
        )
        Text(
            modifier = Modifier.fillMaxHeight(),
            text = chart.text,
            style = style,
            color = textColor
        )
    }
}

@ThemePreviews
@Composable
private fun FilterRowPreview() {
    var idx by remember { mutableStateOf(0) }

    BillboardTheme {
        FilterRow(
            currentIndex = idx
        ) {
            idx = ChartFilter.entries.indexOf(it)
        }
    }
}