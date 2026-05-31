package com.hancekim.billboard.core.designsystem.componenet.header

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Collection
import com.hancekim.billboard.core.designfoundation.icon.Logo
import com.hancekim.billboard.core.designfoundation.icon.Setting
import com.hancekim.billboard.core.designfoundation.indication.OffscreenIndication
import com.hancekim.billboard.core.designfoundation.modifier.clickableIfNeed
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.resource.R

@Composable
fun BillboardHeader(
    title: String,
    modifier: Modifier = Modifier,
    isLogoVisible: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = BillboardIcons.Setting,
    trailingIconContentDescription: String? = null,
    onLeadingIconClick: (() -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    collectionCount: Int? = null,
    onCollectionClick: (() -> Unit)? = null,
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    leadingIcon?.let { icon ->
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .clickableIfNeed(
                                    role = Role.Button,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = OffscreenIndication(BillboardTheme.colorScheme.textSecondary),
                                    onClick = onLeadingIconClick
                                ),
                            imageVector = icon,
                            contentDescription = stringResource(R.string.cd_open_settings)
                        )
                    }
                    if (isLogoVisible) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = BillboardIcons.Logo,
                            tint = BillboardColor.Green400,
                            contentDescription = null
                        )
                    }
                    Text(
                        text = title,
                        style = BillboardTheme.typography.headingXl()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (collectionCount != null && onCollectionClick != null) {
                        val collectionLabel = stringResource(R.string.cd_open_collection)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .noRippleClickable(onClick = onCollectionClick)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = collectionLabel
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = BillboardIcons.Collection,
                                contentDescription = null,
                                tint = BillboardTheme.colorScheme.textPrimary,
                            )
                            if (collectionCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .background(
                                            BillboardTheme.colorScheme.holoGlow,
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = collectionCount.toString(),
                                        style = BillboardTheme.typography.caption(),
                                        color = BillboardTheme.colorScheme.textOnAccent,
                                    )
                                }
                            }
                        }
                    }
                    trailingIcon?.let { icon ->
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .clickableIfNeed(
                                    role = Role.Button,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = OffscreenIndication(BillboardTheme.colorScheme.textSecondary),
                                    onClick = onTrailingIconClick
                                ),
                            imageVector = icon,
                            contentDescription = trailingIconContentDescription
                                ?: stringResource(R.string.cd_open_settings),
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BillboardHeader1Preview() {
    BillboardTheme {
        BillboardHeader(
            title = stringResource(R.string.app_name)
        )
    }
}

@ThemePreviews
@Composable
private fun BillboardHeader2Preview() {
    BillboardTheme {
        BillboardHeader(
            title = "Setting",
            trailingIcon = null,
            isLogoVisible = false,
            leadingIcon = BillboardIcons.ArrowBack,
        )
    }
}
