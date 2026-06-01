package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Billboard 차트 [AppFunction] 모음.
 *
 * 외부 에이전트(Gemini 등)가 콜드부팅으로 우리 앱을 깨워 호출할 때의 진입점.
 * Hilt 가 4 개 차트 UseCase 를 주입 → Retrofit 으로 실시간 차트를 가져온다.
 *
 * Schema 분리 근거:
 *  - Song 차트 3 종(Hot 100 / Billboard 200 / Global 200)은 (title, artist, rank) 동일 schema 라
 *    단일 [getSongChartByRank] 로 통합하고 chartType 으로 분기.
 *  - Artist 100 은 아티스트 단위라 곡/앨범 schema 가 의미가 없음 — 별도 [getArtist100ByRank] 로 분리.
 */
class BillboardFunctions @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
    private val getBillboardGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboardArtist100UseCase: GetBillboardArtist100UseCase,
) {

    /** A song or album entry on a Billboard song chart at a specific rank. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class ChartSong(
        /** Song or album title. */
        val title: String,
        /** Artist credits. */
        val artist: String,
        /** Chart rank position. */
        val rank: Int,
        /** Source chart identifier: "hot100", "billboard200", or "global200". */
        val chartType: String,
    )

    /** An artist entry on the Billboard Artist 100 chart at a specific rank. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class ChartArtist(
        /** Artist name. */
        val name: String,
        /** Chart rank position (1 to 100). */
        val rank: Int,
    )

    /**
     * Look up a single entry on a Billboard song chart by its chart position.
     *
     * Supports three song-oriented charts that share the same schema:
     *  - "hot100": Billboard Hot 100 singles chart (valid rank 1..100)
     *  - "billboard200": Billboard 200 albums chart (valid rank 1..200)
     *  - "global200": Billboard Global 200 singles chart (valid rank 1..200)
     *
     * For Artist 100 use [getArtist100ByRank] instead — Artist 100 ranks artists, not songs.
     *
     * @param context The execution context.
     * @param chartType Chart identifier. Must be one of "hot100", "billboard200", "global200".
     *                  Case-insensitive.
     * @param rank Chart rank to look up. 1..100 for "hot100"; 1..200 for "billboard200" and "global200".
     * @return A [ChartSong] populated with the title, artist credits, rank, and normalized chartType.
     * @throws AppFunctionInvalidArgumentException If [chartType] is not recognized or [rank] is out of range.
     *                                             Suggest the caller pick one of the documented chartType
     *                                             values and provide a rank within the allowed range.
     * @throws AppFunctionElementNotFoundException If the chart has no entry at [rank]. Usually transient —
     *                                             retry after the next chart refresh.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getSongChartByRank(
        context: AppFunctionContext,
        chartType: String,
        rank: Int,
    ): ChartSong = withContext(Dispatchers.IO) {
        val normalized = chartType.lowercase()
        val maxRank = when (normalized) {
            "hot100" -> 100
            "billboard200", "global200" -> 200
            else -> throw AppFunctionInvalidArgumentException(
                "Unknown chartType '$chartType'. Use 'hot100', 'billboard200', or 'global200'."
            )
        }
        if (rank !in 1..maxRank) {
            throw AppFunctionInvalidArgumentException(
                "rank must be in 1..$maxRank for $normalized, got $rank"
            )
        }

        val overview = when (normalized) {
            "hot100" -> getBillboardHot100UseCase()
            "billboard200" -> getBillboard200UseCase()
            "global200" -> getBillboardGlobal200UseCase()
            else -> error("unreachable") // 위에서 이미 검증됨.
        }
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw AppFunctionElementNotFoundException("$normalized has no rank-$rank entry")
        ChartSong(
            title = entry.title,
            artist = entry.artist,
            rank = rank,
            chartType = normalized,
        )
    }

    /**
     * Look up a single entry on the Billboard Artist 100 chart by chart position.
     *
     * Artist 100 ranks artists themselves (not songs or albums), so this function returns
     * a [ChartArtist] with just the artist `name` — there is no separate title field.
     *
     * @param context The execution context.
     * @param rank Chart rank to look up. Must be between 1 and 100 inclusive.
     * @return A [ChartArtist] populated with the artist name and rank.
     * @throws AppFunctionInvalidArgumentException If [rank] is out of the 1..100 range.
     *                                             Suggest the caller correct the rank value.
     * @throws AppFunctionElementNotFoundException If the chart has no entry at [rank]. Usually transient —
     *                                             retry after the next chart refresh.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getArtist100ByRank(context: AppFunctionContext, rank: Int): ChartArtist =
        withContext(Dispatchers.IO) {
            if (rank !in 1..100) {
                throw AppFunctionInvalidArgumentException(
                    "rank must be in 1..100, got $rank"
                )
            }
            val overview = getBillboardArtist100UseCase()
            val entry = overview.chartList.firstOrNull { it.rank == rank }
                ?: throw AppFunctionElementNotFoundException("Artist 100 has no rank-$rank entry")
            // 도메인 매퍼 에 따라 artist 또는 title 한쪽에 아티스트 이름이 들어옴 — 비어있지 않은 쪽 선택.
            val name = entry.artist.ifBlank { entry.title }
            ChartArtist(name = name, rank = rank)
        }
}
