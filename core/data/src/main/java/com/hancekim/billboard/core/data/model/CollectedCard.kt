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
) {
    companion object {
        // 컬렉션 슬롯 최대 개수 — domain/data-impl/data-test 모두 이 상수를 참조
        const val MAX_SLOTS = 9
    }
}
