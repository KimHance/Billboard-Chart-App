package com.hancekim.billboard.core.datatest.fixture

import com.hancekim.billboard.core.data.model.Group

// 디폴트 그룹 id — 테스트가 :core:data 모델을 직접 import 하지 않도록 fixture 가 노출.
const val DEFAULT_GROUP_ID: Long = Group.DEFAULT_ID

fun fakeGroup(
    id: Long = DEFAULT_GROUP_ID,
    name: String = Group.DEFAULT_NAME,
    colorArgb: Int = Group.DEFAULT_COLOR_ARGB,
    createdAt: Long = 0L,
): Group = Group(id, name, colorArgb, createdAt)
