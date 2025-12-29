package com.hancekim.feature.splash

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable
data class SplashState(
    val networkState: NetworkState,
    val eventSink: (SplashEvent) -> Unit,
) : CircuitUiState

sealed interface SplashEvent : CircuitUiEvent {
    data object OnQuitAlertButtonClick : SplashEvent
    data object GoToMainScreen : SplashEvent
}

enum class NetworkState {
    Checking, Connected, DisConnected
}