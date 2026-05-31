package com.hancekim.billboard.core.datasource

import android.database.sqlite.SQLiteConstraintException
import com.hancekim.billboard.core.data.exception.DuplicateGroupNameException
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
        return try {
            dao.insert(entity)
        } catch (e: SQLiteConstraintException) {
            // UNIQUE 인덱스 충돌(race) → 도메인-친화 어댑터 예외로 표면화. SQLite 의존을 도메인에 누설하지 않음.
            throw DuplicateGroupNameException(trimmed).apply { initCause(e) }
        }
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    private fun GroupEntity.toDomain() = Group(id, name, colorArgb, createdAt)
}
