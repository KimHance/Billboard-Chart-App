package com.hancekim.billboard.benchmark

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiObject2Condition
import androidx.test.uiautomator.Until
import com.hancekim.billboard.benchmark.HasChildrenOp.AT_LEAST
import com.hancekim.billboard.benchmark.HasChildrenOp.AT_MOST
import com.hancekim.billboard.benchmark.HasChildrenOp.EXACTLY

val PACKAGE_NAME = buildString {
    append("com.hancekim.billboard")
    append(BuildConfig.APP_FLAVOR_SUFFIX)
}

fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 {
    if (!wait(Until.hasObject(selector), timeout)) {
        throw AssertionError("Element not found on screen in ${timeout}ms (selector=$selector)")
    }

    return findObject(selector)
}

fun UiDevice.flingElementDownUp(element: UiObject2) {
    element.setGestureMargin(displayWidth / 5)
    element.fling(Direction.DOWN)
    waitForIdle()
    element.fling(Direction.UP)
}


fun untilHasChildren(
    childCount: Int = 1,
    op: HasChildrenOp = AT_LEAST,
): UiObject2Condition<Boolean> = object : UiObject2Condition<Boolean>() {
    override fun apply(element: UiObject2): Boolean = when (op) {
        AT_LEAST -> element.childCount >= childCount
        EXACTLY -> element.childCount == childCount
        AT_MOST -> element.childCount <= childCount
    }
}

enum class HasChildrenOp {
    AT_LEAST,
    EXACTLY,
    AT_MOST,
}