package com.hancekim.billboard.core.domain.mapper

import com.hancekim.billboard.core.data.model.ContentDetails
import com.hancekim.billboard.core.data.model.SearchResult
import com.hancekim.billboard.core.data.model.VideoItem
import com.hancekim.billboard.core.domain.model.YoutubeVideoDetail

fun toYoutubeVideoDetail(
    searchResult: SearchResult,
    videoItem: VideoItem,
): YoutubeVideoDetail {
    return YoutubeVideoDetail(
        videoId = searchResult.id.videoId,
        thumbnailUrl = searchResult.snippet.thumbnails.high.url,
        isPlayable = isPlayableInKorea(videoItem.contentDetails),
    )
}

private fun isPlayableInKorea(contentDetails: ContentDetails): Boolean {
    val restriction = contentDetails.regionRestriction
    val blocked = restriction.blocked.map { it.uppercase() }
    val allowed = restriction.allowed.map { it.uppercase() }
    if ("KR" in blocked) return false
    if (allowed.isNotEmpty() && "KR" !in allowed) return false
    val kmrb = contentDetails.contentRating.kmrbRating
    return !(kmrb == "kmrbTeen" || kmrb == "kmrbR")
}
