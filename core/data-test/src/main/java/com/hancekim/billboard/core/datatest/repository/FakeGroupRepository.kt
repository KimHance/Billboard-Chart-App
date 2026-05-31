package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong

class FakeGroupRepository(
    initial: List<Group> = listOf(
        Group(Group.DEFAULT_ID, Group.DEFAULT_NAME, Group.DEFAULT_COLOR_ARGB, 0L)
    ),
) : GroupRepository {
    private val nextId = AtomicLong(initial.maxOf { it.id } + 1L)
    private val state = MutableStateFlow(initial)

    override fun getGroupsFlow(): Flow<List<Group>> = state
    override suspend fun getById(id: Long): Group? = state.value.find { it.id == id }
    override suspend fun existsByName(name: String): Boolean {
        val normalized = name.trim().lowercase()
        return state.value.any { it.name.trim().lowercase() == normalized }
    }
    override suspend fun add(name: String, colorArgb: Int): Long {
        val id = nextId.getAndIncrement()
        state.value = state.value + Group(id, name.trim(), colorArgb, 0L)
        return id
    }
    override suspend fun remove(id: Long) {
        if (id == Group.DEFAULT_ID) return
        state.value = state.value.filterNot { it.id == id }
    }
}
