package com.hancekim.billboard.core.domain.model

import com.hancekim.billboard.core.data.model.Group

data class CollectedCard(
    val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
    val groupId: Long = Group.DEFAULT_ID,
) {
    companion object {
        fun createKey(title: String, artist: String): String = "$title::$artist"
    }
}
