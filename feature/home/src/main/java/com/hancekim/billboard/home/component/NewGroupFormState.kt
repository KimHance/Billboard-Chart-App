package com.hancekim.billboard.home.component

import androidx.compose.runtime.Immutable

@Immutable
data class NewGroupFormState(
    val name: String,
    val hex: String,
    val isDuplicate: Boolean,
    val isHexValid: Boolean,
)
