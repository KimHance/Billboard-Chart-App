package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.ChartOverview
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BillboardFunctionsTest {

    private val useCase: GetBillboardHot100UseCase = mockk()
    private val context: AppFunctionContext = mockk(relaxed = true)
    private val sut = BillboardFunctions(useCase)

    @Test
    fun `getCurrentHot100TopSong - rank 1 entry 가 TopSong 으로 매핑된다`() = runTest {
        coEvery { useCase() } returns ChartOverview(
            chartList = listOf(
                Chart(rank = 2, title = "Second", artist = "B"),
                Chart(rank = 1, title = "Espresso", artist = "Sabrina Carpenter"),
                Chart(rank = 3, title = "Third", artist = "C"),
            ),
        )

        val result = sut.getCurrentHot100TopSong(context)

        assertEquals("Espresso", result.title)
        assertEquals("Sabrina Carpenter", result.artist)
        assertEquals(1, result.rank)
    }

}
