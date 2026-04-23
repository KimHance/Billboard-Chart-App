package com.hancekim.billboard.core.domain.model

data class CollectedCard(
    val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
) {
    companion object {
        const val MAX_SLOTS = 9

        fun createKey(title: String, artist: String): String = "$title::$artist"
    }
}
