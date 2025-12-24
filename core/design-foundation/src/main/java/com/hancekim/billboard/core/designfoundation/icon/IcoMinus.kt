package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoMinus: ImageVector
    get() {
        if (_IcoMinus != null) {
            return _IcoMinus!!
        }
        _IcoMinus = ImageVector.Builder(
            name = "IcoMinus",
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
                moveTo(3.331f, 7.994f)
                horizontalLineTo(12.658f)
            }
        }.build()

        return _IcoMinus!!
    }

@Suppress("ObjectPropertyName")
private var _IcoMinus: ImageVector? = null
