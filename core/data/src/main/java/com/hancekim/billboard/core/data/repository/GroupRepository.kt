package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getGroupsFlow(): Flow<List<Group>>
    suspend fun getById(id: Long): Group?
    suspend fun existsByName(name: String): Boolean
    suspend fun add(name: String, colorArgb: Int): Long
    suspend fun remove(id: Long)
}
