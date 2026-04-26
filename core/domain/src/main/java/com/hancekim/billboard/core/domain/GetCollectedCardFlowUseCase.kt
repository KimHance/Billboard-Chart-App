package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.CollectionRepository
import com.hancekim.billboard.core.domain.mapper.toDomain
import com.hancekim.billboard.core.domain.model.CollectedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCollectedCardFlowUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    operator fun invoke(key: String): Flow<CollectedCard?> =
        repository.getByKeyFlow(key).map { it?.toDomain() }
}
