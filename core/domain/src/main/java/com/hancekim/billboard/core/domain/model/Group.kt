package com.hancekim.billboard.core.domain.model

data class Group(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long,
) {
    companion object {
        // 디폴트 그룹(Starred) 고정 id — 삭제 금지 대상. :core:data Group 의 DEFAULT_ID 와 동일 값.
        const val DEFAULT_ID: Long = 1L

        // 디폴트 그룹 시드 이름/색상 — DataSource 시드와 fake/preview 가 공통 참조.
        const val DEFAULT_NAME: String = "Starred"
        const val DEFAULT_COLOR_ARGB: Int = 0xFF00FF85.toInt()
    }
}
