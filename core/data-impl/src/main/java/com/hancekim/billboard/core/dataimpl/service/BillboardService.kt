package com.hancekim.billboard.core.dataimpl.service

import com.hancekim.billboard.core.data.model.BillboardResponse
import retrofit2.http.GET

interface BillboardService {
    @GET("billboard_hot_100.json")
    suspend fun getHot100(): Result<BillboardResponse>

    @GET("billboard_200.json")
    suspend fun getBillboard200(): Result<BillboardResponse>

    @GET("billboard_global_200.json")
    suspend fun getGlobal200(): Result<BillboardResponse>

    @GET("billboard_artist_100.json")
    suspend fun getArtist100(): Result<BillboardResponse>
}