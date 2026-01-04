package com.hancekim.billboard.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.model.Chart
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class HomeState(
    val date: String = "",
    val topTen: ImmutableList<Chart> = persistentListOf(),
    val chartList: ImmutableList<Chart> = persistentListOf(),
    val chartFilter: ChartFilter = ChartFilter.BillboardHot100,
    val expandedIndex: Int? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val showQuitToast: Boolean = false,
    val lazyListState: LazyListState,
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
}