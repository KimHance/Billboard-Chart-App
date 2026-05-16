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

    @Query("SELECT * FROM collected_cards WHERE groupId = :groupId ORDER BY collectedAt DESC")
    fun observeByGroup(groupId: Long): Flow<List<CollectedCardEntity>>

    // 같은 key 가 들어오면 REPLACE — 한 곡이 다른 그룹으로 이동하는 의미
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CollectedCardEntity)

    @Query("DELETE FROM collected_cards WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM collected_cards WHERE groupId = :groupId")
    suspend fun deleteByGroup(groupId: Long)

    @Query("DELETE FROM collected_cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM collected_cards WHERE `key` = :key LIMIT 1")
    fun observeByKey(key: String): Flow<CollectedCardEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM collected_cards WHERE `key` = :key)")
    suspend fun exists(key: String): Boolean

    @Query("SELECT COUNT(*) FROM collected_cards")
    suspend fun count(): Int
}
