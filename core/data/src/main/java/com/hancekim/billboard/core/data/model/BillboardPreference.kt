package com.hancekim.billboard.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BillboardPreference(
    val isSystemFont: Boolean = true,
    val theme: Int = 0,
)
