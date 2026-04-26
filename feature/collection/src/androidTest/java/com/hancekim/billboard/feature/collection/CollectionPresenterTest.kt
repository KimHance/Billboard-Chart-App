package com.hancekim.billboard.feature.collection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.fixture.fakeCollectedCard
import com.hancekim.billboard.core.datatest.repository.FakeCollectionRepository
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.slack.circuit.test.FakeNavigator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionPresenterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeNavigator: FakeNavigator
    private lateinit var fakeRepository: FakeCollectionRepository
    private lateinit var presenter: CollectionPresenter
    private var currentState: CollectionState? = null

    @Before
    fun setUp() {
        fakeRepository = FakeCollectionRepository()
        fakeNavigator = FakeNavigator(BillboardScreen.Collection)
        presenter = CollectionPresenter(
            navigator = fakeNavigator,
            getCollectionFlowUseCase = GetCollectionFlowUseCase(fakeRepository),
        )
    }

    private fun launchPresenter() {
        composeTestRule.setContent {
            currentState = presenter.present()
        }
    }

    private fun sendEvent(event: CollectionEvent) {
        composeTestRule.runOnIdle { checkNotNull(currentState).eventSink(event) }
    }

    // ── 초기 상태 ──────────────────────────────────────────────────────────────

    @Test
    fun 초기_cards는_비어있다() {
        launchPresenter()
        composeTestRule.runOnIdle {
            assertTrue(checkNotNull(currentState).cards.isEmpty())
        }
    }

    // ── 컬렉션 데이터 로드 ──────────────────────────────────────────────────────

    @Test
    fun repository에_카드가_있으면_state에_반영된다() = runTest {
        fakeRepository.add(fakeCollectedCard("key1"))
        fakeRepository.add(fakeCollectedCard("key2"))

        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.cards?.size == 2
        }
        assertEquals(2, checkNotNull(currentState).cards.size)
    }

    // ── 네비게이션 ──────────────────────────────────────────────────────────────

    @Test
    fun OnBackClick으로_navigator_pop이_호출된다() = runTest {
        launchPresenter()

        sendEvent(CollectionEvent.OnBackClick)

        fakeNavigator.awaitPop()
    }

    @Test
    fun OnCardClick으로_CardDetail_화면으로_이동한다() = runTest {
        launchPresenter()

        sendEvent(CollectionEvent.OnCardClick("test_key"))

        assertEquals(
            BillboardScreen.CardDetail("test_key"),
            fakeNavigator.awaitNextScreen(),
        )
    }
}
