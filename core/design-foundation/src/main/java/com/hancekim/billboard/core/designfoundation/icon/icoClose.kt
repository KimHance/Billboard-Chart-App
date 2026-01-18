package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoClose: ImageVector
    get() {
        if (_IcoClose != null) {
            return _IcoClose!!
        }
        _IcoClose = ImageVector.Builder(
            name = "CloseMdSvgrepoCom",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 18f)
                lineTo(12f, 12f)
                moveTo(12f, 12f)
                lineTo(6f, 6f)
                moveTo(12f, 12f)
                lineTo(18f, 6f)
                moveTo(12f, 12f)
                lineTo(6f, 18f)
            }
        }.build()

        return _IcoClose!!
    }

@Suppress("ObjectPropertyName")
private var _IcoClose: ImageVector? = null
