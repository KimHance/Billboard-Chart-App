package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.BillboardResponse

interface ChartRepository {
    suspend fun getHot100(): BillboardResponse
    suspend fun getBillboard200(): BillboardResponse
    suspend fun getGlobal200(): BillboardResponse
    suspend fun getArtist100(): BillboardResponse
}