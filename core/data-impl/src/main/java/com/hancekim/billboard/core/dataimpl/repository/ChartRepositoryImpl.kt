package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.dataimpl.service.BillboardService
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val billboardService: BillboardService,
) : ChartRepository {
    override suspend fun getHot100(): BillboardResponse =
        billboardService.getHot100().getOrThrow()

    override suspend fun getBillboard200(): BillboardResponse =
        billboardService.getBillboard200().getOrThrow()

    override suspend fun getGlobal200(): BillboardResponse =
        billboardService.getGlobal200().getOrThrow()

    override suspend fun getArtist100(): BillboardResponse =
        billboardService.getArtist100().getOrThrow()
}