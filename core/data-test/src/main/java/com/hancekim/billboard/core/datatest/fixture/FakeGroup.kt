package com.hancekim.billboard.core.datatest.fixture

import com.hancekim.billboard.core.data.model.Group

fun fakeGroup(
    id: Long = Group.DEFAULT_ID,
    name: String = "Starred",
    colorArgb: Int = 0xFF00FF85.toInt(),
    createdAt: Long = 0L,
): Group = Group(id, name, colorArgb, createdAt)
