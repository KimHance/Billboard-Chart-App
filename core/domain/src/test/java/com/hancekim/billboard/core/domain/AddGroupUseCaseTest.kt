package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.exception.DuplicateGroupNameException
import com.hancekim.billboard.core.data.repository.GroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddGroupUseCaseTest {

    private val repo: GroupRepository = mockk(relaxed = true)
    private val useCase = AddGroupUseCase(repo)

    @Test
    fun `빈 이름이면 Empty 실패`() = runTest {
        val result = useCase("   ", 0xFFFFFFFF.toInt())
        assertTrue(result.exceptionOrNull() is GroupValidationError.Empty)
    }

    @Test
    fun `21자 이상이면 TooLong 실패`() = runTest {
        val result = useCase("A".repeat(21), 0)
        assertTrue(result.exceptionOrNull() is GroupValidationError.TooLong)
    }

    @Test
    fun `normalize 기준 중복이면 DuplicateName 실패`() = runTest {
        coEvery { repo.existsByName("workout") } returns true
        val result = useCase("  Workout ", 0)
        assertTrue(result.exceptionOrNull() is GroupValidationError.DuplicateName)
    }

    @Test
    fun `정상 입력이면 새 id 반환하고 trim 된 이름으로 저장`() = runTest {
        coEvery { repo.existsByName(any()) } returns false
        coEvery { repo.add("Workout", 123) } returns 42L
        val result = useCase("  Workout ", 123)
        assertEquals(42L, result.getOrNull())
        coVerify { repo.add("Workout", 123) }
    }

    @Test
    fun `existsByName 미스 후 insert 시 race 충돌 - DuplicateGroupNameException 을 DuplicateName 으로 매핑`() = runTest {
        coEvery { repo.existsByName(any()) } returns false
        coEvery { repo.add("Workout", 0) } throws DuplicateGroupNameException("Workout")
        val result = useCase("Workout", 0)
        assertTrue(result.exceptionOrNull() is GroupValidationError.DuplicateName)
    }
}
