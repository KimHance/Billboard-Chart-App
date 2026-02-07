package com.hancekim.billboard.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric

@OptIn(ExperimentalMetricApi::class)
object BaselineProfileMetrics {
    val jitCompile = TraceSectionMetric(sectionName = "JIT Compiling %", label = " JIT compilation")
    val classInit = TraceSectionMetric(sectionName = "L%/%;", label = "ClassInit")
    val all = listOf(StartupTimingMetric(), jitCompile, classInit)
}