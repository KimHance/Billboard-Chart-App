package com.hancekim.billboard.core.dataimpl.di

import android.content.Context
import androidx.room3.Room
import com.hancekim.billboard.core.dataimpl.db.CollectionDao
import com.hancekim.billboard.core.dataimpl.db.CollectionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCollectionDatabase(
        @ApplicationContext context: Context,
    ): CollectionDatabase =
        Room.databaseBuilder<CollectionDatabase>(
            context = context,
            name = "billboard_collection.db",
        ).build()

    @Provides
    fun provideCollectionDao(db: CollectionDatabase): CollectionDao =
        db.collectionDao()
}
