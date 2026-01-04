package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.PreferenceRepository
import com.hancekim.billboard.core.domain.model.AppFont
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAppFontFlowUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke() = preferenceRepository.getAppFontFlow().map { AppFont.fromCode(it) }
}