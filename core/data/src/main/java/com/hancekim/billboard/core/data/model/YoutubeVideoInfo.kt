package com.hancekim.billboard.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoListResponse(
    @SerialName("kind") val kind: String = "",
    @SerialName("etag") val etag: String = "",
    @SerialName("items") val items: List<VideoItem> = emptyList()
)

@Serializable
data class VideoItem(
    @SerialName("kind") val kind: String = "",
    @SerialName("etag") val etag: String = "",
    @SerialName("id") val id: String = "",
    @SerialName("contentDetails") val contentDetails: ContentDetails = ContentDetails()
)

@Serializable
data class ContentDetails(
    @SerialName("contentRating") val contentRating: ContentRating = ContentRating(),
    @SerialName("regionRestriction") val regionRestriction: RegionRestriction = RegionRestriction(),
)

@Serializable
data class ContentRating(
    @SerialName("kmrbRating") val kmrbRating: String = "",
)

@Serializable
data class RegionRestriction(
    @SerialName("allowed") val allowed: List<String> = emptyList(),
    @SerialName("blocked") val blocked: List<String> = emptyList(),
)
