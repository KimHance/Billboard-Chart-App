package com.hancekim.billboard.core.player

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoPause
import com.hancekim.billboard.core.designfoundation.icon.IcoPlay
import com.hancekim.billboard.core.designfoundation.icon.IcoSoundOff
import com.hancekim.billboard.core.designfoundation.icon.IcoSoundOn
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun PlayerControllerButtons(
    isPlay: Boolean,
    isMute: Boolean,
    onPlayStateChanged: (Boolean) -> Unit,
    onMuteStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControllerButton(
            valueOnImageVector = BillboardIcons.IcoPlay,
            valueOffImageVector = BillboardIcons.IcoPause,
            value = isPlay,
            onValueChanged = onPlayStateChanged
        )
        ControllerButton(
            valueOnImageVector = BillboardIcons.IcoSoundOff,
            valueOffImageVector = BillboardIcons.IcoSoundOn,
            value = isMute,
            onValueChanged = onMuteStateChanged
        )
    }
}

@Composable
fun ControllerButton(
    valueOnImageVector: ImageVector,
    valueOffImageVector: ImageVector,
    value: Boolean,
    modifier: Modifier = Modifier,
    onValueChanged: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .semantics {
                contentDescription = "Player Button"
            }
            .toggleable(
                value = value,
                interactionSource = null,
                indication = null,
                enabled = true,
                role = Role.Button,
                onValueChange = onValueChanged
            )
            .background(color = Color.Black.copy(.6f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = value,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) { isValueOn ->
            Icon(
                imageVector = if (isValueOn) valueOnImageVector else valueOffImageVector,
                contentDescription = null,
                tint = BillboardColor.Green400,
            )
        }
    }
}

@Preview
@Composable
fun PlayerControllerButtonsPreview() {
    BillboardTheme {
        PlayerControllerButtons(
            isPlay = false,
            isMute = false,
            onPlayStateChanged = {},
            onMuteStateChanged = {}
        )
    }
}

@Preview
@Composable
fun ControllerButtonPreview() {
    BillboardTheme {
        ControllerButton(
            valueOnImageVector = BillboardIcons.IcoPlay,
            valueOffImageVector = BillboardIcons.IcoPause,
            value = true,
        ) {

        }
    }
}