package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.GroupRepository
import com.hancekim.billboard.core.domain.model.Group
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        if (id == Group.DEFAULT_ID) {
            return Result.failure(GroupValidationError.DefaultGroupNotDeletable)
        }
        return runCatching { repo.remove(id) }
    }
}
