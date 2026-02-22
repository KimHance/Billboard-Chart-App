package com.hancekim.billboard.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.datatest.repository.FakeChartRepository
import com.hancekim.billboard.core.datatest.repository.FakeYoutubeRepository
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.GetYoutubeVideoDetailUseCase
import com.slack.circuit.test.FakeNavigator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePresenterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeNavigator: FakeNavigator
    private lateinit var presenter: HomePresenter
    private var currentState: HomeState? = null

    @Before
    fun setUp() {
        val fakeChartRepository = FakeChartRepository()
        val fakeYoutubeRepository = FakeYoutubeRepository()

        fakeNavigator = FakeNavigator(BillboardScreen.Home)
        presenter = HomePresenter(
            navigator = fakeNavigator,
            getHot100UseCase = GetBillboardHot100UseCase(fakeChartRepository),
            getArtist100UseCase = GetBillboardArtist100UseCase(fakeChartRepository),
            getGlobal200UseCase = GetBillboardGlobal200UseCase(fakeChartRepository),
            getBillboard200UseCase = GetBillboard200UseCase(fakeChartRepository),
            getYoutubeVideoDetailUseCase = GetYoutubeVideoDetailUseCase(fakeYoutubeRepository),
        )
    }

    private fun launchPresenter() {
        composeTestRule.setContent {
            currentState = presenter.present()
        }
    }

    private fun sendEvent(event: HomeEvent) {
        composeTestRule.runOnIdle { checkNotNull(currentState).eventSink(event) }
    }

    // ── 초기 상태 ──────────────────────────────────────────────────────────────

    @Test
    fun 초기_chartFilter는_BillboardHot100이다() {
        launchPresenter()
        composeTestRule.runOnIdle {
            assertEquals(ChartFilter.BillboardHot100, checkNotNull(currentState).chartFilter)
        }
    }

    @Test
    fun 초기_expandedIndex는_null이다() {
        launchPresenter()
        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).expandedIndex)
        }
    }

    @Test
    fun 초기_showQuitToast는_false이다() {
        launchPresenter()
        composeTestRule.runOnIdle {
            assertFalse(checkNotNull(currentState).showQuitToast)
        }
    }

    // ── 차트 로드 ───────────────────────────────────────────────────────────────

    @Test
    fun 차트_로드_후_chartList가_채워진다() {
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.chartList?.isNotEmpty() == true
        }
        assertTrue(checkNotNull(currentState).chartList.isNotEmpty())
    }

    @Test
    fun 차트_로드_후_topTen이_채워진다() {
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.topTen?.isNotEmpty() == true
        }
        assertTrue(checkNotNull(currentState).topTen.isNotEmpty())
    }

    // ── 필터 변경 ───────────────────────────────────────────────────────────────

    @Test
    fun OnFilterClick_Artist100으로_필터가_변경된다() {
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.chartList?.isNotEmpty() == true
        }

        sendEvent(HomeEvent.OnFilterClick(ChartFilter.Artist100))

        composeTestRule.runOnIdle {
            assertEquals(ChartFilter.Artist100, checkNotNull(currentState).chartFilter)
        }
    }

    @Test
    fun OnFilterClick_Billboard200으로_필터가_변경된다() {
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.chartList?.isNotEmpty() == true
        }

        sendEvent(HomeEvent.OnFilterClick(ChartFilter.Billboard200))

        composeTestRule.runOnIdle {
            assertEquals(ChartFilter.Billboard200, checkNotNull(currentState).chartFilter)
        }
    }

    @Test
    fun OnFilterClick_시_expandedIndex가_초기화된다() {
        launchPresenter()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.chartList?.isNotEmpty() == true
        }

        sendEvent(HomeEvent.OnExpandButtonClick(2))
        composeTestRule.runOnIdle { assertNotNull(checkNotNull(currentState).expandedIndex) }

        sendEvent(HomeEvent.OnFilterClick(ChartFilter.Artist100))

        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).expandedIndex)
        }
    }

    // ── 아이템 확장 ─────────────────────────────────────────────────────────────

    @Test
    fun OnExpandButtonClick으로_해당_인덱스가_확장된다() {
        launchPresenter()

        sendEvent(HomeEvent.OnExpandButtonClick(3))

        composeTestRule.runOnIdle {
            assertEquals(3, checkNotNull(currentState).expandedIndex)
        }
    }

    @Test
    fun 같은_인덱스를_다시_클릭하면_expandedIndex가_null이_된다() {
        launchPresenter()

        sendEvent(HomeEvent.OnExpandButtonClick(3))
        composeTestRule.runOnIdle { assertEquals(3, checkNotNull(currentState).expandedIndex) }

        sendEvent(HomeEvent.OnExpandButtonClick(3))

        composeTestRule.runOnIdle {
            assertNull(checkNotNull(currentState).expandedIndex)
        }
    }

    @Test
    fun 다른_인덱스_클릭_시_expandedIndex가_변경된다() {
        launchPresenter()

        sendEvent(HomeEvent.OnExpandButtonClick(1))
        composeTestRule.runOnIdle { assertEquals(1, checkNotNull(currentState).expandedIndex) }

        sendEvent(HomeEvent.OnExpandButtonClick(5))

        composeTestRule.runOnIdle {
            assertEquals(5, checkNotNull(currentState).expandedIndex)
        }
    }

    // ── 뒤로가기 ────────────────────────────────────────────────────────────────

    @Test
    fun OnBackPressed_첫_번째_호출_시_showQuitToast가_true가_된다() {
        launchPresenter()

        sendEvent(HomeEvent.OnBackPressed)

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.showQuitToast == true
        }
        assertTrue(checkNotNull(currentState).showQuitToast)
    }

    @Test
    fun OnBackPressed_두_번_연속_호출_시_navigator_pop이_호출된다() = runTest {
        launchPresenter()

        sendEvent(HomeEvent.OnBackPressed)
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            currentState?.showQuitToast == true
        }

        sendEvent(HomeEvent.OnBackPressed)

        assertEquals(PopResult.QuitAppResult, fakeNavigator.awaitPop().result)
    }

    // ── 네비게이션 ──────────────────────────────────────────────────────────────

    @Test
    fun OnSettingIconClick으로_Setting_화면으로_이동한다() = runTest {
        launchPresenter()

        sendEvent(HomeEvent.OnSettingIconClick)

        assertEquals(BillboardScreen.Setting, fakeNavigator.awaitNextScreen())
    }
}
