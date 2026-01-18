package com.hancekim.billboard.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.player.PlayerState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class HomeState(
    val showQuitToast: Boolean = false,
    val expandedIndex: Int? = null,
    val isPipMode: Boolean = false,
    val topTen: ImmutableList<Chart> = persistentListOf(),
    val chartList: ImmutableList<Chart> = persistentListOf(),
    val chartFilter: ChartFilter = ChartFilter.BillboardHot100,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val scrollState: ScrollState = ScrollState(0),
    val lazyListState: LazyListState = LazyListState(),
    val playerState: PlayerState,
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
}