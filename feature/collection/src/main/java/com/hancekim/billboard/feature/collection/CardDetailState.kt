package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable
data class CardDetailState(
    val card: CollectedCard? = null,
    val eventSink: (CardDetailEvent) -> Unit,
) : CircuitUiState

sealed interface CardDetailEvent : CircuitUiEvent {
    data object OnCloseClick : CardDetailEvent
    data object OnRemoveClick : CardDetailEvent
}
