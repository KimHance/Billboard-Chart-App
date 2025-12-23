package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.BillboardResponse

interface ChartRepository {
    suspend fun getHot100(): Result<BillboardResponse>
    suspend fun getBillboard200(): Result<BillboardResponse>
    suspend fun getGlobal200(): Result<BillboardResponse>
    suspend fun getArtist100(): Result<BillboardResponse>
}