package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.ChartRepository
import javax.inject.Inject

class GetBillboard200UseCase @Inject constructor(
    private val repository: ChartRepository,
) {
    suspend operator fun invoke() = repository.getBillboard200()
}