package com.hancekim.billboard.home

import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.domain.model.Chart
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class HomeState(
    val date: String = "",
    val topTen: ImmutableList<Chart> = persistentListOf(),
    val chartList: ImmutableList<Chart> = persistentListOf(),
) : CircuitUiState