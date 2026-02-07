package com.hancekim.billboard.benchmark.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.hancekim.billboard.benchmark.PACKAGE_NAME
import com.hancekim.billboard.benchmark.startActivityAndWaitForMain
import org.junit.Rule
import org.junit.Test

class StartupBaselineProfile {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
        profileBlock = MacrobenchmarkScope::startActivityAndWaitForMain
    )
}