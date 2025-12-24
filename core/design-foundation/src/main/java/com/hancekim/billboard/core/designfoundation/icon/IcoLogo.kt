package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.Logo: ImageVector
    get() {
        if (_IcoLogo != null) {
            return _IcoLogo!!
        }
        _IcoLogo = ImageVector.Builder(
            name = "IcoLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF00FF85))) {
                moveTo(5.999f, 11.998f)
                horizontalLineTo(4.499f)
                curveTo(3.671f, 11.998f, 3f, 12.67f, 3f, 13.498f)
                verticalLineTo(19.497f)
                curveTo(3f, 20.326f, 3.671f, 20.997f, 4.499f, 20.997f)
                horizontalLineTo(5.999f)
                curveTo(6.827f, 20.997f, 7.499f, 20.326f, 7.499f, 19.497f)
                verticalLineTo(13.498f)
                curveTo(7.499f, 12.67f, 6.827f, 11.998f, 5.999f, 11.998f)
                close()
            }
            path(fill = SolidColor(Color(0xFF00FF85))) {
                moveTo(12.748f, 7.499f)
                horizontalLineTo(11.249f)
                curveTo(10.42f, 7.499f, 9.749f, 8.17f, 9.749f, 8.999f)
                verticalLineTo(19.497f)
                curveTo(9.749f, 20.326f, 10.42f, 20.997f, 11.249f, 20.997f)
                horizontalLineTo(12.748f)
                curveTo(13.577f, 20.997f, 14.248f, 20.326f, 14.248f, 19.497f)
                verticalLineTo(8.999f)
                curveTo(14.248f, 8.17f, 13.577f, 7.499f, 12.748f, 7.499f)
                close()
            }
            path(fill = SolidColor(Color(0xFF00FF85))) {
                moveTo(19.497f, 3f)
                horizontalLineTo(17.998f)
                curveTo(17.169f, 3f, 16.498f, 3.671f, 16.498f, 4.499f)
                verticalLineTo(19.497f)
                curveTo(16.498f, 20.326f, 17.169f, 20.997f, 17.998f, 20.997f)
                horizontalLineTo(19.497f)
                curveTo(20.326f, 20.997f, 20.997f, 20.326f, 20.997f, 19.497f)
                verticalLineTo(4.499f)
                curveTo(20.997f, 3.671f, 20.326f, 3f, 19.497f, 3f)
                close()
            }
        }.build()

        return _IcoLogo!!
    }

@Suppress("ObjectPropertyName")
private var _IcoLogo: ImageVector? = null
