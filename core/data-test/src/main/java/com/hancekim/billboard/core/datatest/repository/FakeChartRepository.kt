package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import javax.inject.Inject

class FakeChartRepository @Inject constructor() : ChartRepository {
    override suspend fun getHot100(): Result<BillboardResponse> {
        return runCatching { BillboardResponse() }
    }

    override suspend fun getBillboard200(): Result<BillboardResponse> {
        return runCatching { BillboardResponse() }
    }

    override suspend fun getGlobal200(): Result<BillboardResponse> {
        return runCatching { BillboardResponse() }
    }

    override suspend fun getArtist100(): Result<BillboardResponse> {
        return runCatching { BillboardResponse() }
    }
}