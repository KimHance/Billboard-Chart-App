package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse

interface YoutubeRepository {
    suspend fun searchVideo(query: String): YoutubeSearchResponse
    suspend fun getVideoInfo(videoId: String): VideoListResponse
}
