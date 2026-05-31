package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.CollectedCard
import kotlinx.coroutines.flow.Flow

interface CollectionDataSource {
    fun observeAll(): Flow<List<CollectedCard>>
    fun observeByKey(key: String): Flow<CollectedCard?>
    // true = 신규 삽입, false = 같은 key 가 이미 존재해 삽입 안 됨 (IGNORE 시맨틱).
    suspend fun insert(card: CollectedCard): Boolean
    suspend fun moveToGroup(key: String, groupId: Long)
    suspend fun deleteByKey(key: String)
    suspend fun deleteAll()
    suspend fun exists(key: String): Boolean
    suspend fun count(): Int
}
