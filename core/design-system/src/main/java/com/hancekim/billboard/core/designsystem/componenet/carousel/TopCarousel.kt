package com.hancekim.billboard.core.designsystem.componenet.carousel

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.absoluteValue
import kotlin.math.sqrt

@Composable
fun TopCarousel(
    carouselList: ImmutableList<Carousel>,
    modifier: Modifier = Modifier,
    onItemClick: (Carousel) -> Unit,
) {
    val pagerState = rememberPagerState { carouselList.size }
    var offsetY by remember { mutableFloatStateOf(0f) }

    HorizontalPager(
        modifier = modifier
            .fillMaxWidth()
            .pointerInteropFilter {
                offsetY = it.y
                false
            }
            .clip(RoundedCornerShape(16.dp)),
        state = pagerState,
    ) { page ->
        CarouselItem(
            modifier = Modifier
                .noRippleClickable(onClick = throttledProcess {
                    onItemClick(carouselList[page])
                })
                .graphicsLayer {
                    // MAKE THE PAGE NOT MOVE
                    val pageOffset = pagerState.offsetForPage(page)
                    translationX = size.width * pageOffset

                    // ADD THE CIRCULAR CLIPPING
                    val endOffset = pagerState.endOffsetForPage(page)

                    shadowElevation = 20f
                    shape = CirclePath(
                        progress = 1f - endOffset.absoluteValue,
                        origin = Offset(
                            size.width,
                            offsetY,
                        )
                    )
                    clip = true

                    // PARALLAX SCALING
                    val absoluteOffset = pagerState.offsetForPage(page).absoluteValue
                    val scale = 1f + (absoluteOffset.absoluteValue * .4f)

                    scaleX = scale
                    scaleY = scale

                    // FADE AWAY
                    val startOffset = pagerState.startOffsetForPage(page)
                    alpha = (2f - startOffset) / 2f

                    if (page == 0) {
                        Log.d(
                            "hi", "" +
                                    "pageOffset: $pageOffset, startOffset: $startOffset," +
                                    "endOffset: ${endOffset.absoluteValue}, absoluteOffset: $absoluteOffset, " +
                                    "scale: $scale"
                        )
                    }

                },
            item = carouselList[page]
        )
    }
}


fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction

fun PagerState.startOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtLeast(0f)
}

fun PagerState.endOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtMost(0f)
}

class CirclePath(private val progress: Float, private val origin: Offset = Offset(0f, 0f)) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {

        val center = Offset(
            x = size.center.x - ((size.center.x - origin.x) * (1f - progress)),
            y = size.center.y - ((size.center.y - origin.y) * (1f - progress)),
        )
        val radius = (sqrt(
            size.height * size.height + size.width * size.width
        ) * .5f) * progress

        return Outline.Generic(Path().apply {
            addOval(
                Rect(
                    center = center,
                    radius = radius,
                )
            )
        })
    }
}

@ThemePreviews
@Composable
private fun TopCarouselPreview() {
    BillboardTheme {
        val list = (0..9).map {
            Carousel(
                imgUrl = "https://static.cdn.kmong.com/gigs/I3ijI1729673110.jpg",
                title = "Title $it",
                artist = "Artist $it"
            )
        }.toPersistentList()
        TopCarousel(list) { }
    }
}