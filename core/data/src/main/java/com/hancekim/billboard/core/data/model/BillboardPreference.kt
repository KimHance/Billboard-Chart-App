package com.hancekim.billboard.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BillboardPreference(
    val fontCode: Int = 0,
    val themeCode: Int = 0,
)
