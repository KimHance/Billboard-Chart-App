package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.datasource.service.BillboardService
import javax.inject.Inject

class ChartDataSourceImpl @Inject constructor(
    private val billboardService: BillboardService,
) : ChartDataSource {
    override suspend fun getHot100(): BillboardResponse =
        billboardService.getHot100().getOrThrow()

    override suspend fun getBillboard200(): BillboardResponse =
        billboardService.getBillboard200().getOrThrow()

    override suspend fun getGlobal200(): BillboardResponse =
        billboardService.getGlobal200().getOrThrow()

    override suspend fun getArtist100(): BillboardResponse =
        billboardService.getArtist100().getOrThrow()
}
