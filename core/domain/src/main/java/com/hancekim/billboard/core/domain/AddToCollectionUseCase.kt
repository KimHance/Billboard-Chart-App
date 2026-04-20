package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.repository.CollectionRepository
import javax.inject.Inject

class AddToCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    suspend operator fun invoke(card: CollectedCard) = repository.add(card)
}
