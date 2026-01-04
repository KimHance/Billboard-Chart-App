package com.hancekim.billboard.core.data.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.hancekim.billboard.core.data.datastore.serializer.BillboardPreferenceSerializer
import com.hancekim.billboard.core.data.model.BillboardPreference
import com.hancekim.common.di.BillboardDispatcher
import com.hancekim.common.di.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providesBillboardPreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(BillboardDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<BillboardPreference> = DataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { BillboardPreference() },
        serializer = BillboardPreferenceSerializer,
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
    ) {
        context.dataStoreFile(BillboardPreferenceSerializer.BIllBOARD_PREFERENCE_PATH)
    }
}