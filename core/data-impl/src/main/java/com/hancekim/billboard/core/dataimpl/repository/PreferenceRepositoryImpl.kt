package com.hancekim.billboard.core.dataimpl.repository

import androidx.datastore.core.DataStore
import com.hancekim.billboard.core.data.model.BillboardPreference
import com.hancekim.billboard.core.data.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val preferenceStore: DataStore<BillboardPreference>,
) : PreferenceRepository {
    override fun getAppFontFlow(): Flow<Int> =
        preferenceStore.data.map { it.fontCode }

    override fun getAppThemeFlow(): Flow<Int> = preferenceStore.data.map { it.themeCode }

    override suspend fun setAppFont(fontCode: Int) {
        preferenceStore.updateData { it.copy(fontCode = fontCode) }
    }

    override suspend fun setAppTheme(themeCode: Int) {
        preferenceStore.updateData { it.copy(themeCode = themeCode) }
    }
}