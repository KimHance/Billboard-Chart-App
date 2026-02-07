package com.hancekim.billboard.core.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PreferenceDataSourceImpl @Inject constructor() : PreferenceDataSource {

    private val _appFontFlow = MutableStateFlow(0)
    private val _appThemeFlow = MutableStateFlow(0)

    override fun getAppFontFlow(): Flow<Int> = _appFontFlow.asStateFlow()

    override fun getAppThemeFlow(): Flow<Int> = _appThemeFlow.asStateFlow()

    override suspend fun setAppFont(fontCode: Int) {
        _appFontFlow.value = fontCode
    }

    override suspend fun setAppTheme(themeCode: Int) {
        _appThemeFlow.value = themeCode
    }
}
