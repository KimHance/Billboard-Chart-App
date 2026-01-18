package com.hancekim.billboard.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.StateDiffLogEffect
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.home.component.PlayerWithPager
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(BillboardScreen.Home::class, ActivityRetainedComponent::class)
@Composable
fun HomeUi(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val eventSink = state.eventSink

    BackHandler {
        eventSink(HomeEvent.OnBackPressed)
    }

    StateDiffLogEffect(
        state = state,
        enabled = true,
        tag = "home"
    )

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = "BILLBOARD"
            ) { eventSink(HomeEvent.OnSettingIconClick) }
        },
        snackbarHost = { SnackbarHost(state.snackbarHostState) },
        content = { paddingValues ->
            PlayerWithPager(
                modifier = Modifier.padding(paddingValues),
                eventSink = eventSink,
                chartFilter = state.chartFilter,
                chartList = state.chartList,
                expandedIndex = state.expandedIndex,
                playerState = state.playerState,
                scrollState = state.scrollState,
                lazyListState = state.lazyListState,
            )
        }
    )
}

@ThemePreviews
@Composable
private fun HomeUiPreview() {
    BillboardTheme {
        HomeUi(
            state = HomeState(
                lazyListState = rememberLazyListState(),
                playerState = PlayerState(LocalContext.current),
            ) {

            },
            modifier = Modifier.fillMaxSize()
        )
    }
}