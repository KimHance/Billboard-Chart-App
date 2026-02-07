package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import com.hancekim.billboard.core.data.repository.YoutubeRepository
import com.hancekim.billboard.core.datasource.YoutubeDataSource
import javax.inject.Inject

class YoutubeRepositoryImpl @Inject constructor(
    private val dataSource: YoutubeDataSource,
) : YoutubeRepository {

    override suspend fun searchVideo(query: String): YoutubeSearchResponse =
        dataSource.searchVideo(query)

    override suspend fun getVideoInfo(videoId: String): VideoListResponse =
        dataSource.getVideoInfo(videoId)
}
