package com.hancekim.billboard.benchmark.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.hancekim.billboard.benchmark.PACKAGE_NAME
import com.hancekim.billboard.benchmark.scrollListUpAndDown
import com.hancekim.billboard.benchmark.startActivityAndWaitForMain
import org.junit.Rule
import org.junit.Test

// 차트 리스트 스크롤 시 사용되는 코드 경로를 프로파일에 추가
class ScrollBaselineProfile {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = false,
        profileBlock = {
            startActivityAndWaitForMain()
            scrollListUpAndDown()
        }
    )
}
