package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.CollectedCard
import kotlinx.coroutines.flow.Flow

interface CollectionDataSource {
    fun observeAll(): Flow<List<CollectedCard>>
    fun observeByKey(key: String): Flow<CollectedCard?>
    suspend fun insert(card: CollectedCard)
    suspend fun deleteByKey(key: String)
    suspend fun exists(key: String): Boolean
    suspend fun count(): Int
}
