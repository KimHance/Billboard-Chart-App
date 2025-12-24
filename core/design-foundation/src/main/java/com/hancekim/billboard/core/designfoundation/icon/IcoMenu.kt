package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.Menu: ImageVector
    get() {
        if (_IcoMenu != null) {
            return _IcoMenu!!
        }
        _IcoMenu = ImageVector.Builder(
            name = "IcoMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9993f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.999f, 4.998f)
                horizontalLineTo(19.993f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9993f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.999f, 11.996f)
                horizontalLineTo(19.993f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9993f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.999f, 18.993f)
                horizontalLineTo(19.993f)
            }
        }.build()

        return _IcoMenu!!
    }

@Suppress("ObjectPropertyName")
private var _IcoMenu: ImageVector? = null
