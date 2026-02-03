package com.hancekim.billboard.core.domain

import app.cash.turbine.test
import com.hancekim.billboard.core.datatest.repository.FakePreferenceRepository
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppPreferenceUseCaseTest {

    private lateinit var repository: FakePreferenceRepository
    private lateinit var getAppThemeFlowUseCase: GetAppThemeFlowUseCase
    private lateinit var getAppFontFlowUseCase: GetAppFontFlowUseCase
    private lateinit var updateAppThemeUseCase: UpdateAppThemeUseCase
    private lateinit var updateAppFontUseCase: UpdateAppFontUseCase

    @Before
    fun setup() {
        repository = FakePreferenceRepository()
        getAppThemeFlowUseCase = GetAppThemeFlowUseCase(repository)
        getAppFontFlowUseCase = GetAppFontFlowUseCase(repository)
        updateAppThemeUseCase = UpdateAppThemeUseCase(repository)
        updateAppFontUseCase = UpdateAppFontUseCase(repository)
    }

    @Test
    fun `초기 테마는 Dark이다`() = runTest {
        getAppThemeFlowUseCase().test {
            assertEquals(AppTheme.Dark, awaitItem())
        }
    }

    @Test
    fun `초기 폰트는 App이다`() = runTest {
        getAppFontFlowUseCase().test {
            assertEquals(AppFont.App, awaitItem())
        }
    }

    @Test
    fun `테마를 Light로 변경하면 flow가 Light를 방출한다`() = runTest {
        getAppThemeFlowUseCase().test {
            assertEquals(AppTheme.Dark, awaitItem())

            updateAppThemeUseCase(AppTheme.Light)

            assertEquals(AppTheme.Light, awaitItem())
        }
    }

    @Test
    fun `테마를 System으로 변경하면 flow가 System을 방출한다`() = runTest {
        getAppThemeFlowUseCase().test {
            assertEquals(AppTheme.Dark, awaitItem())

            updateAppThemeUseCase(AppTheme.System)

            assertEquals(AppTheme.System, awaitItem())
        }
    }

    @Test
    fun `테마를 여러번 변경하면 모든 변경이 순서대로 방출된다`() = runTest {
        getAppThemeFlowUseCase().test {
            assertEquals(AppTheme.Dark, awaitItem())

            updateAppThemeUseCase(AppTheme.Light)
            assertEquals(AppTheme.Light, awaitItem())

            updateAppThemeUseCase(AppTheme.System)
            assertEquals(AppTheme.System, awaitItem())

            updateAppThemeUseCase(AppTheme.Dark)
            assertEquals(AppTheme.Dark, awaitItem())
        }
    }

    @Test
    fun `폰트를 System으로 변경하면 flow가 System을 방출한다`() = runTest {
        getAppFontFlowUseCase().test {
            assertEquals(AppFont.App, awaitItem())

            updateAppFontUseCase(AppFont.System)

            assertEquals(AppFont.System, awaitItem())
        }
    }

    @Test
    fun `폰트를 여러번 변경하면 모든 변경이 순서대로 방출된다`() = runTest {
        getAppFontFlowUseCase().test {
            assertEquals(AppFont.App, awaitItem())

            updateAppFontUseCase(AppFont.System)
            assertEquals(AppFont.System, awaitItem())

            updateAppFontUseCase(AppFont.App)
            assertEquals(AppFont.App, awaitItem())
        }
    }
}
