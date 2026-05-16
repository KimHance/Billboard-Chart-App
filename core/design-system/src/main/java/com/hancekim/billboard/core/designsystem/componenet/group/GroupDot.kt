package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun GroupDot(
    colorArgb: Int,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    val color = Color(colorArgb)
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.45f), Color.Transparent),
                radius = this.size.minDimension,
            ),
        )
        drawCircle(color = color, radius = this.size.minDimension * 0.42f)
    }
}

@ThemePreviews
@Composable
private fun GroupDotPreview() {
    BillboardTheme {
        GroupDot(colorArgb = BillboardColor.HoloAmber.toArgb())
    }
}
