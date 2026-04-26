package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import com.hancekim.billboard.core.datasource.CollectionDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val dataSource: CollectionDataSource,
) : CollectionRepository {

    override fun getCollectionFlow(): Flow<List<CollectedCard>> =
        dataSource.observeAll()

    override fun getByKeyFlow(key: String): Flow<CollectedCard?> =
        dataSource.observeByKey(key)

    override suspend fun add(card: CollectedCard): Boolean {
        if (dataSource.count() >= CollectedCard.MAX_SLOTS) return false
        dataSource.insert(card)
        return true
    }

    override suspend fun remove(key: String) {
        dataSource.deleteByKey(key)
    }

    override suspend fun isCollected(key: String): Boolean =
        dataSource.exists(key)

    override suspend fun count(): Int = dataSource.count()
}
