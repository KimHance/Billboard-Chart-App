package com.hancekim.billboard.home.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.designsystem.componenet.filter.FilterRow
import com.hancekim.billboard.core.designsystem.componenet.list.RankingItem
import com.hancekim.billboard.core.designsystem.componenet.list.toStatus
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSection
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSize
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.core.player.YoutubePlayer
import com.hancekim.billboard.home.HomeEvent
import com.hancekim.billboard.home.ignoreHorizontalContentPadding
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PlayWithPager(
    chartFilter: ChartFilter,
    chartList: ImmutableList<Chart>,
    expandedIndex: Int?,
    playerState: PlayerState,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    eventSink: (HomeEvent) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    state = scrollState,
                    overscrollEffect = rememberOverscrollEffect()?.withoutVisualEffect(),
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                TitleSection(
                    title = "Trending Now",
                    size = TitleSize.Medium,
                )
                YoutubePlayer(
                    state = playerState,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }
            Column(modifier = Modifier.height(this@BoxWithConstraints.maxHeight)) {
                FilterRow(
                    modifier = Modifier
                        .ignoreHorizontalContentPadding(ContentHorizontalPadding),
                    currentIndex = ChartFilter.entries.indexOf(chartFilter),
                ) {
                    eventSink(HomeEvent.OnFilterClick(it))
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(
                            remember {
                                object : NestedScrollConnection {
                                    override fun onPreScroll(
                                        available: Offset,
                                        source: NestedScrollSource,
                                    ): Offset = if (available.y > 0) {
                                        Offset.Zero
                                    } else {
                                        Offset(
                                            x = 0f,
                                            y = -scrollState.dispatchRawDelta(-available.y),
                                        )
                                    }
                                }
                            },
                        ),
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        vertical = 12.dp,
                        horizontal = ContentHorizontalPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(
                        contentType = "filterTitle"
                    ) {
                        TitleSection(
                            title = chartFilter.text,
                            size = TitleSize.Large,
                        )
                    }
                    itemsIndexed(
                        items = chartList,
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
                            expand = index == expandedIndex,
                            debut = item.debutPosition,
                            debutDate = item.debutDate,
                            peakDate = item.peakDate,
                            onExpandButtonClick = { eventSink(HomeEvent.OnExpandButtonClick(index)) }
                        )
                    }
                }
            }
        }
    }
}

private val ContentHorizontalPadding = 16.dp