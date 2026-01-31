package com.hancekim.billboard.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeSearchResponse(
    @SerialName("kind") val kind: String = "",
    @SerialName("etag") val etag: String = "",
    @SerialName("nextPageToken") val nextPageToken: String = "",
    @SerialName("prevPageToken") val prevPageToken: String = "",
    @SerialName("regionCode") val regionCode: String = "",
    @SerialName("pageInfo") val pageInfo: PageInfo = PageInfo(),
    @SerialName("items") val items: List<SearchResult> = emptyList()
)

@Serializable
data class PageInfo(
    @SerialName("totalResults") val totalResults: Int = 0,
    @SerialName("resultsPerPage") val resultsPerPage: Int = 0
)

@Serializable
data class SearchResult(
    @SerialName("kind") val kind: String = "",
    @SerialName("etag") val etag: String = "",
    @SerialName("id") val id: SearchResultId = SearchResultId(),
    @SerialName("snippet") val snippet: Snippet = Snippet()
)

@Serializable
data class SearchResultId(
    @SerialName("kind") val kind: String = "",
    @SerialName("videoId") val videoId: String = "",
    @SerialName("channelId") val channelId: String = "",
    @SerialName("playlistId") val playlistId: String = ""
)

@Serializable
data class Snippet(
    @SerialName("publishedAt") val publishedAt: String = "",
    @SerialName("channelId") val channelId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("thumbnails") val thumbnails: Thumbnails = Thumbnails(),
    @SerialName("channelTitle") val channelTitle: String = "",
    @SerialName("liveBroadcastContent") val liveBroadcastContent: String = ""
)

@Serializable
data class Thumbnails(
    @SerialName("default") val default: Thumbnail = Thumbnail(),
    @SerialName("medium") val medium: Thumbnail = Thumbnail(),
    @SerialName("high") val high: Thumbnail = Thumbnail()
)

@Serializable
data class Thumbnail(
    @SerialName("url") val url: String = "",
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0
)