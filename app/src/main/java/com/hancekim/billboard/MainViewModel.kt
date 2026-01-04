package com.hancekim.billboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hancekim.billboard.core.domain.GetAppFontFlowUseCase
import com.hancekim.billboard.core.domain.GetAppThemeFlowUseCase
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getAppFontFlowUseCase: GetAppFontFlowUseCase,
    getAppThemeFlowUseCase: GetAppThemeFlowUseCase,
) : ViewModel() {
    val appFont = getAppFontFlowUseCase().stateIn(
        initialValue = AppFont.App,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L)
    )

    val appTheme = getAppThemeFlowUseCase().stateIn(
        initialValue = AppTheme.Dark,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L)
    )
}