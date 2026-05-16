package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupDataSource {
    fun observeAll(): Flow<List<Group>>
    suspend fun getById(id: Long): Group?
    suspend fun existsByName(normalized: String): Boolean
    suspend fun insert(name: String, colorArgb: Int): Long
    suspend fun deleteById(id: Long)
}
