package com.hancekim.billboard.benchmark.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.hancekim.billboard.benchmark.PACKAGE_NAME
import com.hancekim.billboard.benchmark.scrollListUpAndDown
import com.hancekim.billboard.benchmark.startActivityAndWaitForMain
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ScrollChartBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollChartCompilationNone() = scrollChart(CompilationMode.None())

    @Test
    fun scrollChartCompilationBaselineProfile() = scrollChart(CompilationMode.Partial())

    @Test
    fun scrollChartCompilationFull() = scrollChart(CompilationMode.Full())


    private fun scrollChart(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = 10,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWaitForMain()
        },
        measureBlock = MacrobenchmarkScope::scrollListUpAndDown
    )
}