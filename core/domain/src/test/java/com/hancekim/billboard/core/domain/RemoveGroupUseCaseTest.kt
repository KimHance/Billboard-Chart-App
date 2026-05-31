package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.GroupRepository
import com.hancekim.billboard.core.domain.model.Group
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveGroupUseCaseTest {

    private val repo: GroupRepository = mockk(relaxed = true)
    private val useCase = RemoveGroupUseCase(repo)

    @Test
    fun `Default 그룹 삭제 시 DefaultGroupNotDeletable 실패`() = runTest {
        val result = useCase(Group.DEFAULT_ID)
        assertTrue(result.isFailure)
        assertEquals(GroupValidationError.DefaultGroupNotDeletable, result.exceptionOrNull())
    }

    @Test
    fun `일반 그룹 id 면 repo remove 호출 후 success`() = runTest {
        val result = useCase(42L)
        assertTrue(result.isSuccess)
        coVerify { repo.remove(42L) }
    }
}
