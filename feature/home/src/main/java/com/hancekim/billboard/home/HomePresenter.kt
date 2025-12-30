package com.hancekim.billboard.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.ChartOverview
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

class HomePresenter @AssistedInject constructor(
    private val getHot100UseCase: GetBillboardHot100UseCase,
    private val getArtist100UseCase: GetBillboardArtist100UseCase,
    private val getGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {

        var chartFilter by rememberRetained { mutableStateOf<ChartFilter>(ChartFilter.BillboardHot100) }
        var date by rememberRetained { mutableStateOf("") }
        var topTen by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }
        var chartList by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }

        val hot100 by produceRetainedState(
            initialValue = ChartOverview()
        ) {
            runCatching { getHot100UseCase() }.onSuccess { value = it }
        }

        val artist100 by produceRetainedState(
            initialValue = ChartOverview()
        ) {
            runCatching { getArtist100UseCase() }.onSuccess { value = it }
        }

        val global200 by produceRetainedState(
            initialValue = ChartOverview()
        ) {
            runCatching { getGlobal200UseCase() }.onSuccess { value = it }
        }

        val billboard200 by produceRetainedState(
            initialValue = ChartOverview()
        ) {
            runCatching { getBillboard200UseCase() }.onSuccess { value = it }
        }

        val onFilterChanged: (ChartFilter) -> Unit = { filter ->
            chartFilter = filter
        }

        return HomeState(
            date = hot100.date,
            topTen = hot100.topTen.toPersistentList(),
            chartList = hot100.chartList.toPersistentList(),
            chartFilter = chartFilter,
        ) { event ->
            when (event) {
                is HomeEvent.OnFilterClick -> onFilterChanged(event.filter)
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Home::class, ActivityRetainedComponent::class)
    fun interface HomePresenterFactory {
        fun create(): HomePresenter
    }
}