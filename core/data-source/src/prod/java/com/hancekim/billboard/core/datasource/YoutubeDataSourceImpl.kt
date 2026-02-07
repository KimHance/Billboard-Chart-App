package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.exception.YoutubeException
import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import com.hancekim.billboard.core.datasource.service.YoutubeService
import com.hancekim.billboard.core.network.retrofit.ApiException
import javax.inject.Inject

class YoutubeDataSourceImpl @Inject constructor(
    private val youtubeService: YoutubeService,
) : YoutubeDataSource {

    override suspend fun searchVideo(query: String): YoutubeSearchResponse =
        youtubeService.getSearchList(query = query).getOrElse { throw it.toYoutubeException() }

    override suspend fun getVideoInfo(videoId: String): VideoListResponse =
        youtubeService.getVideoInfo(id = videoId).getOrElse { throw it.toYoutubeException() }

    private fun Throwable.toYoutubeException(): YoutubeException {
        if (this !is ApiException) return YoutubeException.Unknown(message ?: "Unknown error")
        val msg = apiError.message ?: "Unknown error"
        return when (httpCode) {
            400 -> when (apiError.code) {
                "invalidChannelId" -> YoutubeException.InvalidChannelId(msg)
                "invalidLocation" -> YoutubeException.InvalidLocation(msg)
                "invalidRelevanceLanguage" -> YoutubeException.InvalidRelevanceLanguage(msg)
                "invalidSearchFilter" -> YoutubeException.InvalidSearchFilter(msg)
                "videoChartNotFound" -> YoutubeException.VideoChartNotFound(msg)
                else -> YoutubeException.BadRequest(msg)
            }
            403 -> YoutubeException.VideoForbidden(msg)
            404 -> YoutubeException.VideoNotFound(msg)
            else -> YoutubeException.Unknown(msg)
        }
    }
}
