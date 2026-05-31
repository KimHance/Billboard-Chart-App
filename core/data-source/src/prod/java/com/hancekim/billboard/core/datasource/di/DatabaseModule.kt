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
                // onCreate 는 DB 가 처음 만들어질 때만 호출되어, 이전 빌드에 DB 가 이미 있는
                // 디바이스에는 시드가 누락된다. onOpen + INSERT OR IGNORE 로 매번 보장.
                override suspend fun onOpen(connection: SQLiteConnection) {
                    val now = System.currentTimeMillis()
                    val name = Group.DEFAULT_NAME
                    val nameNormalized = name.lowercase()
                    connection.execSQL(
                        "INSERT OR IGNORE INTO groups (id, name, nameNormalized, colorArgb, createdAt) " +
                            "VALUES (${Group.DEFAULT_ID}, '$name', '$nameNormalized', ${Group.DEFAULT_COLOR_ARGB}, $now)"
                    )
                }
            })
            .build()

    @Provides
    fun provideCollectionDao(db: CollectionDatabase): CollectionDao = db.collectionDao()

    @Provides
    fun provideGroupDao(db: CollectionDatabase): GroupDao = db.groupDao()
}
