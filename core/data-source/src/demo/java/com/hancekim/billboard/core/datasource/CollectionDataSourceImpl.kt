package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.CollectedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 데모 플레이버용 in-memory 구현 — 프로세스 종료 시 데이터 소실
@Singleton
class CollectionDataSourceImpl @Inject constructor() : CollectionDataSource {

    private val cards = MutableStateFlow<List<CollectedCard>>(emptyList())

    override fun observeAll(): Flow<List<CollectedCard>> = cards

    override fun observeByKey(key: String): Flow<CollectedCard?> =
        cards.map { list -> list.find { it.key == key } }

    override suspend fun insert(card: CollectedCard) {
        // 신규 삽입 전용 — 이미 동일 key 가 있으면 IGNORE (prod Dao 의 INSERT IGNORE 와 동등).
        if (cards.value.any { it.key == card.key }) return
        cards.value = cards.value + card
    }

    override suspend fun moveToGroup(key: String, groupId: Long) {
        cards.value = cards.value.map { if (it.key == key) it.copy(groupId = groupId) else it }
    }

    override suspend fun deleteByKey(key: String) {
        cards.value = cards.value.filter { it.key != key }
    }

    override suspend fun deleteAll() {
        cards.value = emptyList()
    }

    override suspend fun exists(key: String): Boolean =
        cards.value.any { it.key == key }

    override suspend fun count(): Int = cards.value.size
}
