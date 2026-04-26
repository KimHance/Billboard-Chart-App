package com.hancekim.billboard.core.datasource.db

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [CollectedCardEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}
