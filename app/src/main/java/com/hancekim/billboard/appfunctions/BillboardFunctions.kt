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

    /** 현재 빌보드 Hot 100 1위 곡. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class TopSong(
        /** Song title. */
        val title: String,
        /** Artist name. */
        val artist: String,
        /** Chart rank (always 1 for this function). */
        val rank: Int,
    )

    /**
     * Returns the current #1 song on Billboard Hot 100.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCurrentHot100TopSong(context: AppFunctionContext): TopSong {
        val overview = getBillboardHot100UseCase()
        val top = overview.chartList.firstOrNull { it.rank == 1 }
            ?: throw AppFunctionElementNotFoundException("Hot 100 has no rank-1 entry")
        return TopSong(title = top.title, artist = top.artist, rank = 1)
    }
}
