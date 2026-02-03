package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.BillboardChart
import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import javax.inject.Inject

class FakeChartRepository @Inject constructor() : ChartRepository {
    override suspend fun getHot100(): BillboardResponse {
        return getFakeList()
    }

    override suspend fun getBillboard200(): BillboardResponse {
        return getFakeList()
    }

    override suspend fun getGlobal200(): BillboardResponse {
        return getFakeList()
    }

    override suspend fun getArtist100(): BillboardResponse {
        return getFakeList()
    }

    private fun getFakeList(): BillboardResponse {
        return BillboardResponse(
            date = "",
            data = buildList {
                repeat(100) {
                    add(
                        BillboardChart(
                            status = "Steady",
                            rank = it + 1,
                            title = "",
                            artist = "artist $it",
                        )
                    )
                }
            }
        )
    }
}