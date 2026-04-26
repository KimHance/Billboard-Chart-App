package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.CollectionRepository
import javax.inject.Inject

class RemoveAllFromCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository,
) {
    suspend operator fun invoke() = repository.removeAll()
}
