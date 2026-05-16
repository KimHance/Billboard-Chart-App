package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class RemoveGroupUseCaseTest {

    private val repo: GroupRepository = mockk(relaxed = true)
    private val useCase = RemoveGroupUseCase(repo)

    @Test
    fun `Default 그룹 삭제 시 IllegalArgumentException`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(Group.DEFAULT_ID) }
        }
    }

    @Test
    fun `일반 그룹 id 면 repo remove 호출`() = runTest {
        useCase(42L)
        coVerify { repo.remove(42L) }
    }
}
