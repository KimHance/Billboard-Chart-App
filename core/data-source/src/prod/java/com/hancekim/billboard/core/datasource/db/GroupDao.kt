package com.hancekim.billboard.core.datasource.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY id ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GroupEntity?

    @Insert
    suspend fun insert(entity: GroupEntity): Long

    // Default(=1) 안전망 — UseCase 가드 + DAO 가드 이중 방어
    @Query("DELETE FROM groups WHERE id = :id AND id != 1")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM groups WHERE nameNormalized = :normalized)")
    suspend fun existsByName(normalized: String): Boolean
}
