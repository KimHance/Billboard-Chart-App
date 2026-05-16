package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.player.PlayerControllerButtons
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.core.player.YoutubePlayer

@Composable
fun NowPlayingPlayer(
    playerState: PlayerState?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center,
    ) {
        if (playerState != null) {
            YoutubePlayer(
                state = playerState,
                modifier = Modifier.fillMaxWidth(),
            )
            PlayerControllerButtons(
                isPlay = playerState.isPlay,
                isMute = playerState.isMute,
                onPlayStateChanged = { isPlay ->
                    if (isPlay) playerState.play() else playerState.pause()
                },
                onMuteStateChanged = { isMute ->
                    if (isMute) playerState.mute() else playerState.unMute()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "NOTHING PLAYING",
                    style = BillboardTheme.typography.labelMd(),
                    color = BillboardTheme.colorScheme.textSecondary,
                )
            }
        }
    }
}
