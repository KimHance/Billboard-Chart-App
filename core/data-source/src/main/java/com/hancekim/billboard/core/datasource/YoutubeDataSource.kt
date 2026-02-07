package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse

interface YoutubeDataSource {
    suspend fun searchVideo(query: String): YoutubeSearchResponse
    suspend fun getVideoInfo(videoId: String): VideoListResponse
}
