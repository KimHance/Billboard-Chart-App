package com.hancekim.billboard.core.datasource.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collected_cards ORDER BY collectedAt DESC")
    fun observeAll(): Flow<List<CollectedCardEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: CollectedCardEntity)

    @Query("DELETE FROM collected_cards WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM collected_cards WHERE `key` = :key LIMIT 1")
    fun observeByKey(key: String): Flow<CollectedCardEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM collected_cards WHERE `key` = :key)")
    suspend fun exists(key: String): Boolean

    @Query("SELECT COUNT(*) FROM collected_cards")
    suspend fun count(): Int
}
