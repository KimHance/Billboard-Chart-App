package com.hancekim.billboard.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.StateDiffLogEffect
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.designsystem.componenet.filter.FilterRow
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.designsystem.componenet.list.RankingItem
import com.hancekim.billboard.core.designsystem.componenet.list.toStatus
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSection
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSize
import com.hancekim.billboard.core.player.YoutubePlayer
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
            val contentHorizontalPadding = 16.dp
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                state = state.lazyListState,
                contentPadding = PaddingValues(
                    vertical = 12.dp,
                    horizontal = contentHorizontalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(
                    contentType = "trending"
                ) {
                    TitleSection(
                        title = "Trending Now",
                        size = TitleSize.Medium,
                    )
                    //Todo 뮤비로 대체
                    YoutubePlayer(
                        Modifier
                            .padding(top = 32.dp, bottom = 28.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                    /* TrendingSection(
                         modifier = Modifier
                             .padding(top = 32.dp, bottom = 28.dp),
                         trendingList = state.topTen,
                         onCarouselItemClick = {}
                     )*/
                }
                stickyHeader(
                    contentType = "filter"
                ) {
                    FilterRow(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .ignoreHorizontalContentPadding(contentHorizontalPadding),
                        currentIndex = ChartFilter.entries.indexOf(state.chartFilter),
                    ) {
                        eventSink(HomeEvent.OnFilterClick(it))
                    }
                }
                item(
                    contentType = "filterTitle"
                ) {
                    TitleSection(
                        modifier = Modifier.padding(bottom = 12.dp),
                        title = state.chartFilter.text,
                        size = TitleSize.Large,
                    )
                }
                itemsIndexed(
                    items = state.chartList,
                    key = { _, item -> "${item.rank}/${item.status}/${item.title}/${item.artist}" },
                    contentType = { _, _ -> "chart" }
                ) { index, item ->
                    RankingItem(
                        rank = item.rank,
                        imgUrl = item.image,
                        status = item.status.toStatus(),
                        title = item.title,
                        artist = item.artist,
                        lastWeek = item.lastWeek,
                        peak = item.peakPosition,
                        onWeeks = item.weekOnChart,
                        expand = index == state.expandedIndex,
                        debut = item.debutPosition,
                        debutDate = item.debutDate,
                        peakDate = item.peakDate,
                        onExpandButtonClick = { eventSink(HomeEvent.OnExpandButtonClick(index)) }
                    )
                }
            }
        }
    )
}

@ThemePreviews
@Composable
private fun HomeUiPreview() {
    BillboardTheme {
        HomeUi(
            state = HomeState(
                lazyListState = rememberLazyListState()
            ) {

            },
            modifier = Modifier.fillMaxSize()
        )
    }
}