package com.hancekim.billboard.core.designfoundation.typography

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hancekim.billboard.core.designfoundation.R

object BillboardFont {
    enum class Font { AppFont, Default }

    val appFontFamily = FontFamily(
        Font(R.font.inter, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_bold, FontWeight.Bold),
        Font(R.font.inter_black, FontWeight.Black)
    )

    val defaultFontFamily = FontFamily.Default
}