package com.hancekim.billboard.core.domain.mapper

import com.hancekim.billboard.core.data.model.BillboardChart
import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.ChartOverview

fun BillboardResponse.toOverview(): ChartOverview {
    val chartList = this.data.map {
        it.toModel()
    }
    return ChartOverview(
        date = this.date,
        chartList = chartList,
        topTen = chartList.take(10)
    )
}

fun BillboardChart.toModel(): Chart {
    return Chart(
        status = this.status,
        rank = this.rank,
        title = this.title,
        artist = this.artist,
        image = this.image,
        lastWeek = this.lastWeek,
        peakPosition = this.peakPosition,
        peakDate = this.peakDate,
        debutPosition = this.debutPosition,
        debutDate = this.debutDate,
        weekOnChart = this.weeksOnChart
    )
}