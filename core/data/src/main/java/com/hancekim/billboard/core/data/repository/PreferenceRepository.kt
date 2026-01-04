package com.hancekim.billboard.core.data.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun getAppFontFlow(): Flow<Int>
    fun getAppThemeFlow(): Flow<Int>
    suspend fun setAppFont(fontCode: Int)
    suspend fun setAppTheme(themeCode: Int)
}