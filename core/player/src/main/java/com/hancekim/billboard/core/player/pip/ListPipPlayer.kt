package com.hancekim.billboard.core.player.pip

import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoClose
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.imageloader.BillboardAsyncImage
import com.hancekim.billboard.core.player.PlayerControllerButtons
import com.hancekim.billboard.core.player.PlayerState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ListPipPlayer(
    state: PlayerState,
    modifier: Modifier = Modifier,
) {
    var showController by rememberSaveable { mutableStateOf(false) }

    val dimColor by animateColorAsState(
        targetValue = if (showController || state.isPlayable.not()) Color.Black.copy(.2f) else Color.Transparent
    )

    LaunchedEffect(showController) {
        if (showController) {
            delay(3.seconds)
            showController = false
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth(0.4f)
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val playerView = state.getPlayerView()
                (playerView.parent as? ViewGroup)?.removeView(playerView)
                playerView
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(color = dimColor) }
                .noRippleClickable(
                    onClick = throttledProcess(throttledTimeMillis = 200) {
                        if (state.isPlayable) showController = !showController
                    }
                ),
        ) {

            val closeButton: @Composable () -> Unit = {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .noRippleClickable { state.disabled() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = BillboardIcons.IcoClose,
                        tint = BillboardColor.Green400,
                        contentDescription = null
                    )
                }
            }

            if (state.isPlayable) {
                BillboardAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = state.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                if (showController) {
                    PlayerControllerButtons(
                        modifier = Modifier.align(Alignment.Center),
                        isPlay = state.isPlay,
                        isMute = state.isMute,
                        onPlayStateChanged = { isPlay ->
                            if (isPlay) state.play() else state.pause()
                        },
                        onMuteStateChanged = { isMute ->
                            if (isMute) state.mute() else state.unMute()
                        },
                    )
                    closeButton()
                }
            } else {
                BillboardAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = state.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "This video cannot be played",
                    style = BillboardTheme.typography.titleMd(),
                    color = BillboardColor.Grey300
                )
                closeButton()
            }
        }
    }
}

@Preview
@Composable
private fun ListPipPlayerPreview() {
    BillboardTheme {
        val context = LocalContext.current
        val state = remember { PlayerState(context) }
        ListPipPlayer(
            modifier = Modifier.width(320.dp),
            state = state,
        )
    }
}

@Preview
@Composable
private fun ListPipPlayerNotPlayablePreview() {
    BillboardTheme {
        val context = LocalContext.current
        val state = remember {
            PlayerState(context).apply {
                changePlayable(false)
            }
        }
        ListPipPlayer(
            modifier = Modifier.width(320.dp),
            state = state,
        )
    }
}