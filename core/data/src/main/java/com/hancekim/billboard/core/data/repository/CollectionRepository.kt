package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.CollectedCard
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getCollectionFlow(): Flow<List<CollectedCard>>
    fun getByKeyFlow(key: String): Flow<CollectedCard?>
    suspend fun add(card: CollectedCard): Boolean
    suspend fun remove(key: String)
    suspend fun isCollected(key: String): Boolean
    suspend fun count(): Int
}
