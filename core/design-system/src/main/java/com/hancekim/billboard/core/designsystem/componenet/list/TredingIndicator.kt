package com.hancekim.billboard.core.designsystem.componenet.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.ArrowDown
import com.hancekim.billboard.core.designfoundation.icon.ArrowForward
import com.hancekim.billboard.core.designfoundation.icon.ArrowUp
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme


@Composable
fun TrendingIndicator(
    status: ChartStatus,
    modifier: Modifier = Modifier
) {
    val background =
        if (status is ChartStatus.Entrance) BillboardTheme.colorScheme.accent else Color.Transparent
    Box(
        modifier = modifier
            .size(24.dp, 64.dp)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        if (status is ChartStatus.Entrance) {
            Text(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                                minHeight = constraints.minWidth,
                                maxHeight = constraints.maxWidth
                            )
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                x = -(placeable.width - placeable.height) / 2,
                                y = (placeable.width - placeable.height) / 2
                            )
                        }
                    }
                    .rotate(-90f),
                text = status.description,
                style = BillboardTheme.typography.tagSm(),
                color = BillboardTheme.colorScheme.onAccent,
                overflow = TextOverflow.Visible,
            )
        } else {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = when (status as ChartStatus.Moved) {
                    ChartStatus.Moved.Down -> BillboardIcons.ArrowDown
                    ChartStatus.Moved.Steady -> BillboardIcons.ArrowForward
                    ChartStatus.Moved.Up -> BillboardIcons.ArrowUp
                },
                tint = BillboardColor.Grey300,
                contentDescription = null
            )

        }
    }
}

@ThemePreviews
@Composable
private fun TrendingIndicatorPreview(
    @PreviewParameter(ChartStatusParameterProvider::class) status: ChartStatus
) {
    BillboardTheme {
        TrendingIndicator(status)
    }
}