package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.YoutubeRepository
import com.hancekim.billboard.core.domain.mapper.toYoutubeVideoDetail
import com.hancekim.billboard.core.domain.model.YoutubeVideoDetail
import javax.inject.Inject

class GetYoutubeVideoDetailUseCase @Inject constructor(
    private val youtubeRepository: YoutubeRepository,
) {
    suspend operator fun invoke(title: String, artist: String): YoutubeVideoDetail {
        val query = "$title - $artist"
        val searchResponse = youtubeRepository.searchVideo(query)
        val searchResult = searchResponse.items.first()
        val videoId = searchResult.id.videoId
        val videoResponse = youtubeRepository.getVideoInfo(videoId)
        val videoItem = videoResponse.items.first()
        return toYoutubeVideoDetail(searchResult, videoItem)
    }
}
