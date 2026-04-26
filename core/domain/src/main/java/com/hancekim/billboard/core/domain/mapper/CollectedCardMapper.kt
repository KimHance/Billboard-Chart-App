package com.hancekim.billboard.core.domain.mapper

import com.hancekim.billboard.core.data.model.CollectedCard as DataCollectedCard
import com.hancekim.billboard.core.domain.model.CollectedCard

fun DataCollectedCard.toDomain() = CollectedCard(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
)

fun CollectedCard.toData() = DataCollectedCard(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
)
