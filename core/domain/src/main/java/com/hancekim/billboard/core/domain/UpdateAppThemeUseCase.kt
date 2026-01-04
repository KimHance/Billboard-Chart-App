package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import com.hancekim.billboard.core.domain.model.AppTheme
import javax.inject.Inject

class UpdateAppThemeUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    suspend operator fun invoke(theme: AppTheme) {
        preferenceRepository.setAppTheme(theme.code)
    }
}