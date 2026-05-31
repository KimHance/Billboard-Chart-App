package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import com.hancekim.billboard.core.datasource.GroupDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val dataSource: GroupDataSource,
) : GroupRepository {

    override fun getGroupsFlow(): Flow<List<Group>> = dataSource.observeAll()

    override suspend fun getById(id: Long): Group? = dataSource.getById(id)

    override suspend fun existsByName(name: String): Boolean =
        dataSource.existsByName(name.trim().lowercase())

    override suspend fun add(name: String, colorArgb: Int): Long =
        dataSource.insert(name, colorArgb)

    override suspend fun remove(id: Long) = dataSource.deleteById(id)
}
