package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.ArrowForward: ImageVector
    get() {
        if (_IcoArrowForward != null) {
            return _IcoArrowForward!!
        }
        _IcoArrowForward = ImageVector.Builder(
            name = "IcoArrow",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.33239f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.994f, 3.331f)
                lineTo(12.658f, 7.994f)
                lineTo(7.994f, 12.658f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.33239f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.331f, 7.994f)
                horizontalLineTo(12.658f)
            }
        }.build()

        return _IcoArrowForward!!
    }

@Suppress("ObjectPropertyName")
private var _IcoArrowForward: ImageVector? = null
