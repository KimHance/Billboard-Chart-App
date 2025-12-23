package com.hancekim.billboard.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillboardResponse(
    @SerialName("date")
    val date: String = "",
    @SerialName("data")
    val data: List<BillboardChart> = emptyList()
)

@Serializable
data class BillboardChart(
    @SerialName("status")
    val status: String = "",
    @SerialName("rank")
    val rank: Int = 0,
    @SerialName("title")
    val title: String = "",
    @SerialName("artist")
    val artist: String = "",
    @SerialName("image")
    val image: String = "",
    @SerialName("last_week")
    val lastWeek: Int? = null,
    @SerialName("peak_position")
    val peakPosition: Int = 0,
    @SerialName("peak_date")
    val peakDate: String? = null,
    @SerialName("debut_position")
    val debutPosition: Int? = null,
    @SerialName("debut_date")
    val debutDate: String? = null,
    @SerialName("weeks_on_chart")
    val weeksOnChart: Int = 0
)
