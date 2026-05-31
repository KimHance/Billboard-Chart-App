package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.IcoDelete: ImageVector
    get() {
        if (_IcoDelete != null) {
            return _IcoDelete!!
        }
        _IcoDelete = ImageVector.Builder(
            name = "IcoDelete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // 뚜껑 가로선
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 7f)
                horizontalLineTo(20f)
            }
            // 뚜껑 손잡이
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(9f, 7f)
                verticalLineTo(5f)
                horizontalLineTo(15f)
                verticalLineTo(7f)
            }
            // 통 본체
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5f, 7f)
                lineTo(6f, 19f)
                horizontalLineTo(18f)
                lineTo(19f, 7f)
            }
            // 왼쪽 세로줄
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10f, 11f)
                verticalLineTo(15f)
            }
            // 오른쪽 세로줄
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(14f, 11f)
                verticalLineTo(15f)
            }
        }.build()

        return _IcoDelete!!
    }

@Suppress("ObjectPropertyName")
private var _IcoDelete: ImageVector? = null
