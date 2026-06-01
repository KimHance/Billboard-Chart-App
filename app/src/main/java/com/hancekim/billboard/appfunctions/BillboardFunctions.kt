package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import javax.inject.Inject

// Gemini 앱이 콜드부팅으로 우리 앱을 깨워 호출할 때 진입점.
// Hilt 가 GetBillboardHot100UseCase 를 주입 → Retrofit 으로 실시간 차트 가져옴.
class BillboardFunctions @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
) {

    /** A song entry on the Billboard Hot 100 chart at a specific rank. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class ChartSong(
        /** Song title. */
        val title: String,
        /** Artist name. */
        val artist: String,
        /** Chart rank position (1 to 100). */
        val rank: Int,
    )

    /**
     * Returns the Billboard Hot 100 song at the specified chart position.
     *
     * 1위 조회는 rank=1 로 호출하면 되므로 별도 Top 함수는 불필요 — generic 하나로 통합.
     *
     * @param rank The chart rank to look up. Must be between 1 and 100 inclusive.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getHot100SongByRank(context: AppFunctionContext, rank: Int): ChartSong {
        require(rank in 1..100) { "rank must be in 1..100, got $rank" }
        val overview = getBillboardHot100UseCase()
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw AppFunctionElementNotFoundException("Hot 100 has no rank-$rank entry")
        return ChartSong(title = entry.title, artist = entry.artist, rank = rank)
    }
}
