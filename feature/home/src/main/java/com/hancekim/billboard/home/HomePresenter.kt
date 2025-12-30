package com.hancekim.billboard.home

import androidx.compose.runtime.Composable
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent

class HomePresenter @AssistedInject constructor(
    @Assisted private val screen: BillboardScreen.Home
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {
        return HomeState
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Home::class, ActivityRetainedComponent::class)
    fun interface HomePresenterFactory {
        fun create(
            screen: BillboardScreen.Home
        ): HomePresenter
    }
}