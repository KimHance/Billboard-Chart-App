package com.hancekim.billboard.feature.collection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.fixture.fakeCollectedCard
import com.hancekim.billboard.core.datatest.repository.FakeCollectionRepository
import com.hancekim.billboard.core.datatest.repository.FakeGroupRepository
import com.hancekim.billboard.core.domain.GetCollectedCardFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.slack.circuit.test.FakeNavigator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardDetailPresenterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeNavigator: FakeNavigator
    private lateinit var fakeRepository: FakeCollectionRepository
    private lateinit var fakeGroupRepository: FakeGroupRepository
    private var currentState: CardDetailState? = null

    private val testCardKey = "TestTitle::TestArtist"

    @Before
    fun setUp() {
        fakeRepository = FakeCollectionRepository()
        fakeGroupRepository = FakeGroupRepository()
        fakeNavigator = FakeNavigator(BillboardScreen.CardDetail(testCardKey))
    }

    private fun createPresenter(cardKey: String = testCardKey) = CardDetailPresenter(
        navigator = fakeNavigator,
        screen = BillboardScreen.CardDetail(cardKey),
        getCollectedCardFlowUseCase = GetCollectedCardFlowUseCase(fakeRepository),
        getGroupsFlowUseCase = GetGroupsFlowUseCase(fakeGroupRepository),
        removeFromCollectionUseCase = RemoveFromCollectionUseCase(fakeRepository),
    )

    private fun launchPresenter(cardKey: String = testCardKey) {
        val presenter = createPresenter(cardKey)
        composeTestRule.setContent {
            currentState = presenter.present()
        }
    }

    // ── 카드 로드 ──────────────────────────────────────────────────────────────

    @Test
    fun `카드가 존재하면 state 에 반영된다`() = runTest {
        fakeRepository.add(fakeCollectedCard(testCardKey))

        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.card != null
        }

        val card = checkNotNull(currentState).card
        assertNotNull(card)
        assertEquals(testCardKey, card?.key)
    }

    @Test
    fun `카드가 없으면 state card 는 null 이다`() {
        launchPresenter("nonexistent_key")

        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).card)
        }
    }

    // ── 닫기 ────────────────────────────────────────────────────────────────────

    @Test
    fun `OnCloseClick 으로 navigator pop 이 호출된다`() = runTest {
        fakeRepository.add(fakeCollectedCard(testCardKey))
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.card != null
        }

        composeTestRule.runOnIdle {
            checkNotNull(currentState).eventSink(CardDetailEvent.OnCloseClick)
        }

        fakeNavigator.awaitPop()
    }

    // ── 삭제 ────────────────────────────────────────────────────────────────────

    @Test
    fun `OnRemoveClick 으로 카드가 삭제되고 pop 이 호출된다`() = runTest {
        fakeRepository.add(fakeCollectedCard(testCardKey))
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.card != null
        }

        composeTestRule.runOnIdle {
            checkNotNull(currentState).eventSink(CardDetailEvent.OnRemoveClick)
        }

        fakeNavigator.awaitPop()
        assertEquals(0, fakeRepository.count())
    }
}

