package com.hancekim.billboard.core.designfoundation.modifier

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

fun Modifier.clickableIfNeed(
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: (() -> Unit)?,
): Modifier = then(
    onClick?.let {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
    } ?: Modifier,
)

fun Modifier.noRippleClickableIfNeeded(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: (() -> Unit)?
): Modifier = then(
    onClick?.let {
        Modifier.noRippleClickable(
            enabled = enabled,
            interactionSource = interactionSource,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )
    } ?: Modifier,
)

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = this.clickable(
    enabled = enabled,
    indication = null,
    onClickLabel = onClickLabel,
    role = role,
    interactionSource = interactionSource,
    onClick = onClick,
)

