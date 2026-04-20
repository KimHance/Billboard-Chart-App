package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.Collection: ImageVector
    get() {
        if (_Collection != null) return _Collection!!
        _Collection = ImageVector.Builder(
            name = "Collection",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 4f)
                lineTo(18f, 4f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                lineTo(20f, 16f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 8f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                lineTo(14f, 6f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                lineTo(16f, 18f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                lineTo(6f, 20f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                close()
            }
        }.build()
        return _Collection!!
    }

@Suppress("ObjectPropertyName")
private var _Collection: ImageVector? = null
