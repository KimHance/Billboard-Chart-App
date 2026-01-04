package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import com.hancekim.billboard.core.domain.model.AppFont
import javax.inject.Inject

class UpdateAppFontUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(font: AppFont) {
        preferenceRepository.setAppFont(font.code)
    }
}