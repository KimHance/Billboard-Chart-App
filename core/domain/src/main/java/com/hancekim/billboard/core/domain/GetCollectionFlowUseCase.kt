package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionFlowUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    operator fun invoke(): Flow<List<CollectedCard>> = repository.getCollectionFlow()
}
