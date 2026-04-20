package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor() : CollectionRepository {

    private val cards = MutableStateFlow<List<CollectedCard>>(emptyList())

    override fun getCollectionFlow(): Flow<List<CollectedCard>> = cards

    override suspend fun add(card: CollectedCard) {
        val current = cards.value
        if (current.size >= MAX_SLOTS) return
        if (current.any { it.key == card.key }) return
        cards.value = current + card
    }

    override suspend fun remove(key: String) {
        cards.value = cards.value.filter { it.key != key }
    }

    override suspend fun isCollected(key: String): Boolean =
        cards.value.any { it.key == key }

    override suspend fun count(): Int = cards.value.size

    companion object {
        const val MAX_SLOTS = 9
    }
}
