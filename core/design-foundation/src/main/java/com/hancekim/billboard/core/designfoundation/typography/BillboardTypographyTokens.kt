package com.hancekim.billboard.core.designfoundation.typography

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

sealed interface BillboardTypographyTokens {

    @Composable
    fun heading2Xl(): TextStyle

    @Composable
    fun headingXl(): TextStyle

    @Composable
    fun headingLg(): TextStyle

    @Composable
    fun headingMd(): TextStyle

    @Composable
    fun titleMd(): TextStyle

    @Composable
    fun buttonMd(): TextStyle

    @Composable
    fun bodyBold(): TextStyle

    @Composable
    fun bodyMd(): TextStyle

    @Composable
    fun bodySm(): TextStyle

    @Composable
    fun labelMd(): TextStyle

    @Composable
    fun labelBold(): TextStyle

    @Composable
    fun dateSm(): TextStyle

    @Composable
    fun caption(): TextStyle

    @Composable
    fun tagSm(): TextStyle

    data object AppTypographyTokens : BillboardTypographyTokens {
        @Composable
        override fun heading2Xl() = BillboardTypography.heading2Xl(BillboardFont.Font.AppFont)

        @Composable
        override fun headingXl() = BillboardTypography.headingXl(BillboardFont.Font.AppFont)

        @Composable
        override fun headingLg() = BillboardTypography.headingLg(BillboardFont.Font.AppFont)

        @Composable
        override fun headingMd() = BillboardTypography.headingMd(BillboardFont.Font.AppFont)

        @Composable
        override fun titleMd() = BillboardTypography.titleMd(BillboardFont.Font.AppFont)

        @Composable
        override fun buttonMd() = BillboardTypography.buttonMd(BillboardFont.Font.AppFont)

        @Composable
        override fun bodyBold() = BillboardTypography.bodyBold(BillboardFont.Font.AppFont)

        @Composable
        override fun bodyMd() = BillboardTypography.bodyMd(BillboardFont.Font.AppFont)

        @Composable
        override fun bodySm() = BillboardTypography.bodySm(BillboardFont.Font.AppFont)

        @Composable
        override fun labelMd() = BillboardTypography.labelMd(BillboardFont.Font.AppFont)

        @Composable
        override fun labelBold() = BillboardTypography.labelBold(BillboardFont.Font.AppFont)

        @Composable
        override fun dateSm() = BillboardTypography.dateSm(BillboardFont.Font.AppFont)

        @Composable
        override fun caption() = BillboardTypography.caption(BillboardFont.Font.AppFont)

        @Composable
        override fun tagSm() = BillboardTypography.tagSm(BillboardFont.Font.AppFont)
    }

    data object DeviceTypographyTokens : BillboardTypographyTokens {
        @Composable
        override fun heading2Xl() = BillboardTypography.heading2Xl(BillboardFont.Font.Default)

        @Composable
        override fun headingXl() = BillboardTypography.headingXl(BillboardFont.Font.Default)

        @Composable
        override fun headingLg() = BillboardTypography.headingLg(BillboardFont.Font.Default)

        @Composable
        override fun headingMd() = BillboardTypography.headingMd(BillboardFont.Font.Default)

        @Composable
        override fun titleMd() = BillboardTypography.titleMd(BillboardFont.Font.Default)

        @Composable
        override fun buttonMd() = BillboardTypography.buttonMd(BillboardFont.Font.Default)

        @Composable
        override fun bodyBold() = BillboardTypography.bodyBold(BillboardFont.Font.Default)

        @Composable
        override fun bodyMd() = BillboardTypography.bodyMd(BillboardFont.Font.Default)

        @Composable
        override fun bodySm() = BillboardTypography.bodySm(BillboardFont.Font.Default)

        @Composable
        override fun labelMd() = BillboardTypography.labelMd(BillboardFont.Font.Default)

        @Composable
        override fun labelBold() = BillboardTypography.labelBold(BillboardFont.Font.Default)

        @Composable
        override fun dateSm() = BillboardTypography.dateSm(BillboardFont.Font.Default)

        @Composable
        override fun caption() = BillboardTypography.caption(BillboardFont.Font.Default)

        @Composable
        override fun tagSm() = BillboardTypography.tagSm(BillboardFont.Font.Default)
    }
}

@SuppressLint("ComposeCompositionLocalUsage")
val LocalTypographyTokens =
    staticCompositionLocalOf<BillboardTypographyTokens> { BillboardTypographyTokens.AppTypographyTokens }
