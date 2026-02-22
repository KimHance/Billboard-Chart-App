package com.hancekim.billboard.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.datatest.repository.FakeChartRepository
import com.hancekim.billboard.core.datatest.repository.FakeYoutubeRepository
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.filter.ChartFilter
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.GetYoutubeVideoDetailUseCase
import com.slack.circuit.test.FakeNavigator
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeUiTest {

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

    private fun launchHomeUi() {
        composeTestRule.setContent {
            BillboardTheme {
                currentState = presenter.present()
                HomeUi(state = checkNotNull(currentState))
            }
        }
    }

    private fun sendEvent(event: HomeEvent) {
        composeTestRule.runOnIdle { checkNotNull(currentState).eventSink(event) }
    }

    // ── 기본 요소 ────────────────────────────────────────────────────────────────

    @Test
    fun 홈_화면_기본_요소가_표시된다() {
        launchHomeUi()

        composeTestRule.run {
            onNodeWithTag("home").assertIsDisplayed()
            onNodeWithTag("title_player").assertIsDisplayed()
            onNodeWithTag("filter").assertIsDisplayed()
            onNodeWithTag("chart_list").assertIsDisplayed()
        }
    }

    // ── 플레이어 / PIP ───────────────────────────────────────────────────────────

    @Test
    fun 초기에_player가_표시되고_pip_player는_존재하지_않는다() {
        launchHomeUi()

        composeTestRule.run {
            onNodeWithTag("player").assertIsDisplayed()
            onAllNodesWithTag("pip_player").assertCountEquals(0)
        }
    }

    @Test
    fun 스크롤_후_pip_player가_노출되고_player가_사라진다() {
        launchHomeUi()

        composeTestRule.run {
            waitUntil(timeoutMillis = 3_000) { currentState?.chartList?.isNotEmpty() == true }
            onNodeWithTag("chart_list").performTouchInput { swipeUp() }
            waitForIdle()
            onNodeWithTag("pip_player").assertIsDisplayed()
            onAllNodesWithTag("player").assertCountEquals(0)
        }
    }

    // ── 차트 목록 ────────────────────────────────────────────────────────────────

    @Test
    fun 차트_로드_후_chart_item이_표시된다() {
        launchHomeUi()
        composeTestRule.run {
            waitUntil(timeoutMillis = 3_000) { currentState?.chartList?.isNotEmpty() == true }
            onAllNodesWithTag("chart_item").onFirst().assertIsDisplayed()
        }
    }

    // ── 필터 ─────────────────────────────────────────────────────────────────────

    @Test
    fun 초기_필터는_BillboardHot100이_선택되어있다() {
        launchHomeUi()
        composeTestRule
            .onNode(hasText(ChartFilter.BillboardHot100.text) and isSelectable())
            .assertIsSelected()
    }

    @Test
    fun Artist100_필터_클릭_시_선택_상태가_변경된다() {
        launchHomeUi()

        composeTestRule.run {
            waitUntil(timeoutMillis = 3_000) { currentState?.chartList?.isNotEmpty() == true }
            sendEvent(HomeEvent.OnFilterClick(ChartFilter.Artist100))
            onNode(hasText(ChartFilter.Artist100.text) and isSelectable())
                .assertIsSelected()
        }

    }
}
