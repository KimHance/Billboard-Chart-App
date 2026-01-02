package com.hancekim.feature.splash

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable
data class SplashState(
    val networkState: NetworkState,
    val quitEventSink: (OnQuitAlertButtonClick) -> Unit,
) : CircuitUiState

data object OnQuitAlertButtonClick : CircuitUiEvent

enum class NetworkState {
    Checking, Connected, DisConnected
}