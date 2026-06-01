package com.hancekim.billboard.core.circuit

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

sealed interface BillboardScreen : Screen {
    @Parcelize
    data object Splash : BillboardScreen

    @Parcelize
    data object Home : BillboardScreen

    @Parcelize
    data object Setting : BillboardScreen

    @Parcelize
    data object Collection : BillboardScreen

    @Parcelize
    data class CardDetail(val cardKey: String) : BillboardScreen

    // 테스트용 — 외부 LLM(Gemini REST)이 인앱 AppFunction 데이터를 사용해 채팅으로 응답.
    @Parcelize
    data object AgentChat : BillboardScreen
}