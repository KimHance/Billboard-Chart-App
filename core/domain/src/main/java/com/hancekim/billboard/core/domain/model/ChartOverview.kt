package com.hancekim.billboard.core.domain.model

data class ChartOverview(
    val date: String = "",
    val chartList: List<Chart> = listOf(),
    val topTen: List<Chart> = listOf(),
)
