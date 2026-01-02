package com.hancekim.billboard.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.carousel.Carousel
import com.hancekim.billboard.core.designsystem.componenet.carousel.TopCarousel
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSection
import com.hancekim.billboard.core.designsystem.componenet.title.TitleSize
import com.hancekim.billboard.core.domain.model.Chart
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun TrendingSection(
    trendingList: ImmutableList<Chart>,
    modifier: Modifier = Modifier,
    onCarouselItemClick: (Carousel) -> Unit,
) {

    val carouseList = remember(trendingList) {
        trendingList.map {
            Carousel(
                imgUrl = it.image,
                title = it.title,
                artist = it.artist
            )
        }.toPersistentList()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        TopCarousel(
            carouselList = carouseList,
            onItemClick = onCarouselItemClick
        )
    }
}

@ThemePreviews
@Composable
private fun TrendingSectionPreview() {
    BillboardTheme {
        val trendingList = buildList {
            repeat(10) { count ->
                add(
                    Chart(
                        image = "",
                        title = "Title $count",
                        artist = "Artist $count"
                    )
                )
            }
        }.toPersistentList()

        TrendingSection(
            trendingList = trendingList
        ) {

        }
    }
}