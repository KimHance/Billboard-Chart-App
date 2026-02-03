package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import com.hancekim.billboard.core.data.repository.YoutubeRepository
import javax.inject.Inject

class FakeYoutubeRepository @Inject constructor() : YoutubeRepository {
    override suspend fun searchVideo(query: String): YoutubeSearchResponse {
        return YoutubeSearchResponse()
    }

    override suspend fun getVideoInfo(videoId: String): VideoListResponse {
        return VideoListResponse()
    }
}