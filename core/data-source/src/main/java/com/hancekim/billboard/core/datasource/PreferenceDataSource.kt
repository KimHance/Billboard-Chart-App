package com.hancekim.billboard.core.datasource

import kotlinx.coroutines.flow.Flow

interface PreferenceDataSource {
    fun getAppFontFlow(): Flow<Int>
    fun getAppThemeFlow(): Flow<Int>
    suspend fun setAppFont(fontCode: Int)
    suspend fun setAppTheme(themeCode: Int)
}
