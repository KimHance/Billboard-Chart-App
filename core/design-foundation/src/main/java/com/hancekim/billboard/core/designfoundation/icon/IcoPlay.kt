package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoPlay: ImageVector
    get() {
        if (_icoPlay != null) {
            return _icoPlay!!
        }
        _icoPlay = ImageVector.Builder(
            name = "icoPlay",
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
                    moveTo(19f, 10.268f)
                    curveTo(20.333f, 11.038f, 20.333f, 12.962f, 19f, 13.732f)
                    lineTo(10f, 18.928f)
                    curveTo(8.667f, 19.698f, 7f, 18.736f, 7f, 17.196f)
                    lineTo(7f, 6.804f)
                    curveTo(7f, 5.264f, 8.667f, 4.302f, 10f, 5.072f)
                    lineTo(19f, 10.268f)
                    close()
                }
            }
        }.build()

        return _icoPlay!!
    }

@Suppress("ObjectPropertyName")
private var _icoPlay: ImageVector? = null
