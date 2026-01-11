package com.hancekim.billboard.core.player

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import timber.log.Timber

@Stable
class PlayerState(
    context: Context,
) {
    var player: YouTubePlayer? = null

    private val playerView = YouTubePlayerView(context).apply {
        enableAutomaticInitialization = false
        initialize(
            youTubePlayerListener = object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    player = youTubePlayer

                    if (videoId.isNotEmpty()) player?.loadVideo(videoId, 0f)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.PLAYING -> isPlay = true
                        PlayerConstants.PlayerState.PAUSED -> isPlay = false
                        else -> {}
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    Timber.tag("ruben").d("$second")
                }
            }
        )
    }

    fun getPlayerView(): YouTubePlayerView {
        return playerView
    }

    private var videoId: String = ""
    var isPlay by mutableStateOf(true)
    var isMute by mutableStateOf(false)

    var isEnabled by mutableStateOf(true)

    fun load(videoId: String) {
        this.videoId = videoId
    }

    fun play() {
        player?.let { player ->
            player.play()
            isPlay = true
        }
    }

    fun pause() {
        player?.let { player ->
            player.pause()
            isPlay = false
        }
    }

    fun mute() {
        player?.let { player ->
            player.mute()
            isMute = true
        }
    }

    fun unMute() {
        player?.let { player ->
            player.unMute()
            isMute = false
        }
    }

    fun enable() {
        isEnabled = true
    }

    fun disabled() {
        isEnabled = false
    }

    fun release() {
        playerView.release()
        player = null
    }
}