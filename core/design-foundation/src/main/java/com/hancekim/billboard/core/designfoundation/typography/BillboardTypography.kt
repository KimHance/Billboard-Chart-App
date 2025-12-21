package com.hancekim.billboard.core.designfoundation.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

internal object BillboardTypography {
    @Composable
    fun heading2Xl(
        font: BillboardFont.Font
    ) = setTextStyle(
        font = font,
        fontSize = 36.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun headingXl(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 24.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun headingLg(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 20.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun headingMd(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 18.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun titleMd(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 16.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun buttonMd(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 14.nonScaledSp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.nonScaledSp,
        letterSpacing = 1.25.nonScaledSp,
    )

    @Composable
    fun bodyBold(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 14.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun bodyMd(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 14.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun bodySm(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 12.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun labelMd(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 10.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun labelBold(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 10.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun dateSm(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 9.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun caption(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 8.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    fun tagSm(font: BillboardFont.Font) = setTextStyle(
        font = font,
        fontSize = 8.nonScaledSp,
        fontWeight = FontWeight.Bold,
    )

    @Composable
    private fun setTextStyle(
        font: BillboardFont.Font,
        fontSize: TextUnit,
        fontWeight: FontWeight = FontWeight.Normal,
        lineHeight: TextUnit = TextUnit.Unspecified,
        letterSpacing: TextUnit = TextUnit.Unspecified,
    ) = TextStyle(
        fontFamily = when (font) {
            BillboardFont.Font.AppFont -> BillboardFont.appFontFamily
            BillboardFont.Font.Default -> BillboardFont.defaultFontFamily
        },
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
}

private val Number.nonScaledSp
    @Composable
    get() = (toFloat() / LocalDensity.current.fontScale).sp