package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.dataimpl.service.BillboardService
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val billboardService: BillboardService,
) : ChartRepository {
    override suspend fun getHot100(): Result<BillboardResponse> = billboardService.getHot100()

    override suspend fun getBillboard200(): Result<BillboardResponse> = billboardService.getBillboard200()

    override suspend fun getGlobal200(): Result<BillboardResponse> = billboardService.getGlobal200()

    override suspend fun getArtist100(): Result<BillboardResponse> = billboardService.getArtist100()
}