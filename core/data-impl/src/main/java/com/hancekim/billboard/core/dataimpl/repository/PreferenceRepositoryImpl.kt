package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import com.hancekim.billboard.core.datasource.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val dataSource: PreferenceDataSource,
) : PreferenceRepository {
    override fun getAppFontFlow(): Flow<Int> =
        dataSource.getAppFontFlow()

    override fun getAppThemeFlow(): Flow<Int> =
        dataSource.getAppThemeFlow()

    override suspend fun setAppFont(fontCode: Int) {
        dataSource.setAppFont(fontCode)
    }

    override suspend fun setAppTheme(themeCode: Int) {
        dataSource.setAppTheme(themeCode)
    }
}
