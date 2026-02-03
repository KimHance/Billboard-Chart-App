package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.BillboardChart
import com.hancekim.billboard.core.data.model.BillboardResponse
import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.domain.model.ChartOverview
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ChartUseCaseTest(
    private val chartType: ChartType,
) {
    private lateinit var repository: ChartRepository

    enum class ChartType {
        HOT100, BILLBOARD200, GLOBAL200, ARTIST100
    }

    @Before
    fun setup() {
        repository = mockk()
    }

    private fun stubRepository(response: BillboardResponse) {
        when (chartType) {
            ChartType.HOT100 -> coEvery { repository.getHot100() } returns response
            ChartType.BILLBOARD200 -> coEvery { repository.getBillboard200() } returns response
            ChartType.GLOBAL200 -> coEvery { repository.getGlobal200() } returns response
            ChartType.ARTIST100 -> coEvery { repository.getArtist100() } returns response
        }
    }

    private fun stubRepositoryThrows(exception: Exception) {
        when (chartType) {
            ChartType.HOT100 -> coEvery { repository.getHot100() } throws exception
            ChartType.BILLBOARD200 -> coEvery { repository.getBillboard200() } throws exception
            ChartType.GLOBAL200 -> coEvery { repository.getGlobal200() } throws exception
            ChartType.ARTIST100 -> coEvery { repository.getArtist100() } throws exception
        }
    }

    private suspend fun invokeUseCase(): ChartOverview {
        return when (chartType) {
            ChartType.HOT100 -> GetBillboardHot100UseCase(repository).invoke()
            ChartType.BILLBOARD200 -> GetBillboard200UseCase(repository).invoke()
            ChartType.GLOBAL200 -> GetBillboardGlobal200UseCase(repository).invoke()
            ChartType.ARTIST100 -> GetBillboardArtist100UseCase(repository).invoke()
        }
    }

    @Test
    fun `응답 데이터를 ChartOverview로 변환한다`() = runTest {
        stubRepository(createResponse(date = "2024-01-01", chartCount = 50))

        val result = invokeUseCase()

        assertEquals("2024-01-01", result.date)
        assertEquals(50, result.chartList.size)
    }

    @Test
    fun `topTen은 상위 10개 항목만 포함한다`() = runTest {
        stubRepository(createResponse(chartCount = 100))

        val result = invokeUseCase()

        assertEquals(10, result.topTen.size)
        assertEquals(1, result.topTen.first().rank)
        assertEquals(10, result.topTen.last().rank)
    }

    @Test
    fun `BillboardChart가 Chart 모델로 올바르게 매핑된다`() = runTest {
        val response = BillboardResponse(
            date = "2024-01-01",
            data = listOf(
                BillboardChart(
                    status = "NEW",
                    rank = 1,
                    title = "Test Song",
                    artist = "Test Artist",
                    image = "https://example.com/image.jpg",
                    lastWeek = 0,
                    peakPosition = 1,
                    peakDate = "2024-01-01",
                    debutPosition = 1,
                    debutDate = "2024-01-01",
                    weeksOnChart = 1
                )
            )
        )
        stubRepository(response)

        val result = invokeUseCase()
        val chart = result.chartList.first()

        assertEquals("NEW", chart.status)
        assertEquals(1, chart.rank)
        assertEquals("Test Song", chart.title)
        assertEquals("Test Artist", chart.artist)
        assertEquals("https://example.com/image.jpg", chart.image)
        assertEquals(0, chart.lastWeek)
        assertEquals(1, chart.peakPosition)
        assertEquals("2024-01-01", chart.peakDate)
        assertEquals(1, chart.debutPosition)
        assertEquals("2024-01-01", chart.debutDate)
        assertEquals(1, chart.weekOnChart)
    }

    @Test
    fun `repository 예외 발생시 예외를 전파한다`() = runTest {
        stubRepositoryThrows(RuntimeException("Network Error"))

        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking { invokeUseCase() }
        }
        assertEquals("Network Error", exception.message)
    }

    private fun createResponse(
        date: String = "",
        chartCount: Int = 10,
    ) = BillboardResponse(
        date = date,
        data = List(chartCount) { index ->
            BillboardChart(
                rank = index + 1,
                title = "Song ${index + 1}",
                artist = "Artist ${index + 1}"
            )
        }
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = ChartType.entries.toList()
    }
}
