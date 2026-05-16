package com.hancekim.billboard.core.datasource.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.datasource.db.CollectionDao
import com.hancekim.billboard.core.datasource.db.CollectionDatabase
import com.hancekim.billboard.core.datasource.db.GroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DEFAULT_GROUP_COLOR_ARGB: Int = 0xFF00FF85.toInt()

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
        )
            // DB 버전 업그레이드 시 기존 데이터 파기 후 재생성
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override suspend fun onCreate(connection: SQLiteConnection) {
                    val now = System.currentTimeMillis()
                    // Default 그룹 시드 — id=1 고정, 삭제 불가 대상
                    connection.execSQL(
                        "INSERT INTO groups (id, name, nameNormalized, colorArgb, createdAt) " +
                            "VALUES (${Group.DEFAULT_ID}, 'Default', 'default', $DEFAULT_GROUP_COLOR_ARGB, $now)"
                    )
                }
            })
            .build()

    @Provides
    fun provideCollectionDao(db: CollectionDatabase): CollectionDao = db.collectionDao()

    @Provides
    fun provideGroupDao(db: CollectionDatabase): GroupDao = db.groupDao()
}
