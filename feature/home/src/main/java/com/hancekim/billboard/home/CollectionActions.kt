package com.hancekim.billboard.home

import com.hancekim.billboard.core.domain.AddToCollectionUseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.IsCollectedUseCase
import com.hancekim.billboard.core.domain.MoveCollectedCardUseCase
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.hancekim.billboard.core.domain.model.CollectedCard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CollectionActions @Inject constructor(
    private val addToCollectionUseCase: AddToCollectionUseCase,
    private val removeFromCollectionUseCase: RemoveFromCollectionUseCase,
    private val moveCollectedCardUseCase: MoveCollectedCardUseCase,
    private val getCollectionFlowUseCase: GetCollectionFlowUseCase,
    private val isCollectedUseCase: IsCollectedUseCase,
) {
    fun observeAll(): Flow<List<CollectedCard>> = getCollectionFlowUseCase()

    suspend fun add(card: CollectedCard): Boolean = addToCollectionUseCase(card)

    suspend fun move(key: String, groupId: Long) = moveCollectedCardUseCase(key, groupId)

    suspend fun remove(key: String) = removeFromCollectionUseCase(key)

    suspend fun isCollected(key: String): Boolean = isCollectedUseCase(key)
}
