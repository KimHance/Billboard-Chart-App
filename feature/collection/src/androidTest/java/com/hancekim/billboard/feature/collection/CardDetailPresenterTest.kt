package com.hancekim.billboard.feature.collection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.repository.FakeCollectionRepository
import com.hancekim.billboard.core.domain.GetCollectedCardFlowUseCase
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
    private var currentState: CardDetailState? = null

    private val testCardKey = "TestTitle::TestArtist"

    @Before
    fun setUp() {
        fakeRepository = FakeCollectionRepository()
        fakeNavigator = FakeNavigator(BillboardScreen.CardDetail(testCardKey))
    }

    private fun createPresenter(cardKey: String = testCardKey) = CardDetailPresenter(
        navigator = fakeNavigator,
        screen = BillboardScreen.CardDetail(cardKey),
        getCollectedCardFlowUseCase = GetCollectedCardFlowUseCase(fakeRepository),
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
    fun 카드가_존재하면_state에_반영된다() = runTest {
        fakeRepository.add(createFakeCard(testCardKey))

        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.card != null
        }

        val card = checkNotNull(currentState).card
        assertNotNull(card)
        assertEquals(testCardKey, card?.key)
    }

    @Test
    fun 카드가_없으면_state_card는_null이다() {
        launchPresenter("nonexistent_key")

        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).card)
        }
    }

    // ── 닫기 ────────────────────────────────────────────────────────────────────

    @Test
    fun OnCloseClick으로_navigator_pop이_호출된다() = runTest {
        fakeRepository.add(createFakeCard(testCardKey))
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
    fun OnRemoveClick으로_카드가_삭제되고_pop이_호출된다() = runTest {
        fakeRepository.add(createFakeCard(testCardKey))
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

private fun createFakeCard(key: String) = com.hancekim.billboard.core.data.model.CollectedCard(
    key = key,
    title = "TestTitle",
    artist = "TestArtist",
    albumArtUrl = "",
    collectedAt = System.currentTimeMillis(),
    lastWeek = 5,
    peakPosition = 3,
    weeksOnChart = 10,
)
