package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.exception.DuplicateGroupNameException
import com.hancekim.billboard.core.data.repository.GroupRepository
import javax.inject.Inject

class AddGroupUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    suspend operator fun invoke(name: String, colorArgb: Int): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(GroupValidationError.Empty)
        if (trimmed.length > 20) return Result.failure(GroupValidationError.TooLong)
        if (repo.existsByName(trimmed.lowercase())) return Result.failure(GroupValidationError.DuplicateName)
        return try {
            Result.success(repo.add(trimmed, colorArgb))
        } catch (e: DuplicateGroupNameException) {
            // existsByName pre-check 와 insert 사이 race — 도메인 검증 실패로 매핑.
            Result.failure(GroupValidationError.DuplicateName)
        }
    }
}
