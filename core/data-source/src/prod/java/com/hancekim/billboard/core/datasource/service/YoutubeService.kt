package com.hancekim.billboard.core.datasource.service

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeService {
    @GET("search")
    suspend fun getSearchList(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("regionCode") regionCode: String = "KR",
        @Query("maxResults") maxResults: Int = 1,
    ): Result<YoutubeSearchResponse>

    @GET("videos")
    suspend fun getVideoInfo(
        @Query("part") part: String = "contentDetails,status",
        @Query("id") id: String,
    ): Result<VideoListResponse>
}
