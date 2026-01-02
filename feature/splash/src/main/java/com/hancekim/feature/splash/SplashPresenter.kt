package com.hancekim.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.network.NetworkMonitor
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.resetRoot
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class SplashPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val networkMonitor: NetworkMonitor,
) : Presenter<SplashState> {

    @Composable
    override fun present(): SplashState {
        var networkState by rememberRetained { mutableStateOf(NetworkState.Checking) }
        LaunchedImpressionEffect {
            delay(2.seconds)
            networkState =
                if (networkMonitor.isConnected()) NetworkState.Connected else NetworkState.DisConnected
        }

        LaunchedImpressionEffect(networkState) {
            if (networkState == NetworkState.Connected) navigator.resetRoot(BillboardScreen.Home)
        }

        return SplashState(networkState) { navigator.pop(PopResult.QuitAppResult) }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Splash::class, ActivityRetainedComponent::class)
    fun interface SplashPresenterFactory {
        fun create(
            navigator: Navigator,
        ): SplashPresenter
    }
}
