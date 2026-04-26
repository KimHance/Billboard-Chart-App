package com.hancekim.billboard.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.hancekim.billboard.core.domain.GetYoutubeVideoDetailUseCase
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.core.domain.model.ChartOverview
import com.hancekim.billboard.core.domain.model.YoutubeVideoDetail
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.core.player.pip.PipState
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
    private val getYoutubeVideoDetailUseCase: GetYoutubeVideoDetailUseCase,
    private val collectionActions: CollectionActions,
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        // Ui StateHolder
        val snackbarHostState = rememberRetained { SnackbarHostState() }
        val lazyListState = rememberRetained { LazyListState() }
        val scrollState = rememberRetained { ScrollState(0) }
        val playerState = rememberRetained { PlayerState(context) }
        val pipState = rememberRetained { PipState(onDismiss = { playerState.disabled() }) }

        var chartFilter by rememberRetained { mutableStateOf(ChartFilter.BillboardHot100) }
        var topTen by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }
        var chartList by rememberRetained { mutableStateOf(persistentListOf<Chart>()) }
        var expandedIndex by rememberRetained { mutableStateOf<Int?>(null) }
        var currentVideo by rememberRetained { mutableStateOf<YoutubeVideoDetail?>(null) }
        var showCollectOverlay by rememberRetained { mutableStateOf(false) }
        var overlayChart by rememberRetained { mutableStateOf<Chart?>(null) }
        var isOverlayItemCollected by rememberRetained { mutableStateOf(false) }

        val collectionCount by produceRetainedState(0) {
            collectionActions.observeAll().collect { value = it.size }
        }

        var exitSnackbarVisible by rememberRetained { mutableStateOf(false) }
        var listOffsetY by rememberRetained { mutableFloatStateOf(0f) }
        val isFilterSticky by rememberRetained {
            derivedStateOf {
                listOffsetY > 0f && scrollState.value >= listOffsetY
            }
        }

        val loadVideo: (Chart) -> Unit = { chart ->
            if (chart.title.isNotEmpty()) {
                scope.launch {
                    runCatching {
                        getYoutubeVideoDetailUseCase(chart.title, chart.artist)
                    }.onSuccess { detail ->
                        if (currentVideo == detail && playerState.isEnabled) return@onSuccess
                        with(playerState) {
                            changePlayable(detail.isPlayable)
                            if (detail.isPlayable) {
                                currentVideo = detail
                                load(
                                    videoId = detail.videoId,
                                    thumbnailUrl = detail.thumbnailUrl,
                                )
                                play()
                            } else {
                                pause()
                            }
                        }
                    }.onFailure { e ->
                        snackbarHostState.showSnackbar(e.message ?: "Unknown Error")
                    }
                }
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
                it.chartList.firstOrNull()?.let { chart ->
                    loadVideo(chart)
                }
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

        LaunchedEffect(scope) { pipState.setScope(scope) }

        return HomeState(
            topTen = topTen,
            isPipMode = isFilterSticky,
            chartList = chartList,
            chartFilter = chartFilter,
            currentVideo = currentVideo,
            expandedIndex = expandedIndex,
            snackbarHostState = snackbarHostState,
            lazyListState = lazyListState,
            scrollState = scrollState,
            showQuitToast = exitSnackbarVisible,
            playerState = playerState,
            pipState = pipState,
            showCollectOverlay = showCollectOverlay,
            overlayChart = overlayChart,
            isOverlayItemCollected = isOverlayItemCollected,
            collectionCount = collectionCount,
            isCollectionFull = collectionCount >= CollectedCard.MAX_SLOTS,
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
                    if (exitSnackbarVisible) {
                        navigator.pop(PopResult.QuitAppResult)
                    } else {
                        exitSnackbarVisible = true
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Press back again to exit",
                                duration = SnackbarDuration.Short,
                            )
                            exitSnackbarVisible = false
                        }
                    }
                }

                is HomeEvent.OnListPositioned -> listOffsetY = event.y
                HomeEvent.OnSettingIconClick -> navigator.goTo(BillboardScreen.Setting)
                is HomeEvent.OnItemClick -> loadVideo(event.item)
                is HomeEvent.OnLongPressItem -> {
                    overlayChart = event.item
                    scope.launch {
                        runCatching {
                            collectionActions.isCollected(CollectedCard.createKey(event.item.title, event.item.artist))
                        }.onSuccess { collected ->
                            isOverlayItemCollected = collected
                            showCollectOverlay = true
                        }.onFailure {
                            snackbarHostState.showSnackbar("Failed to check collection")
                        }
                    }
                }
                HomeEvent.OnCollectionIconClick -> navigator.goTo(BillboardScreen.Collection)
                HomeEvent.OnCollectItem -> {
                    overlayChart?.let { chart ->
                        scope.launch {
                            runCatching {
                                collectionActions.add(
                                    CollectedCard(
                                        key = CollectedCard.createKey(chart.title, chart.artist),
                                        title = chart.title,
                                        artist = chart.artist,
                                        albumArtUrl = chart.image,
                                        collectedAt = System.currentTimeMillis(),
                                        lastWeek = chart.lastWeek,
                                        peakPosition = chart.peakPosition,
                                        weeksOnChart = chart.weekOnChart,
                                    ),
                                )
                            }.onSuccess { added ->
                                if (added) {
                                    isOverlayItemCollected = true
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "Collection is full (max ${CollectedCard.MAX_SLOTS})",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            }.onFailure {
                                snackbarHostState.showSnackbar("Failed to save card")
                            }
                        }
                    }
                }
                HomeEvent.OnRemoveItem -> {
                    overlayChart?.let { chart ->
                        scope.launch {
                            runCatching {
                                collectionActions.remove(CollectedCard.createKey(chart.title, chart.artist))
                            }.onSuccess {
                                isOverlayItemCollected = false
                            }.onFailure {
                                snackbarHostState.showSnackbar("Failed to remove card")
                            }
                        }
                    }
                }
                HomeEvent.OnDismissOverlay -> showCollectOverlay = false
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