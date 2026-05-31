package com.hancekim.billboard.core.domain.mapper

import com.hancekim.billboard.core.domain.model.Group
import com.hancekim.billboard.core.data.model.Group as DataGroup

internal fun DataGroup.toDomain(): Group = Group(
    id = id,
    name = name,
    colorArgb = colorArgb,
    createdAt = createdAt,
)
