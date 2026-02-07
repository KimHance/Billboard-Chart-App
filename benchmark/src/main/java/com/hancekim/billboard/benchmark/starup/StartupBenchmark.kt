package com.hancekim.billboard.benchmark.starup

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.hancekim.billboard.benchmark.BaselineProfileMetrics
import com.hancekim.billboard.benchmark.PACKAGE_NAME
import com.hancekim.billboard.benchmark.startActivityAndWaitForMain
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()


    @Test
    fun startupWithPartialCompilationAndDisabledBaselineProfile() = startup(
        CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Disable,
            warmupIterations = 1
        ),
    )

    @Test
    fun startupPrecompiledWithBaselineProfile() =
        startup(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    @Test
    fun startupFullyPrecompiled() = startup(CompilationMode.Full())

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = BaselineProfileMetrics.all,
        compilationMode = compilationMode,
        iterations = 10,
        startupMode = StartupMode.COLD,
        setupBlock = MacrobenchmarkScope::pressHome,
        measureBlock = MacrobenchmarkScope::startActivityAndWaitForMain
    )
}