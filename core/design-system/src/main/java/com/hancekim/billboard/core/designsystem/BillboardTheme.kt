package com.hancekim.billboard.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.hancekim.billboard.core.designfoundation.color.BillboardColorScheme
import com.hancekim.billboard.core.designfoundation.color.BillboardDarkColorScheme
import com.hancekim.billboard.core.designfoundation.color.BillboardLightColorScheme
import com.hancekim.billboard.core.designfoundation.color.LocalColorScheme
import com.hancekim.billboard.core.designfoundation.typography.BillboardTypographyTokens
import com.hancekim.billboard.core.designfoundation.typography.LocalTypographyTokens

@Composable
fun BillboardTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isSystemFont: Boolean = false,
    content: @Composable () -> Unit,
) {

    val colorScheme = remember(isDarkTheme) {
        if (isDarkTheme) BillboardDarkColorScheme() else BillboardLightColorScheme()
    }

    val typography = remember(isSystemFont) {
        if (isSystemFont) BillboardTypographyTokens.DeviceTypographyTokens
        else BillboardTypographyTokens.AppTypographyTokens
    }

    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalTypographyTokens provides typography,
        content = content,
    )
}

object BillboardTheme {
    val colorScheme: BillboardColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: BillboardTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTypographyTokens.current
}