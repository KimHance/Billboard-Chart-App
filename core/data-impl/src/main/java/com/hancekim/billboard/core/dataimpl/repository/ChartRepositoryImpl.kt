package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.datasource.ChartDataSource
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val dataSource: ChartDataSource,
) : ChartRepository {
    override suspend fun getHot100(): BillboardResponse =
        dataSource.getHot100()

    override suspend fun getBillboard200(): BillboardResponse =
        dataSource.getBillboard200()

    override suspend fun getGlobal200(): BillboardResponse =
        dataSource.getGlobal200()

    override suspend fun getArtist100(): BillboardResponse =
        dataSource.getArtist100()
}
