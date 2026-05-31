package com.hancekim.billboard.core.domain

import app.cash.turbine.test
import com.hancekim.billboard.core.datatest.repository.FakeGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.hancekim.billboard.core.domain.model.Group as DomainGroup

class GetGroupsFlowUseCaseTest {

    @Test
    fun `data Group 을 domain Group 으로 매핑해 흐른다`() = runTest {
        val repo = FakeGroupRepository().apply { add("Workout", 0xFFFFB400.toInt()) }
        val useCase = GetGroupsFlowUseCase(repo)

        useCase().test {
            val emitted = awaitItem()
            // 디폴트 + Workout 두 개가 모두 노출
            assertEquals(2, emitted.size)
            val workout = emitted.first { it.name == "Workout" }
            assertEquals(0xFFFFB400.toInt(), workout.colorArgb)
            // 반환 타입이 도메인 모델임은 타입 시그니처가 보증 (Flow<List<DomainGroup>>)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `초기엔 Default 그룹 하나만 노출`() = runTest {
        val repo = FakeGroupRepository()
        val useCase = GetGroupsFlowUseCase(repo)

        useCase().test {
            val initial = awaitItem()
            assertEquals(1, initial.size)
            assertEquals(DomainGroup.DEFAULT_ID, initial.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
