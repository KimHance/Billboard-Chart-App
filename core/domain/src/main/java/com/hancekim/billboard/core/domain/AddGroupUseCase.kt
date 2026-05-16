package com.hancekim.billboard.core.domain

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
        return runCatching { repo.add(trimmed, colorArgb) }
            .recoverCatching { e ->
                if (e::class.qualifiedName == "android.database.sqlite.SQLiteConstraintException") {
                    throw GroupValidationError.DuplicateName
                } else throw e
            }
    }
}
