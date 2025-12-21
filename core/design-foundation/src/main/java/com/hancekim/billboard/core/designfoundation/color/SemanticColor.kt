package com.hancekim.billboard.core.designfoundation.color

import android.annotation.SuppressLint
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

sealed interface BillboardColorScheme {
    val bgApp: Color
    val bgCard: Color
    val bgAppbar: Color
    val bgFilter: Color
    val bgExpanded: Color
    val textPrimary: Color
    val textHeading: Color
    val textSecondary: Color
    val textTertiary: Color
    val textOnDark: Color
    val textOnDarkMuted: Color
    val accent: Color
    val onAccent: Color
    val borderButton: Color
    val borderAppbar: Color
    val bgSurface: Color
    val bgImageFallback: Color
    val borderCard: Color
}

data class BillboardLightColorScheme(
    override val bgApp: Color = BillboardColor.Grey50,
    override val bgCard: Color = BillboardColor.White,
    override val bgAppbar: Color = BillboardColor.White,
    override val bgFilter: Color = BillboardColor.Black,
    override val bgExpanded: Color = BillboardColor.Black,
    override val textPrimary: Color = BillboardColor.Black,
    override val textHeading: Color = BillboardColor.Grey950,
    override val textSecondary: Color = BillboardColor.Grey350,
    override val textTertiary: Color = BillboardColor.Grey450,
    override val textOnDark: Color = BillboardColor.White,
    override val textOnDarkMuted: Color = BillboardColor.Grey300,
    override val accent: Color = BillboardColor.Green400,
    override val onAccent: Color = BillboardColor.Black,
    override val borderButton: Color = BillboardColor.Grey300,
    override val borderAppbar: Color = BillboardColor.Grey100,
    override val bgSurface: Color = BillboardColor.Grey800,
    override val bgImageFallback: Color = BillboardColor.Grey200,
    override val borderCard: Color = BillboardColor.Grey100
) : BillboardColorScheme

data class BillboardDarkColorScheme(
    override val bgApp: Color = BillboardColor.Slate950,
    override val bgCard: Color = BillboardColor.Slate900,
    override val bgAppbar: Color = BillboardColor.Slate850,
    override val bgFilter: Color = BillboardColor.Grey950,
    override val bgExpanded: Color = BillboardColor.Slate800,
    override val textPrimary: Color = BillboardColor.White,
    override val textHeading: Color = BillboardColor.Grey50,
    override val textSecondary: Color = BillboardColor.Grey400,
    override val textTertiary: Color = BillboardColor.Grey500,
    override val textOnDark: Color = BillboardColor.White,
    override val textOnDarkMuted: Color = BillboardColor.Grey300,
    override val accent: Color = BillboardColor.Green400,
    override val onAccent: Color = BillboardColor.Black,
    override val borderButton: Color = BillboardColor.Grey600,
    override val borderAppbar: Color = BillboardColor.Slate800,
    override val bgSurface: Color = BillboardColor.Grey800,
    override val bgImageFallback: Color = BillboardColor.Slate800,
    override val borderCard: Color = BillboardColor.Grey700
) : BillboardColorScheme

@SuppressLint("ComposeCompositionLocalUsage")
val LocalColorScheme = staticCompositionLocalOf<BillboardColorScheme> { BillboardLightColorScheme() }