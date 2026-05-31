package com.hancekim.billboard.feature.collection.component

import androidx.compose.runtime.Immutable

@Immutable
data class NewGroupFormState(
    val name: String,
    val colorArgb: Int?,
    val isDuplicate: Boolean,
)
