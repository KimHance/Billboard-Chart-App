package com.hancekim.billboard.feature.collection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.fixture.DEFAULT_GROUP_ID
import com.hancekim.billboard.core.datatest.fixture.fakeCollectedCard
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
import org.junit.Assert.assertNotNull
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
        val groupRepo = FakeGroupRepository()
        val workoutId = groupRepo.add("Workout", 0xFFFFB400.toInt())
        launchPresenter(buildPresenter(groupRepo = groupRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.any { it.id == workoutId } == true
        }
        sendEvent(CollectionEvent.OnSelectGroup(workoutId))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.currentGroupId == workoutId
        }
        composeTestRule.runOnIdle {
            assertEquals(workoutId, checkNotNull(currentState).currentGroupId)
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
        val groupRepo = FakeGroupRepository()
        val workoutId = groupRepo.add("Workout", 0)
        launchPresenter(buildPresenter(groupRepo = groupRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.any { it.id == workoutId } == true
        }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(workoutId))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.none { it.id == workoutId } == true
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

    // ── nowPlayingKey LaunchedEffect 폴백 ─────────────────────────────────────────

    @Test
    fun `현재 그룹에 카드가 있으면 nowPlayingKey 가 첫 카드로 자동 설정된다`() = runTest {
        val collectionRepo = FakeCollectionRepository().apply {
            add(fakeCollectedCard("a", title = "First"))
            add(fakeCollectedCard("b", title = "Second"))
        }
        launchPresenter(buildPresenter(collectionRepo = collectionRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.nowPlayingKey != null
        }
        composeTestRule.runOnIdle {
            val s = checkNotNull(currentState)
            // 카드는 첫 emit 의 순서 그대로 (Fake 는 collectedAt 정렬 안 함)
            assertEquals(s.cardsInCurrentGroup.first().key, s.nowPlayingKey)
        }
    }

    @Test
    fun `활성 카드가 첫 카드가 아닐 때 삭제해도 nowPlayingKey 가 다른 카드로 자동 폴백된다`() = runTest {
        val collectionRepo = FakeCollectionRepository().apply {
            add(fakeCollectedCard("a"))
            add(fakeCollectedCard("b"))
            add(fakeCollectedCard("c"))
        }
        launchPresenter(buildPresenter(collectionRepo = collectionRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.cardsInCurrentGroup?.size == 3
        }
        // 비-첫 카드 활성화 → 삭제 → 폴백 검증 (첫 카드가 아닌 b 를 활성화)
        sendEvent(CollectionEvent.OnSelectCard("b"))
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState?.nowPlayingKey == "b" }
        sendEvent(CollectionEvent.OnRemoveCard("b"))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.cardsInCurrentGroup?.size == 2 &&
                currentState?.nowPlayingKey != null &&
                currentState?.nowPlayingKey != "b"
        }
    }

    // ── OnInspectClick ────────────────────────────────────────────────────────────

    @Test
    fun `OnInspectClick 으로 navigator 가 CardDetail 로 이동한다`() = runTest {
        val collectionRepo = FakeCollectionRepository().apply { add(fakeCollectedCard("a")) }
        val navigator = FakeNavigator(BillboardScreen.Collection)
        val presenter = CollectionPresenter(
            navigator = navigator,
            getGroupsFlow = GetGroupsFlowUseCase(FakeGroupRepository()),
            getCollectionFlow = GetCollectionFlowUseCase(collectionRepo),
            addGroupUseCase = AddGroupUseCase(FakeGroupRepository()),
            removeGroupUseCase = RemoveGroupUseCase(FakeGroupRepository()),
            removeFromCollectionUseCase = RemoveFromCollectionUseCase(collectionRepo),
        )
        launchPresenter(presenter)
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState?.nowPlayingKey == "a" }
        sendEvent(CollectionEvent.OnInspectClick)
        val next = navigator.awaitNextScreen()
        assertEquals(BillboardScreen.CardDetail("a"), next)
    }

    // ── 새 그룹 폼 ────────────────────────────────────────────────────────────────

    @Test
    fun `OnNewGroupClick 으로 form 이 비어있는 상태로 생성된다`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState != null }
        sendEvent(CollectionEvent.OnNewGroupClick)
        composeTestRule.runOnIdle {
            val form = checkNotNull(currentState).newGroupForm
            assertEquals("", form?.name)
            assertNull(form?.colorArgb)
            assertEquals(false, form?.isDuplicate)
        }
    }

    @Test
    fun `OnSubmitNewGroup 성공 시 currentGroupId 가 새 id 로 바뀌고 form 이 닫힌다`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState != null }
        sendEvent(CollectionEvent.OnNewGroupClick)
        sendEvent(CollectionEvent.OnNewGroupNameChange("Workout"))
        sendEvent(CollectionEvent.OnNewGroupColorSelect(0xFFFFB400.toInt()))
        sendEvent(CollectionEvent.OnSubmitNewGroup)
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.newGroupForm == null && currentState?.currentGroupId != DEFAULT_GROUP_ID
        }
        composeTestRule.runOnIdle {
            val s = checkNotNull(currentState)
            // 생성된 non-default 그룹의 id 와 currentGroupId 가 일치 — fake 의 id 정책에 결합되지 않음
            val newId = s.groups.first { it.id != DEFAULT_GROUP_ID }.id
            assertEquals(newId, s.currentGroupId)
        }
    }

    @Test
    fun `OnSubmitNewGroup - 이름 비어있으면 form 유지하고 그룹 미생성`() = runTest {
        launchPresenter(buildPresenter())
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState != null }
        sendEvent(CollectionEvent.OnNewGroupClick)
        sendEvent(CollectionEvent.OnNewGroupColorSelect(0xFFFFB400.toInt()))
        // 이름 입력 없음
        sendEvent(CollectionEvent.OnSubmitNewGroup)
        composeTestRule.runOnIdle {
            val s = checkNotNull(currentState)
            assertNotNull(s.newGroupForm)
            assertEquals(1, s.groups.size) // Default 만 존재
        }
    }

    @Test
    fun `OnNewGroupNameChange 가 기존 그룹과 normalize 충돌이면 isDuplicate true`() = runTest {
        val groupRepo = FakeGroupRepository().apply { add("Workout", 0xFFFFB400.toInt()) }
        launchPresenter(buildPresenter(groupRepo = groupRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.size == 2
        }
        sendEvent(CollectionEvent.OnNewGroupClick)
        sendEvent(CollectionEvent.OnNewGroupNameChange("  WORKOUT  "))
        composeTestRule.runOnIdle {
            assertEquals(true, checkNotNull(currentState).newGroupForm?.isDuplicate)
        }
    }

    // ── 그룹 삭제 흐름 ────────────────────────────────────────────────────────────

    @Test
    fun `OnRequestDeleteGroup 카드가 1개 이상이면 pendingDeleteGroupId 가 세팅된다`() = runTest {
        val groupRepo = FakeGroupRepository()
        val workoutId = groupRepo.add("Workout", 0xFFFFB400.toInt())
        val collectionRepo = FakeCollectionRepository().apply {
            add(fakeCollectedCard("a", groupId = workoutId))
        }
        launchPresenter(buildPresenter(groupRepo = groupRepo, collectionRepo = collectionRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.countsByGroupId?.get(workoutId) == 1
        }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(workoutId))
        composeTestRule.runOnIdle {
            assertEquals(workoutId, checkNotNull(currentState).pendingDeleteGroupId)
        }
    }

    @Test
    fun `OnConfirmDeleteGroup 으로 pending 그룹 제거 + pending 클리어 + 현재 그룹 폴백`() = runTest {
        val groupRepo = FakeGroupRepository()
        val workoutId = groupRepo.add("Workout", 0xFFFFB400.toInt())
        val collectionRepo = FakeCollectionRepository().apply {
            add(fakeCollectedCard("a", groupId = workoutId))
        }
        launchPresenter(buildPresenter(groupRepo = groupRepo, collectionRepo = collectionRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.countsByGroupId?.get(workoutId) == 1
        }
        sendEvent(CollectionEvent.OnSelectGroup(workoutId))
        composeTestRule.waitUntil(timeoutMillis = 3_000) { currentState?.currentGroupId == workoutId }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(workoutId))
        sendEvent(CollectionEvent.OnConfirmDeleteGroup)
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.groups?.none { it.id == workoutId } == true &&
                currentState?.pendingDeleteGroupId == null
        }
        composeTestRule.runOnIdle {
            assertEquals(DEFAULT_GROUP_ID, checkNotNull(currentState).currentGroupId)
        }
    }

    @Test
    fun `OnCancelDeleteGroup 으로 pendingDeleteGroupId 가 클리어된다`() = runTest {
        val groupRepo = FakeGroupRepository()
        val workoutId = groupRepo.add("Workout", 0xFFFFB400.toInt())
        val collectionRepo = FakeCollectionRepository().apply {
            add(fakeCollectedCard("a", groupId = workoutId))
        }
        launchPresenter(buildPresenter(groupRepo = groupRepo, collectionRepo = collectionRepo))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.countsByGroupId?.get(workoutId) == 1
        }
        sendEvent(CollectionEvent.OnRequestDeleteGroup(workoutId))
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.pendingDeleteGroupId == workoutId
        }
        sendEvent(CollectionEvent.OnCancelDeleteGroup)
        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).pendingDeleteGroupId)
        }
    }
}
