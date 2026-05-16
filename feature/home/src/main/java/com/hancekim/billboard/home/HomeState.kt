package com.hancekim.billboard.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.YoutubeVideoDetail
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.core.player.pip.PipState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.home.component.NewGroupFormState
import com.hancekim.billboard.home.component.OverlayCollectState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Stable
data class HomeState(
    val playerState: PlayerState,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val scrollState: ScrollState = ScrollState(0),
    val lazyListState: LazyListState = LazyListState(),
    val pipState: PipState = PipState(),
    val showQuitToast: Boolean = false,
    val expandedIndex: Int? = null,
    val isPipMode: Boolean = false,
    val currentVideo: YoutubeVideoDetail? = null,
    val topTen: ImmutableList<Chart> = persistentListOf(),
    val chartList: ImmutableList<Chart> = persistentListOf(),
    val chartFilter: ChartFilter = ChartFilter.BillboardHot100,
    val showCollectOverlay: Boolean = false,
    val overlayChart: Chart? = null,
    val isOverlayItemCollected: Boolean = false,
    val collectionCount: Int = 0,
    val groups: ImmutableMap<Long, Group> = persistentMapOf(),
    val selectedGroupIdInOverlay: Long = Group.DEFAULT_ID,
    val collectedGroupColorByKey: ImmutableMap<String, Int> = persistentMapOf(),
    val overlayState: OverlayCollectState = OverlayCollectState.Uncollected,
    val newGroupFormInOverlay: NewGroupFormState? = null,
    val eventSink: (HomeEvent) -> Unit,
) : CircuitUiState

sealed interface HomeEvent : CircuitUiEvent {
    data class OnFilterClick(
        val filter: ChartFilter
    ) : HomeEvent

    data class OnExpandButtonClick(
        val itemIndex: Int,
    ) : HomeEvent

    data object OnBackPressed : HomeEvent

    data object OnSettingIconClick : HomeEvent

    data class OnListPositioned(
        val y: Float,
    ) : HomeEvent

    data class OnItemClick(
        val item: Chart
    ) : HomeEvent

    data class OnLongPressItem(val item: Chart) : HomeEvent
    data object OnCollectionIconClick : HomeEvent
    data object OnCollectItem : HomeEvent
    data object OnRemoveItem : HomeEvent
    data object OnDismissOverlay : HomeEvent
    data class OnSelectGroupInOverlay(val id: Long) : HomeEvent
    data object OnCreateNewGroupClickInOverlay : HomeEvent
    data class OnNewGroupNameChangeInOverlay(val name: String) : HomeEvent
    data class OnNewGroupHexChangeInOverlay(val hex: String) : HomeEvent
    data object OnSubmitNewGroupInOverlay : HomeEvent
    data object OnCancelNewGroupInOverlay : HomeEvent
    data object OnCommitOverlay : HomeEvent
}