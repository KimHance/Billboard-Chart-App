package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.Album: ImageVector
    get() {
        if (_IcoAlbum != null) {
            return _IcoAlbum!!
        }
        _IcoAlbum = ImageVector.Builder(
            name = "IcoAlbum",
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40f,
            viewportHeight = 40f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20f, 36.667f)
                curveTo(29.205f, 36.667f, 36.667f, 29.205f, 36.667f, 20f)
                curveTo(36.667f, 10.795f, 29.205f, 3.333f, 20f, 3.333f)
                curveTo(10.795f, 3.333f, 3.333f, 10.795f, 3.333f, 20f)
                curveTo(3.333f, 29.205f, 10.795f, 36.667f, 20f, 36.667f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20f, 23.333f)
                curveTo(21.841f, 23.333f, 23.333f, 21.841f, 23.333f, 20f)
                curveTo(23.333f, 18.159f, 21.841f, 16.667f, 20f, 16.667f)
                curveTo(18.159f, 16.667f, 16.667f, 18.159f, 16.667f, 20f)
                curveTo(16.667f, 21.841f, 18.159f, 23.333f, 20f, 23.333f)
                close()
            }
        }.build()

        return _IcoAlbum!!
    }

@Suppress("ObjectPropertyName")
private var _IcoAlbum: ImageVector? = null