package com.hancekim.billboard.feature.setting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@CircuitInject(BillboardScreen.Setting::class, ActivityRetainedComponent::class)
@Composable
fun SettingUi(
    state: SettingState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val eventSink = state.eventSink
    val themeOptions = remember { AppTheme.entries.toPersistentList() }
    val fontOptions = remember { AppFont.entries.toPersistentList() }

    BackHandler {
        eventSink(SettingEvent.OnBackButtonClick)
    }

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = "Setting",
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { eventSink(SettingEvent.OnBackButtonClick) },
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(
                        vertical = 12.dp,
                        horizontal = 12.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SingleChoiceButtonRow(
                    title = "Theme",
                    options = themeOptions,
                    modifier = Modifier.fillMaxWidth(),
                    selectedIndex = themeOptions.indexOf(state.themeOption),
                    onClick = throttledProcess<AppTheme> {
                        eventSink(SettingEvent.OnThemeOptionClick(it))
                    }
                )
                SingleChoiceButtonRow(
                    title = "Font",
                    options = fontOptions,
                    modifier = Modifier.fillMaxWidth(),
                    selectedIndex = fontOptions.indexOf(state.fontOption),
                    onClick = throttledProcess<AppFont> {
                        eventSink(SettingEvent.OnFontOptionClick(it))
                    }
                )
            }
        }
    )
}

@Composable
fun <T> SingleChoiceButtonRow(
    title: String,
    options: ImmutableList<T>,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onClick: (T) -> Unit,
) {
    val colorScheme = BillboardTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 4.dp),
            color = colorScheme.textPrimary,
            style = BillboardTheme.typography.headingLg(),
        )
        SingleChoiceSegmentedButtonRow(
            modifier = modifier,
        ) {
            options.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = throttledProcess { onClick(item) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    colors = SegmentedButtonColors(
                        activeContainerColor = colorScheme.bgCard,
                        activeContentColor = colorScheme.textSecondary,
                        activeBorderColor = colorScheme.borderButton,
                        inactiveContainerColor = colorScheme.bgApp,
                        inactiveContentColor = colorScheme.textTertiary,
                        inactiveBorderColor = colorScheme.borderButton,
                        disabledActiveContainerColor = Color.Unspecified,
                        disabledActiveContentColor = Color.Unspecified,
                        disabledActiveBorderColor = Color.Unspecified,
                        disabledInactiveContainerColor = Color.Unspecified,
                        disabledInactiveContentColor = Color.Unspecified,
                        disabledInactiveBorderColor = Color.Unspecified,
                    ),
                    border = BorderStroke(1.dp, colorScheme.borderButton),
                ) {
                    Text(
                        text = item.toString(),
                        style = BillboardTheme.typography.buttonMd()
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SingleChoiceButtonRowPreview() {
    BillboardTheme {
        SingleChoiceButtonRow(
            title = "Theme",
            options = AppTheme.entries.toPersistentList(),
        ) {}
    }
}

@ThemePreviews
@Composable
private fun SettingUiPreview() {
    BillboardTheme {
        SettingUi(
            state = SettingState() {}
        )
    }
}