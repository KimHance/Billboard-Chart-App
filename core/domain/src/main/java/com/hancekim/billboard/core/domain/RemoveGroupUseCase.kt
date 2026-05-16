package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    suspend operator fun invoke(id: Long) {
        require(id != Group.DEFAULT_ID) { "Default group cannot be deleted" }
        repo.remove(id)
    }
}
