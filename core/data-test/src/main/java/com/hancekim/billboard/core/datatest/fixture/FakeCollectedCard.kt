package com.hancekim.billboard.core.datatest.fixture

import com.hancekim.billboard.core.data.model.CollectedCard

// 테스트용 CollectedCard 픽스처 — 테스트 코드에서 :core:data 모델을 직접 참조하지 않도록 헬퍼 제공
fun fakeCollectedCard(
    key: String,
    title: String = "TestTitle",
    artist: String = "TestArtist",
    albumArtUrl: String = "",
    collectedAt: Long = 0L,
    lastWeek: Int = 1,
    peakPosition: Int = 1,
    weeksOnChart: Int = 1,
): CollectedCard = CollectedCard(
    key = key,
    title = title,
    artist = artist,
    albumArtUrl = albumArtUrl,
    collectedAt = collectedAt,
    lastWeek = lastWeek,
    peakPosition = peakPosition,
    weeksOnChart = weeksOnChart,
)
