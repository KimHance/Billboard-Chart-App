package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.RemoveGroupUseCase
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.feature.collection.component.NewGroupFormState
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class CollectionPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getGroupsFlow: GetGroupsFlowUseCase,
    private val getCollectionFlow: GetCollectionFlowUseCase,
    private val addGroupUseCase: AddGroupUseCase,
    private val removeGroupUseCase: RemoveGroupUseCase,
) : Presenter<CollectionState> {

    @Composable
    override fun present(): CollectionState {
        val scope = rememberRetained<CoroutineScope> { MainScope() }
        val groups by produceRetainedState(persistentListOf<Group>()) {
            getGroupsFlow().collect { value = it.toPersistentList() }
        }
        val allCards by produceRetainedState(persistentListOf<CollectedCard>()) {
            getCollectionFlow().collect { value = it.toPersistentList() }
        }
        var currentGroupId by rememberRetained { mutableLongStateOf(Group.DEFAULT_ID) }
        val cardsInCurrentGroup = remember(allCards, currentGroupId) {
            allCards.filter { it.groupId == currentGroupId }.toImmutableList()
        }
        val countsByGroupId = remember(allCards) {
            allCards.groupingBy { it.groupId }.eachCount().toImmutableMap()
        }
        var nowPlayingKey by rememberRetained { mutableStateOf<String?>(null) }
        var sidebarOpen by rememberRetained { mutableStateOf(false) }
        var newGroupForm by rememberRetained { mutableStateOf<NewGroupFormState?>(null) }
        var pendingDeleteGroupId by rememberRetained { mutableStateOf<Long?>(null) }

        LaunchedEffect(currentGroupId, cardsInCurrentGroup.firstOrNull()?.key) {
            if (nowPlayingKey == null || cardsInCurrentGroup.none { it.key == nowPlayingKey }) {
                nowPlayingKey = cardsInCurrentGroup.firstOrNull()?.key
            }
        }

        return CollectionState(
            groups = groups,
            currentGroupId = currentGroupId,
            cardsInCurrentGroup = cardsInCurrentGroup,
            countsByGroupId = countsByGroupId,
            nowPlayingKey = nowPlayingKey,
            sidebarOpen = sidebarOpen,
            newGroupForm = newGroupForm,
            pendingDeleteGroupId = pendingDeleteGroupId,
            eventSink = { event ->
                when (event) {
                    CollectionEvent.OnBackClick -> navigator.pop()
                    is CollectionEvent.OnSelectCard -> {
                        nowPlayingKey = event.key
                    }
                    CollectionEvent.OnInspectClick ->
                        nowPlayingKey?.let { navigator.goTo(BillboardScreen.CardDetail(it)) }
                    is CollectionEvent.OnSidebarToggle -> sidebarOpen = event.open
                    is CollectionEvent.OnSelectGroup -> {
                        currentGroupId = event.id
                        sidebarOpen = false
                    }
                    is CollectionEvent.OnRequestDeleteGroup -> {
                        if (event.id == Group.DEFAULT_ID) {
                            Timber.e("attempted to delete Default group")
                        } else if ((countsByGroupId[event.id] ?: 0) == 0) {
                            scope.launch {
                                runCatching { removeGroupUseCase(event.id) }
                                    .onSuccess {
                                        if (currentGroupId == event.id) currentGroupId = Group.DEFAULT_ID
                                    }
                                    .onFailure { Timber.e(it, "remove group failed") }
                            }
                        } else {
                            pendingDeleteGroupId = event.id
                        }
                    }
                    CollectionEvent.OnConfirmDeleteGroup -> {
                        pendingDeleteGroupId?.let { id ->
                            scope.launch {
                                runCatching { removeGroupUseCase(id) }
                                    .onSuccess {
                                        if (currentGroupId == id) currentGroupId = Group.DEFAULT_ID
                                        pendingDeleteGroupId = null
                                    }
                                    .onFailure { Timber.e(it, "confirm delete failed") }
                            }
                        }
                    }
                    CollectionEvent.OnCancelDeleteGroup -> pendingDeleteGroupId = null
                    CollectionEvent.OnNewGroupClick ->
                        newGroupForm = NewGroupFormState(name = "", colorArgb = null, isDuplicate = false)
                    CollectionEvent.OnCancelNewGroup -> newGroupForm = null
                    is CollectionEvent.OnNewGroupNameChange -> {
                        val normalized = event.name.trim().lowercase()
                        newGroupForm = newGroupForm?.copy(
                            name = event.name,
                            isDuplicate = groups.any { it.name.trim().lowercase() == normalized },
                        )
                    }
                    is CollectionEvent.OnNewGroupColorSelect ->
                        newGroupForm = newGroupForm?.copy(colorArgb = event.colorArgb)
                    CollectionEvent.OnSubmitNewGroup -> {
                        newGroupForm?.let { form ->
                            val color = form.colorArgb ?: return@let
                            if (form.name.trim().isEmpty() || form.isDuplicate) return@let
                            scope.launch {
                                addGroupUseCase(form.name, color)
                                    .onSuccess { newId ->
                                        currentGroupId = newId
                                        newGroupForm = null
                                        sidebarOpen = false
                                    }
                                    .onFailure { Timber.e(it, "addGroup failed") }
                            }
                        }
                    }
                }
            },
        )
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
    fun interface Factory {
        fun create(navigator: Navigator): CollectionPresenter
    }
}
