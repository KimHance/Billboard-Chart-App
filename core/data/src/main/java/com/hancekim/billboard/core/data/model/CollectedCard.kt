package com.hancekim.billboard.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectedCard(
    val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
)
