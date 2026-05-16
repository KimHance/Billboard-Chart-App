package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupsFlowUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    operator fun invoke(): Flow<List<Group>> = repo.getGroupsFlow()
}
