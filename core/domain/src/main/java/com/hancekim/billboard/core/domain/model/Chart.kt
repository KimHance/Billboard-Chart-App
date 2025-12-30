package com.hancekim.billboard.core.domain.model

data class Chart(
    val status: String = "",
    val rank: Int = 0,
    val title: String = "",
    val artist: String = "",
    val image: String = "",
    val lastWeek: Int = 0,
    val peakPosition: Int = 0,
    val debutPosition: Int = 0,
    val debutDate: String = "",
    val weekOnChart: Int = 0,
)