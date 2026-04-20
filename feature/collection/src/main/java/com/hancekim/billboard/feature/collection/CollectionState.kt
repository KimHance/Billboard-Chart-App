package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.data.model.CollectedCard
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class CollectionState(
    val cards: ImmutableList<CollectedCard> = persistentListOf(),
    val eventSink: (CollectionEvent) -> Unit,
) : CircuitUiState

sealed interface CollectionEvent : CircuitUiEvent {
    data class OnCardClick(val cardKey: String) : CollectionEvent
    data object OnBackClick : CollectionEvent
}
