package com.hancekim.billboard.core.player

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun YoutubePlayer(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
                .pointerInput(Unit) { }
        ) {
            PlayerControllerButtons(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp),
                isPlay = state.isPlay,
                isMute = state.isMute,
                onPlayStateChanged = { isPlay ->
                    if (isPlay) state.play() else state.pause()
                },
                onMuteStateChanged = { isMute ->
                    if (isMute) state.mute() else state.unMute()
                },
            )
        }
    }
}