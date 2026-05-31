package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Immutable
import com.hancekim.billboard.core.domain.model.Group
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.feature.collection.component.NewGroupFormState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class CollectionState(
    val groups: ImmutableList<Group>,
    val currentGroupId: Long,
    val cardsInCurrentGroup: ImmutableList<CollectedCard>,
    val countsByGroupId: ImmutableMap<Long, Int>,
    val nowPlayingKey: String?,
    val newGroupForm: NewGroupFormState?,
    val pendingDeleteGroupId: Long?,
    val eventSink: (CollectionEvent) -> Unit,
) : CircuitUiState

sealed interface CollectionEvent : CircuitUiEvent {
    data object OnBackClick : CollectionEvent
    data class OnSelectCard(val key: String) : CollectionEvent
    data class OnRemoveCard(val key: String) : CollectionEvent
    data object OnInspectClick : CollectionEvent
    data class OnSelectGroup(val id: Long) : CollectionEvent
    data class OnRequestDeleteGroup(val id: Long) : CollectionEvent
    data object OnConfirmDeleteGroup : CollectionEvent
    data object OnCancelDeleteGroup : CollectionEvent
    data object OnNewGroupClick : CollectionEvent
    data object OnCancelNewGroup : CollectionEvent
    data class OnNewGroupNameChange(val name: String) : CollectionEvent
    data class OnNewGroupColorSelect(val colorArgb: Int) : CollectionEvent
    data object OnSubmitNewGroup : CollectionEvent
}
