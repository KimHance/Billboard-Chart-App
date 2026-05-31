package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.GroupRepository
import com.hancekim.billboard.core.domain.mapper.toDomain
import com.hancekim.billboard.core.domain.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupsFlowUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    operator fun invoke(): Flow<List<Group>> =
        repo.getGroupsFlow().map { list -> list.map { it.toDomain() } }
}
