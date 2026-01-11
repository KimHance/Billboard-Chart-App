package com.hancekim.billboard.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.StateDiffLogEffect
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.home.component.PlayWithPager
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
        floatingActionButton = {
            Box(
                Modifier
                    .size(120.dp, 90.dp)
                    .background(Color.Red.copy(.1f))
            )
        },
        snackbarHost = { SnackbarHost(state.snackbarHostState) },
        content = { paddingValues ->
            val containerScrollState = rememberScrollState()
            PlayWithPager(
                modifier = Modifier.padding(paddingValues),
                eventSink = eventSink,
                chartFilter = state.chartFilter,
                chartList = state.chartList,
                expandedIndex = state.expandedIndex,
                playerState = state.playerState,
                scrollState = containerScrollState,
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
                playerState = PlayerState(LocalContext.current)
            ) {

            },
            modifier = Modifier.fillMaxSize()
        )
    }
}