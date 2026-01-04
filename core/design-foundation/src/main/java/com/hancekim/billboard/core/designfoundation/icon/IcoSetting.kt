package com.hancekim.billboard.core.designfoundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BillboardIcons.Setting: ImageVector
    get() {
        if (_Setting != null) {
            return _Setting!!
        }
        _Setting = ImageVector.Builder(
            name = "Setting",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.017f, 19f)
                curveTo(10.66f, 19f, 10.355f, 18.735f, 10.297f, 18.373f)
                curveTo(10.243f, 18.08f, 10.038f, 17.841f, 9.762f, 17.75f)
                curveTo(9.537f, 17.671f, 9.316f, 17.577f, 9.103f, 17.47f)
                curveTo(8.848f, 17.337f, 8.543f, 17.357f, 8.307f, 17.522f)
                curveTo(8.022f, 17.733f, 7.629f, 17.7f, 7.381f, 17.445f)
                lineTo(6.414f, 16.453f)
                curveTo(6.153f, 16.186f, 6.119f, 15.765f, 6.334f, 15.458f)
                curveTo(6.499f, 15.21f, 6.523f, 14.891f, 6.396f, 14.621f)
                curveTo(6.313f, 14.433f, 6.239f, 14.241f, 6.176f, 14.045f)
                curveTo(6.085f, 13.736f, 5.834f, 13.505f, 5.525f, 13.445f)
                curveTo(5.153f, 13.384f, 4.878f, 13.056f, 4.875f, 12.669f)
                verticalLineTo(11.428f)
                curveTo(4.873f, 10.982f, 5.187f, 10.601f, 5.616f, 10.528f)
                curveTo(5.941f, 10.465f, 6.213f, 10.236f, 6.338f, 9.921f)
                curveTo(6.375f, 9.832f, 6.414f, 9.744f, 6.455f, 9.657f)
                curveTo(6.62f, 9.33f, 6.597f, 8.937f, 6.395f, 8.633f)
                curveTo(6.142f, 8.273f, 6.181f, 7.778f, 6.487f, 7.464f)
                lineTo(7.197f, 6.735f)
                curveTo(7.548f, 6.375f, 8.101f, 6.329f, 8.504f, 6.625f)
                lineTo(8.526f, 6.641f)
                curveTo(8.827f, 6.849f, 9.21f, 6.886f, 9.544f, 6.741f)
                curveTo(9.902f, 6.609f, 10.165f, 6.294f, 10.238f, 5.912f)
                lineTo(10.247f, 5.878f)
                curveTo(10.328f, 5.372f, 10.754f, 5f, 11.253f, 5f)
                horizontalLineTo(12.111f)
                curveTo(12.625f, 5f, 13.063f, 5.381f, 13.147f, 5.9f)
                lineTo(13.163f, 5.97f)
                curveTo(13.231f, 6.336f, 13.481f, 6.639f, 13.822f, 6.77f)
                curveTo(14.15f, 6.914f, 14.527f, 6.877f, 14.822f, 6.67f)
                lineTo(14.871f, 6.634f)
                curveTo(15.284f, 6.328f, 15.853f, 6.375f, 16.213f, 6.745f)
                lineTo(16.868f, 7.417f)
                curveTo(17.195f, 7.755f, 17.237f, 8.287f, 16.965f, 8.674f)
                curveTo(16.752f, 8.998f, 16.725f, 9.413f, 16.894f, 9.763f)
                lineTo(16.936f, 9.863f)
                curveTo(17.072f, 10.205f, 17.368f, 10.452f, 17.722f, 10.521f)
                curveTo(18.184f, 10.598f, 18.524f, 11.007f, 18.525f, 11.487f)
                verticalLineTo(12.6f)
                curveTo(18.525f, 13.023f, 18.226f, 13.385f, 17.819f, 13.454f)
                curveTo(17.484f, 13.52f, 17.211f, 13.769f, 17.108f, 14.102f)
                curveTo(17.063f, 14.235f, 17.012f, 14.369f, 16.956f, 14.502f)
                curveTo(16.826f, 14.795f, 16.855f, 15.136f, 17.032f, 15.402f)
                curveTo(17.266f, 15.736f, 17.23f, 16.194f, 16.947f, 16.485f)
                lineTo(16.039f, 17.417f)
                curveTo(15.779f, 17.683f, 15.37f, 17.718f, 15.072f, 17.498f)
                curveTo(14.823f, 17.323f, 14.5f, 17.304f, 14.233f, 17.448f)
                curveTo(14.043f, 17.545f, 13.847f, 17.631f, 13.648f, 17.705f)
                curveTo(13.369f, 17.804f, 13.164f, 18.049f, 13.11f, 18.346f)
                curveTo(13.053f, 18.72f, 12.74f, 18.997f, 12.371f, 19f)
                horizontalLineTo(11.017f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(13.975f, 12f)
                curveTo(13.975f, 13.288f, 12.956f, 14.333f, 11.7f, 14.333f)
                curveTo(10.444f, 14.333f, 9.425f, 13.288f, 9.425f, 12f)
                curveTo(9.425f, 10.712f, 10.444f, 9.667f, 11.7f, 9.667f)
                curveTo(12.956f, 9.667f, 13.975f, 10.712f, 13.975f, 12f)
                close()
            }
        }.build()

        return _Setting!!
    }

@Suppress("ObjectPropertyName")
private var _Setting: ImageVector? = null
