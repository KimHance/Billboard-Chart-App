package com.hancekim.billboard.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

fun MacrobenchmarkScope.startActivityAndWaitForMain() {
    startActivityAndWait()
    device.wait(Until.gone(By.res("splash")), 3_000)
    val contentList = device.waitAndFindObject(By.res("chart_list"), 3_000)
    contentList.wait(untilHasChildren(), 5_000)
}