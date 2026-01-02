package com.hancekim.billboard.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

fun Modifier.ignoreHorizontalContentPadding(padding: Dp): Modifier = this.layout { measurable, constraint ->
    val paddingPx = padding.roundToPx()
    val newWidth = constraint.maxWidth + (paddingPx * 2)
    val placeable = measurable.measure(constraint.copy(maxWidth = newWidth))
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}