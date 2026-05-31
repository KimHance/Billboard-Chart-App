package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.CollectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlinx.coroutines.runBlocking

class MoveCollectedCardUseCaseTest {

    private val repo: CollectionRepository = mockk(relaxed = true)
    private val useCase = MoveCollectedCardUseCase(repo)

    @Test
    fun `repo moveToGroup 에 key 와 groupId 를 그대로 위임한다`() = runTest {
        useCase(key = "abc::xyz", groupId = 7L)
        coVerify(exactly = 1) { repo.moveToGroup("abc::xyz", 7L) }
    }

    @Test
    fun `repo 가 throw 하면 UseCase 가 그대로 전파한다 - 계약 회귀 방지`() {
        coEvery { repo.moveToGroup(any(), any()) } throws IllegalStateException("boom")
        assertThrows(IllegalStateException::class.java) {
            runBlocking { useCase("k", 1L) }
        }
    }
}
