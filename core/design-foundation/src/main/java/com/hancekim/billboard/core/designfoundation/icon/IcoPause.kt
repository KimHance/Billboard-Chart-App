package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoPause: ImageVector
    get() {
        if (_icoPause != null) {
            return _icoPause!!
        }
        _icoPause = ImageVector.Builder(
            name = "icoPause",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
                path(
                    stroke = SolidColor(Color(0xFF292929)),
                    strokeLineWidth = 2.5f,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(5f, 7f)
                    curveTo(5f, 5.895f, 5.895f, 5f, 7f, 5f)
                    horizontalLineTo(8f)
                    curveTo(9.105f, 5f, 10f, 5.895f, 10f, 7f)
                    verticalLineTo(17f)
                    curveTo(10f, 18.105f, 9.105f, 19f, 8f, 19f)
                    horizontalLineTo(7f)
                    curveTo(5.895f, 19f, 5f, 18.105f, 5f, 17f)
                    verticalLineTo(7f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF292929)),
                    strokeLineWidth = 2.5f,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(14f, 7f)
                    curveTo(14f, 5.895f, 14.895f, 5f, 16f, 5f)
                    horizontalLineTo(17f)
                    curveTo(18.105f, 5f, 19f, 5.895f, 19f, 7f)
                    verticalLineTo(17f)
                    curveTo(19f, 18.105f, 18.105f, 19f, 17f, 19f)
                    horizontalLineTo(16f)
                    curveTo(14.895f, 19f, 14f, 18.105f, 14f, 17f)
                    verticalLineTo(7f)
                    close()
                }
            }
        }.build()

        return _icoPause!!
    }

@Suppress("ObjectPropertyName")
private var _icoPause: ImageVector? = null
