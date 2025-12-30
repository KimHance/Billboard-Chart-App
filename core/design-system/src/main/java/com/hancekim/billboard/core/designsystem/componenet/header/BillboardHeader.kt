package com.hancekim.billboard.core.designsystem.componenet.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Logo
import com.hancekim.billboard.core.designfoundation.icon.Menu
import com.hancekim.billboard.core.designfoundation.indication.OffscreenIndication
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun BillboardHeader(
    modifier: Modifier = Modifier,
    onLeadingIconClick: () -> Unit,
) {
    val bgColor = BillboardTheme.colorScheme.bgAppbar
    val contentColor = BillboardTheme.colorScheme.textPrimary
    Box(
        modifier = modifier
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 3.dp,
                    offset = DpOffset(0.dp, 1.dp),
                    color = Color.Black.copy(.1f),
                )
            )
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 2.dp,
                    offset = DpOffset(0.dp, 1.dp),
                    spread = (-1).dp,
                    color = Color.Black.copy(.1f),
                )
            )
            .drawBehind { drawRect(bgColor) }
            .semantics { isTraversalGroup = true }
            .pointerInput(Unit) {},
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            Row(
                modifier = Modifier
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = OffscreenIndication(BillboardTheme.colorScheme.textSecondary),
                            onClick = throttledProcess { onLeadingIconClick() }
                        ),
                    imageVector = BillboardIcons.Menu,
                    contentDescription = "menu_button"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = BillboardIcons.Logo,
                        tint = BillboardColor.Green400,
                        contentDescription = null
                    )
                    Text(
                        text = "BILLBOARD",
                        style = BillboardTheme.typography.headingXl()
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BillboardHeaderPreview() {
    BillboardTheme {
        BillboardHeader {

        }
    }
}