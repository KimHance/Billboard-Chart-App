package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun GroupChip(
    name: String,
    colorArgb: Int,
    modifier: Modifier = Modifier,
) {
    val color = Color(colorArgb)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GroupDot(colorArgb = colorArgb, size = 8.dp)
        Text(text = name.uppercase(), color = color, style = BillboardTheme.typography.labelMd())
    }
}

@ThemePreviews
@Composable
private fun GroupChipPreview() {
    BillboardTheme {
        GroupChip(name = "Workout", colorArgb = BillboardColor.HoloAmber.toArgb())
    }
}
