package com.hancekim.billboard.feature.collection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.fixture.DEFAULT_GROUP_ID
import com.hancekim.billboard.core.datatest.repository.FakeCollectionRepository
import com.hancekim.billboard.core.datatest.repository.FakeGroupRepository
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.hancekim.billboard.core.domain.RemoveGroupUseCase
import com.slack.circuit.test.FakeNavigator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionPresenterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var currentState: CollectionState? = null

    private fun buildPresenter(
        groupRepo: FakeGroupRepository = FakeGroupRepository(),
        collectionRepo: FakeCollectionRepository = FakeCollectionRepository(),
    ): CollectionPresenter = CollectionPresenter(
        navigator = FakeNavigator(BillboardScreen.Collection),
        getGroupsFlow = GetGroupsFlowUseCase(groupRepo),
        getCollectionFlow = GetCollectionFlowUseCase(collectionRepo),
        addGroupUseCase = AddGroupUseCase(groupRepo),
        removeGroupUseCase = RemoveGroupUseCase(groupRepo),
        removeFromCollectionUseCase = RemoveFromCollectionUseCase(collectionRepo),
    )

    private fun launchPresenter(presenter: CollectionPresenter) {
        composeTestRule.setContent { currentState = presenter.present() }
    }

    private fun sendEvent(event: CollectionEvent) {
        composeTestRule.runOnIdle { checkNotNull(currentState).eventSink(event) }
    }

    @Test
    fun `초기 currentGroupId 는 DEFAULT_ID`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.currentGroupId == DEFAULT_GROUP_ID
        }
        composeTestRule.runOnIdle {
            assertEquals(DEFAULT_GROUP_ID, checkNotNull(currentState).currentGroupId)
        }
    }

    @Test
    fun `OnSelectGroup 으로 currentGroupId 변경된다`() = runTest {
        val groupRepo = FakeGroupRepository().apply { add("Workout", 0xFFFFB400.toInt()) }
        launchPresenter(buildPresenter(groupRepo = groupRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.any { it.id == 2L } == true
        }
        sendEvent(CollectionEvent.OnSelectGroup(2L))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.currentGroupId == 2L
        }
        composeTestRule.runOnIdle {
            assertEquals(2L, checkNotNull(currentState).currentGroupId)
        }
    }

    @Test
    fun `빈 그룹이면 cards 빈 리스트 nowPlayingKey null`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.runOnIdle {
            val s = checkNotNull(currentState)
            assertTrue(s.cardsInCurrentGroup.isEmpty())
            assertNull(s.nowPlayingKey)
        }
    }

    @Test
    fun `OnRequestDeleteGroup 카드 0개면 즉시 삭제`() = runTest {
        val groupRepo = FakeGroupRepository().apply { add("Workout", 0) }
        launchPresenter(buildPresenter(groupRepo = groupRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.any { it.id == 2L } == true
        }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(2L))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.none { it.id == 2L } == true
        }
    }

    @Test
    fun `Default 삭제 시도는 무시`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState != null
        }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(DEFAULT_GROUP_ID))
        composeTestRule.runOnIdle {
            val s = checkNotNull(currentState)
            assertTrue(s.groups.any { it.id == DEFAULT_GROUP_ID })
        }
    }
}
