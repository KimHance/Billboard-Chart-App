package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeCollectionRepository @Inject constructor() : CollectionRepository {

    private val cards = MutableStateFlow<List<CollectedCard>>(emptyList())

    override fun getCollectionFlow(): Flow<List<CollectedCard>> = cards

    override fun getByKeyFlow(key: String): Flow<CollectedCard?> =
        cards.map { list -> list.find { it.key == key } }

    override suspend fun add(card: CollectedCard): Boolean {
        if (cards.value.any { it.key == card.key }) return false
        cards.value = cards.value + card
        return true
    }

    override suspend fun moveToGroup(key: String, groupId: Long) {
        cards.value = cards.value.map { if (it.key == key) it.copy(groupId = groupId) else it }
    }

    override suspend fun remove(key: String) {
        cards.value = cards.value.filter { it.key != key }
    }

    override suspend fun removeAll() {
        cards.value = emptyList()
    }

    suspend fun removeByGroup(groupId: Long) {
        cards.value = cards.value.filterNot { it.groupId == groupId }
    }

    override suspend fun isCollected(key: String): Boolean =
        cards.value.any { it.key == key }

    override suspend fun count(): Int = cards.value.size
}
