package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import com.hancekim.billboard.core.domain.model.AppTheme
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAppThemeFlowUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke() = preferenceRepository.getAppThemeFlow().map { AppTheme.fromCode(it) }
}