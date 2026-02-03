package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakePreferenceRepository @Inject constructor() : PreferenceRepository {
    override fun getAppFontFlow(): Flow<Int> {
        return flowOf(0)
    }

    override fun getAppThemeFlow(): Flow<Int> {
        return flowOf(0)
    }

    override suspend fun setAppFont(fontCode: Int) {}

    override suspend fun setAppTheme(themeCode: Int) {}
}
