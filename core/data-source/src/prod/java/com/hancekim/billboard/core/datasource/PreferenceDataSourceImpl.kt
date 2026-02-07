package com.hancekim.billboard.core.datasource

import androidx.datastore.core.DataStore
import com.hancekim.billboard.core.data.model.BillboardPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenceDataSourceImpl @Inject constructor(
    private val preferenceStore: DataStore<BillboardPreference>,
) : PreferenceDataSource {
    override fun getAppFontFlow(): Flow<Int> =
        preferenceStore.data.map { it.fontCode }

    override fun getAppThemeFlow(): Flow<Int> =
        preferenceStore.data.map { it.themeCode }

    override suspend fun setAppFont(fontCode: Int) {
        preferenceStore.updateData { it.copy(fontCode = fontCode) }
    }

    override suspend fun setAppTheme(themeCode: Int) {
        preferenceStore.updateData { it.copy(themeCode = themeCode) }
    }
}
