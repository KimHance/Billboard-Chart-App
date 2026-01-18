package com.hancekim.billboard.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.ChartOverview
import com.hancekim.billboard.core.player.PlayerState
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getHot100UseCase: GetBillboardHot100UseCase,
    private val getArtist100UseCase: GetBillboardArtist100UseCase,
    private val getGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val snackbarHostState = rememberRetained { SnackbarHostState() }
        val lazyListState = rememberRetained { LazyListState() }
        val scrollState = rememberRetained { ScrollState(0) }
        val playerState = rememberRetained { PlayerState(context) }
        var chartFilter by rememberRetained { mutableStateOf(ChartFilter.BillboardHot100) }
        var topTen by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }
        var chartList by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }
        var expandedIndex by rememberRetained { mutableStateOf<Int?>(null) }
        var isExitSnackbarVisible by rememberRetained { mutableStateOf(false) }
        var listOffsetY by rememberRetained { mutableFloatStateOf(0f) }

        val isFilterSticky by rememberRetained {
            derivedStateOf {
                listOffsetY > 0f && scrollState.value >= listOffsetY
            }
        }

        val hot100 by produceRetainedState(
            initialValue = ChartOverview()
        ) {
            runCatching {
                getHot100UseCase()
            }.onSuccess {
                value = it
                topTen = it.topTen.toPersistentList()
                chartList = it.chartList.toPersistentList()
                playerState.load("4MaozyVj8-8")
            }
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
            expandedIndex = null
            when (filter) {
                ChartFilter.BillboardHot100 -> {
                    topTen = hot100.topTen.toPersistentList()
                    chartList = hot100.chartList.toPersistentList()
                }

                ChartFilter.Billboard200 -> {
                    topTen = billboard200.topTen.toPersistentList()
                    chartList = billboard200.chartList.toPersistentList()
                }

                ChartFilter.Global200 -> {
                    topTen = global200.topTen.toPersistentList()
                    chartList = global200.chartList.toPersistentList()
                }

                ChartFilter.Artist100 -> {
                    topTen = artist100.topTen.toPersistentList()
                    chartList = artist100.chartList.toPersistentList()
                }
            }
            lazyListState.requestScrollToItem(0, 0)
        }

        return HomeState(
            topTen = topTen,
            isFilterSticky = isFilterSticky,
            chartList = chartList,
            chartFilter = chartFilter,
            expandedIndex = expandedIndex,
            snackbarHostState = snackbarHostState,
            lazyListState = lazyListState,
            scrollState = scrollState,
            showQuitToast = isExitSnackbarVisible,
            playerState = playerState
        ) { event ->
            when (event) {
                is HomeEvent.OnFilterClick -> onFilterChanged(event.filter)
                is HomeEvent.OnExpandButtonClick -> {
                    expandedIndex = if (expandedIndex == event.itemIndex) {
                        null
                    } else {
                        event.itemIndex
                    }
                }

                is HomeEvent.OnBackPressed -> {
                    if (isExitSnackbarVisible) {
                        navigator.pop(PopResult.QuitAppResult)
                    } else {
                        isExitSnackbarVisible = true
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Press back again to exit",
                                duration = SnackbarDuration.Short,
                            )
                            isExitSnackbarVisible = false
                        }
                    }
                }

                is HomeEvent.OnListPositioned -> listOffsetY = event.y
                HomeEvent.OnSettingIconClick -> navigator.goTo(BillboardScreen.Setting)
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Home::class, ActivityRetainedComponent::class)
    fun interface HomePresenterFactory {
        fun create(
            navigator: Navigator,
        ): HomePresenter
    }
}