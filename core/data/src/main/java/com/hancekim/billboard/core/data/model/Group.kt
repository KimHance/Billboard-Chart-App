package com.hancekim.billboard.core.data.model

data class Group(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long,
) {
    companion object {
        // Default 그룹 고정 id — 삭제 금지 대상
        const val DEFAULT_ID: Long = 1L
    }
}
