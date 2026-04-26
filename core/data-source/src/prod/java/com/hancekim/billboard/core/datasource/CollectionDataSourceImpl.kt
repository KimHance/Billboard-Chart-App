package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.datasource.db.CollectedCardEntity
import com.hancekim.billboard.core.datasource.db.CollectionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionDataSourceImpl @Inject constructor(
    private val dao: CollectionDao,
) : CollectionDataSource {

    override fun observeAll(): Flow<List<CollectedCard>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeByKey(key: String): Flow<CollectedCard?> =
        dao.observeByKey(key).map { it?.toModel() }

    override suspend fun insert(card: CollectedCard) {
        dao.insert(card.toEntity())
    }

    override suspend fun deleteByKey(key: String) {
        dao.deleteByKey(key)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun exists(key: String): Boolean = dao.exists(key)

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
