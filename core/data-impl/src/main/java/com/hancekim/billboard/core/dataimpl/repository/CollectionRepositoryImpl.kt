package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import com.hancekim.billboard.core.dataimpl.db.CollectedCardEntity
import com.hancekim.billboard.core.dataimpl.db.CollectionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val dao: CollectionDao,
) : CollectionRepository {

    override fun getCollectionFlow(): Flow<List<CollectedCard>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    override fun getByKeyFlow(key: String): Flow<CollectedCard?> =
        dao.observeByKey(key).map { it?.toModel() }

    override suspend fun add(card: CollectedCard): Boolean {
        if (dao.count() >= CollectedCard.MAX_SLOTS) return false
        dao.insert(card.toEntity())
        return true
    }

    override suspend fun remove(key: String) {
        dao.deleteByKey(key)
    }

    override suspend fun isCollected(key: String): Boolean =
        dao.exists(key)

    override suspend fun count(): Int = dao.count()
}

private fun CollectedCardEntity.toModel() = CollectedCard(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
)

private fun CollectedCard.toEntity() = CollectedCardEntity(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
)
