package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoSoundOn: ImageVector
    get() {
        if (_IcoSoundOn != null) {
            return _IcoSoundOn!!
        }
        _IcoSoundOn = ImageVector.Builder(
            name = "SoundOnSvgrepoCom",
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
                moveTo(2f, 14.959f)
                lineTo(2f, 9.041f)
                curveTo(2f, 8.466f, 2.448f, 8f, 3f, 8f)
                horizontalLineTo(6.586f)
                curveTo(6.851f, 8f, 7.105f, 7.89f, 7.293f, 7.695f)
                lineTo(10.293f, 4.307f)
                curveTo(10.923f, 3.651f, 12f, 4.116f, 12f, 5.043f)
                verticalLineTo(18.957f)
                curveTo(12f, 19.891f, 10.91f, 20.352f, 10.284f, 19.683f)
                lineTo(7.294f, 16.315f)
                curveTo(7.106f, 16.113f, 6.848f, 16f, 6.578f, 16f)
                horizontalLineTo(3f)
                curveTo(2.448f, 16f, 2f, 15.534f, 2f, 14.959f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 8.5f)
                curveTo(17.333f, 10.278f, 17.333f, 13.722f, 16f, 15.5f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 5f)
                curveTo(22.988f, 8.808f, 23.012f, 15.217f, 19f, 19f)
            }
        }.build()

        return _IcoSoundOn!!
    }

@Suppress("ObjectPropertyName")
private var _IcoSoundOn: ImageVector? = null
