package com.hancekim.billboard.home

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
    val eventSink: (HomeEvent) -> Unit,
) : CircuitUiState

sealed interface HomeEvent : CircuitUiEvent {
    data class OnFilterClick(
        val filter: ChartFilter
    ) : HomeEvent

    data class OnExpandButtonClick(
        val itemIndex: Int,
    ) : HomeEvent
}