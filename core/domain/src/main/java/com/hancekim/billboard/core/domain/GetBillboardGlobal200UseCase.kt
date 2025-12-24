package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.ChartRepository
import javax.inject.Inject

class GetBillboardGlobal200UseCase @Inject constructor(
    private val repository: ChartRepository
) {
    suspend operator fun invoke() = repository.getGlobal200()
}