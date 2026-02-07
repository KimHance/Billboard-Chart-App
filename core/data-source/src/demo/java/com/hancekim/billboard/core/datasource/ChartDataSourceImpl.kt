package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.BillboardChart
import com.hancekim.billboard.core.data.model.BillboardResponse
import javax.inject.Inject

class ChartDataSourceImpl @Inject constructor() : ChartDataSource {
    override suspend fun getHot100(): BillboardResponse = getFakeList()

    override suspend fun getBillboard200(): BillboardResponse = getFakeList()

    override suspend fun getGlobal200(): BillboardResponse = getFakeList()

    override suspend fun getArtist100(): BillboardResponse = getFakeList()

    private fun getFakeList(): BillboardResponse {
        return BillboardResponse(
            date = "2024-01-01",
            data = buildList {
                repeat(100) {
                    add(
                        BillboardChart(
                            status = "Steady",
                            rank = it + 1,
                            title = "Demo Song ${it + 1}",
                            artist = "Demo Artist ${it + 1}",
                        )
                    )
                }
            }
        )
    }
}
