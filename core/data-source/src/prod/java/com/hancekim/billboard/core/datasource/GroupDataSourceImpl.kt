package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.datasource.db.GroupDao
import com.hancekim.billboard.core.datasource.db.GroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupDataSourceImpl @Inject constructor(
    private val dao: GroupDao,
) : GroupDataSource {

    override fun observeAll(): Flow<List<Group>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Group? =
        dao.getById(id)?.toDomain()

    override suspend fun existsByName(normalized: String): Boolean =
        dao.existsByName(normalized)

    override suspend fun insert(name: String, colorArgb: Int): Long {
        val trimmed = name.trim()
        val entity = GroupEntity(
            name = trimmed,
            nameNormalized = trimmed.lowercase(),
            colorArgb = colorArgb,
            createdAt = System.currentTimeMillis(),
        )
        return dao.insert(entity)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    private fun GroupEntity.toDomain() = Group(id, name, colorArgb, createdAt)
}
