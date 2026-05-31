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

    // 신규 삽입 전용 — 같은 key 가 이미 있으면 무시 (그룹 이동은 updateGroup 으로 분리).
    // IGNORE 충돌 시 -1L 반환, 정상 삽입 시 새 rowId 반환 — DataSource 계층에서 Boolean 으로 매핑.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsert(entity: CollectedCardEntity): Long

    // 그룹 이동 전용 — collectedAt 등 다른 컬럼은 보존하고 groupId 만 갱신.
    @Query("UPDATE collected_cards SET groupId = :groupId WHERE `key` = :key")
    suspend fun updateGroup(key: String, groupId: Long)

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
