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

        // Default 그룹 시드 이름/색상 — DataSource 시드와 fake 가 공통 참조해야 한다.
        // (도메인 레이어는 com.hancekim.billboard.core.domain.model.Group 이 같은 상수를 재노출)
        const val DEFAULT_NAME: String = "Starred"
        const val DEFAULT_COLOR_ARGB: Int = 0xFF00FF85.toInt()
    }
}
