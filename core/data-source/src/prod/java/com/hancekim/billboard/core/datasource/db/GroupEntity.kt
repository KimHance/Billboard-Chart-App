package com.hancekim.billboard.core.datasource.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index(value = ["nameNormalized"], unique = true)],
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val nameNormalized: String,
    val colorArgb: Int,
    val createdAt: Long,
)
