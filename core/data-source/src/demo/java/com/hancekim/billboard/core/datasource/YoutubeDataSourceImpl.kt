package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import javax.inject.Inject

class YoutubeDataSourceImpl @Inject constructor() : YoutubeDataSource {
    override suspend fun searchVideo(query: String): YoutubeSearchResponse {
        return YoutubeSearchResponse()
    }

    override suspend fun getVideoInfo(videoId: String): VideoListResponse {
        return VideoListResponse()
    }
}
