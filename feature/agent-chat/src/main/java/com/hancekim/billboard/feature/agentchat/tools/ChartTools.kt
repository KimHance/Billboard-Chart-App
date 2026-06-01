package com.hancekim.billboard.feature.agentchat.tools

import com.google.adk.kt.annotations.Param
import com.google.adk.kt.annotations.Tool
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import javax.inject.Inject

/**
 * 차트 조회 전용 Tool 컬렉션.
 *
 * BillboardFunctions.getSongChartByRank / getArtist100ByRank 와 동일한 UseCase 경로를 사용해
 * @AppFunction 진입점과 결과 동치성을 보장한다.
 */
class ChartTools @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
    private val getBillboardGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboardArtist100UseCase: GetBillboardArtist100UseCase,
) {

    /**
     * Returns a Billboard song chart entry (title + artist) at a specific rank.
     * Supports song-oriented charts: hot100 (1..100), billboard200 (1..200), global200 (1..200).
     * For artist-level lookups use getArtist100ByRank.
     */
    @Tool
    suspend fun getSongChartByRank(
        @Param("Chart identifier: hot100 | billboard200 | global200") chartType: String,
        @Param("Chart rank position (1..100 for hot100, 1..200 for billboard200/global200)") rank: Int,
    ): Map<String, Any?> {
        val normalized = chartType.lowercase()
        val maxRank = when (normalized) {
            "hot100" -> 100
            "billboard200", "global200" -> 200
            else -> throw IllegalArgumentException(
                "Unknown chartType '$chartType'. Use 'hot100', 'billboard200', or 'global200'."
            )
        }
        require(rank in 1..maxRank) { "rank must be in 1..$maxRank for $normalized, got $rank" }

        val overview = when (normalized) {
            "hot100" -> getBillboardHot100UseCase()
            "billboard200" -> getBillboard200UseCase()
            "global200" -> getBillboardGlobal200UseCase()
            else -> error("unreachable")
        }
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw IllegalStateException("$normalized has no rank-$rank entry")
        return mapOf(
            "title" to entry.title,
            "artist" to entry.artist,
            "rank" to rank,
            "chartType" to normalized,
        )
    }

    /**
     * Returns the Billboard Artist 100 entry (artist name only) at a specific rank between 1 and 100.
     * Artist 100 ranks artists, not songs — there is no song title.
     */
    @Tool
    suspend fun getArtist100ByRank(
        @Param("Chart rank position (1..100)") rank: Int,
    ): Map<String, Any?> {
        require(rank in 1..100) { "rank must be in 1..100, got $rank" }
        val overview = getBillboardArtist100UseCase()
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw IllegalStateException("Artist 100 has no rank-$rank entry")
        // 도메인 매퍼에 따라 artist 또는 title 한쪽에 아티스트 이름이 들어옴.
        val name = entry.artist.ifBlank { entry.title }
        return mapOf(
            "name" to name,
            "rank" to rank,
        )
    }
}
