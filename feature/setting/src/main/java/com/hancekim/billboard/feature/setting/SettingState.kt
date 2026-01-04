package com.hancekim.billboard.feature.setting

import androidx.compose.runtime.Stable
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable
data class SettingState(
    val fontOption: AppFont = AppFont.App,
    val themeOption: AppTheme = AppTheme.Dark,
    val eventSink: (SettingEvent) -> Unit,
) : CircuitUiState

sealed interface SettingEvent : CircuitUiEvent {

    data object OnBackButtonClick : SettingEvent
    data class OnThemeOptionClick(val theme: AppTheme) : SettingEvent
    data class OnFontOptionClick(val font: AppFont) : SettingEvent
}