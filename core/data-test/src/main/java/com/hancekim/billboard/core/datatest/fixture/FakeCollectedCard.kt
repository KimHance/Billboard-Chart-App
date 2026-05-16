package com.hancekim.billboard.core.datatest.fixture

import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.model.Group

fun fakeCollectedCard(
    key: String,
    title: String = "TestTitle",
    artist: String = "TestArtist",
    albumArtUrl: String = "",
    collectedAt: Long = 0L,
    lastWeek: Int = 1,
    peakPosition: Int = 1,
    weeksOnChart: Int = 1,
    groupId: Long = Group.DEFAULT_ID,
): CollectedCard = CollectedCard(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
    groupId = groupId,
)
