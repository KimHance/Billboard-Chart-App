package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupDataSourceImpl @Inject constructor() : GroupDataSource {

    // Starred(=1) 는 고정 시드 — 이후 id 는 2부터 증가
    private val nextId = AtomicLong(2L)
    private val state = MutableStateFlow(
        listOf(
            Group(
                id = Group.DEFAULT_ID,
                name = "Starred",
                colorArgb = 0xFF00FF85.toInt(),
                createdAt = System.currentTimeMillis(),
            ),
        ),
    )

    override fun observeAll(): Flow<List<Group>> = state

    override suspend fun getById(id: Long): Group? = state.value.find { it.id == id }

    override suspend fun existsByName(normalized: String): Boolean =
        state.value.any { it.name.trim().lowercase() == normalized }

    override suspend fun insert(name: String, colorArgb: Int): Long {
        val id = nextId.getAndIncrement()
        state.value = state.value + Group(id, name.trim(), colorArgb, System.currentTimeMillis())
        return id
    }

    override suspend fun deleteById(id: Long) {
        // Default 그룹은 삭제 불가
        if (id == Group.DEFAULT_ID) return
        state.value = state.value.filterNot { it.id == id }
    }
}
