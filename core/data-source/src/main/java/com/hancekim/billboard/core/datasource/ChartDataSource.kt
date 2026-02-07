package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.BillboardResponse

interface ChartDataSource {
    suspend fun getHot100(): BillboardResponse
    suspend fun getBillboard200(): BillboardResponse
    suspend fun getGlobal200(): BillboardResponse
    suspend fun getArtist100(): BillboardResponse
}
