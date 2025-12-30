package com.hancekim.billboard.core.domain.model

data class Chart(
    val status: String,
    val rank: Int,
    val title: String,
    val artist: String,
    val image: String,
    val lastWeek: Int,
    val peakPosition: Int,
    val debutPosition: Int,
    val debutDate: String,
    val weekOnChart: Int,
)