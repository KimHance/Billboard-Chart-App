package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import com.hancekim.billboard.core.data.repository.YoutubeRepository
import com.hancekim.billboard.core.dataimpl.service.YoutubeService
import javax.inject.Inject

class YoutubeRepositoryImpl @Inject constructor(
    private val youtubeService: YoutubeService,
) : YoutubeRepository {

    override suspend fun searchVideo(query: String): YoutubeSearchResponse =
        youtubeService.getSearchList(query = query).getOrThrow()

    override suspend fun getVideoInfo(videoId: String): VideoListResponse =
        youtubeService.getVideoInfo(id = videoId).getOrThrow()
}
