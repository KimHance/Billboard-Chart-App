package com.hancekim.billboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hancekim.billboard.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainViewModel @Inject constructor(
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val splashState: StateFlow<SplashState> = flow {
        emit(SplashState.Loading)
        delay(3.seconds)
        emit(
            if (networkMonitor.isConnected()) SplashState.Success
            else SplashState.NetworkError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = SplashState.Loading
    )
}

sealed interface SplashState {
    data object Loading : SplashState
    data object Success : SplashState
    data object NetworkError : SplashState
}