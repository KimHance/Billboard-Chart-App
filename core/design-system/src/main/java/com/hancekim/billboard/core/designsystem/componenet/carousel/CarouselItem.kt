package com.hancekim.billboard.core.designsystem.componenet.carousel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.Album
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage

data class Carousel(
    val imgUrl: String,
    val title: String,
    val artist: String,
)

@Composable
fun CarouselItem(
    item: Carousel,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .aspectRatio(24f / 14f)
            .background(colorScheme.bgCard),
        contentAlignment = Alignment.Center
    ) {
        BillboardAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = item.imgUrl,
            contentDescription = null,
            placeholder = {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight(.35f)
                        .aspectRatio(1f / 1f),
                    imageVector = BillboardIcons.Album,
                    tint = colorScheme.textTertiary,
                    contentDescription = null,
                )
            },
            contentScale = ContentScale.FillBounds
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, BillboardColor.Black),
                startY = size.height / 2,
                endY = size.height
            )
            drawRect(
                brush = gradientBrush,
                topLeft = Offset(x = 0f, y = size.height / 2),
                size = Size(
                    width = size.width,
                    height = size.height / 2
                )
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                color = colorScheme.textOnDark,
                style = BillboardTheme.typography.headingMd()
            )
            Text(
                text = item.artist,
                color = colorScheme.textOnDarkMuted,
                style = BillboardTheme.typography.bodySm()
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CarouselItemPreview() {
    BillboardTheme {
        CarouselItem(
            Carousel(
                imgUrl = "https://i.ytimg.com/vi/OEpSkXIDo_4/maxresdefault.jpg",
                title = "Song Name",
                artist = "Artist",
            )
        )
    }
}