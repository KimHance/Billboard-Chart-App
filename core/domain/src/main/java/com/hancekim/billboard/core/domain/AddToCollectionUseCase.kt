package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.CollectionRepository
import com.hancekim.billboard.core.domain.mapper.toData
import com.hancekim.billboard.core.domain.model.CollectedCard
import javax.inject.Inject

class AddToCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    suspend operator fun invoke(card: CollectedCard): Boolean = repository.add(card.toData())
}
