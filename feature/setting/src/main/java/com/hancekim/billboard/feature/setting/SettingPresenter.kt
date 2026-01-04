package com.hancekim.billboard.feature.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.domain.GetAppFontFlowUseCase
import com.hancekim.billboard.core.domain.GetAppThemeFlowUseCase
import com.hancekim.billboard.core.domain.UpdateAppFontUseCase
import com.hancekim.billboard.core.domain.UpdateAppThemeUseCase
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.launch

class SettingPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getAppThemeFlowUseCase: GetAppThemeFlowUseCase,
    private val getAppFontFlowUseCase: GetAppFontFlowUseCase,
    private val updateAppFontFlowUseCase: UpdateAppFontUseCase,
    private val updateAppThemeUseCase: UpdateAppThemeUseCase,
) : Presenter<SettingState> {
    @Composable
    override fun present(): SettingState {
        val scope = rememberCoroutineScope()
        val font by produceRetainedState(AppFont.App) {
            getAppFontFlowUseCase().collect { value = it }
        }
        val theme by produceRetainedState(AppTheme.Dark) {
            getAppThemeFlowUseCase().collect { value = it }
        }

        return SettingState(
            fontOption = font,
            themeOption = theme
        ) { event ->
            when (event) {
                SettingEvent.OnBackButtonClick -> navigator.pop()
                is SettingEvent.OnFontOptionClick -> {
                    scope.launch {
                        updateAppFontFlowUseCase(event.font)
                    }
                }

                is SettingEvent.OnThemeOptionClick -> {
                    scope.launch {
                        updateAppThemeUseCase(event.theme)
                    }
                }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Setting::class, ActivityRetainedComponent::class)
    fun interface SettingPresenterFactory {
        fun create(
            navigator: Navigator,
        ): SettingPresenter
    }
}