package com.hancekim.billboard.core.datasource.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "collected_cards")
data class CollectedCardEntity(
    @PrimaryKey val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
)
