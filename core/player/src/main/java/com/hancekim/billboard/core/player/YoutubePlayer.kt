package com.hancekim.billboard.core.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class YoutubePlayer {
}

@Composable
fun YoutubePlayer(modifier: Modifier) {

    var youTubePlayerInstance: YouTubePlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                YouTubePlayerView(it).apply {
                    enableAutomaticInitialization = false
                    lifecycleOwner.lifecycle.addObserver(this)
                    val options = IFramePlayerOptions.Builder(it)
                        .controls(0)  // 컨트롤 숨김
                        .build()

                    initialize(
                        youTubePlayerListener = object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayerInstance = youTubePlayer
                                youTubePlayer.loadVideo("qg-fQD_EUbs", 0f)
                                isPlaying = true
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState
                            ) {
                                isPlaying = state == PlayerConstants.PlayerState.PLAYING
                                if (state == PlayerConstants.PlayerState.ENDED) {
                                    youTubePlayer.play()
                                }
                            }
                        },
                        playerOptions = options
                    )

                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { /* 아무것도 안 함 */ }
                }
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 재생/정지 버튼

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Blue, CircleShape)
                    .noRippleClickable {
                        youTubePlayerInstance?.let {
                            if (isPlaying) it.pause() else it.play()
                        }
                    }
            )

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Red, CircleShape)
                    .noRippleClickable {
                        youTubePlayerInstance?.let {
                            if (isMuted) {
                                it.unMute()
                            } else {
                                it.mute()
                            }
                            isMuted = !isMuted
                        }
                    }
            )

        }
    }
}