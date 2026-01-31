package com.hancekim.billboard.core.data.exception

sealed class YoutubeException(message: String) : Exception(message) {

    // Search API errors
    data class InvalidChannelId(override val message: String) : YoutubeException(message)
    data class InvalidLocation(override val message: String) : YoutubeException(message)
    data class InvalidRelevanceLanguage(override val message: String) : YoutubeException(message)
    data class InvalidSearchFilter(override val message: String) : YoutubeException(message)

    // Video API errors
    data class VideoNotFound(override val message: String) : YoutubeException(message)
    data class VideoForbidden(override val message: String) : YoutubeException(message)
    data class VideoChartNotFound(override val message: String) : YoutubeException(message)

    data class BadRequest(override val message: String) : YoutubeException(message)
    data class Unknown(override val message: String) : YoutubeException(message)
}
