package com.hancekim.billboard.core.data.model

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
)
