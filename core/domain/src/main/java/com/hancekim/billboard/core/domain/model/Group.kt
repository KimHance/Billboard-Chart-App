package com.hancekim.billboard.core.domain.model

import com.hancekim.billboard.core.data.model.Group as DataGroup

data class Group(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long,
) {
    companion object {
        // Default 그룹 상수는 :core:data Group 이 SoT — 도메인은 동일 값을 재노출만 한다.
        // (:core:data-source / :core:data-test 가 :core:domain 을 참조할 수 없어 data 측 상수도 필요.)
        const val DEFAULT_ID: Long = DataGroup.DEFAULT_ID
        const val DEFAULT_NAME: String = DataGroup.DEFAULT_NAME
        const val DEFAULT_COLOR_ARGB: Int = DataGroup.DEFAULT_COLOR_ARGB
    }
}
