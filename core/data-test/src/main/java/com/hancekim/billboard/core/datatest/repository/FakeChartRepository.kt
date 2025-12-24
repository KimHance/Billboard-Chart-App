package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import javax.inject.Inject

class FakeChartRepository @Inject constructor() : ChartRepository {
    override suspend fun getHot100(): BillboardResponse {
        return BillboardResponse()
    }

    override suspend fun getBillboard200(): BillboardResponse {
        return BillboardResponse()
    }

    override suspend fun getGlobal200(): BillboardResponse {
        return BillboardResponse()
    }

    override suspend fun getArtist100(): BillboardResponse {
        return BillboardResponse()
    }
}