package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.CollectionRepository
import com.hancekim.billboard.core.domain.mapper.toDomain
import com.hancekim.billboard.core.domain.model.CollectedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCollectionFlowUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    operator fun invoke(): Flow<List<CollectedCard>> =
        repository.getCollectionFlow().map { list -> list.map { it.toDomain() } }
}
