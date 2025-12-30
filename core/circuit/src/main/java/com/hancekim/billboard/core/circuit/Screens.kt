package com.hancekim.billboard.core.circuit

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

sealed interface BillboardScreen : Screen {
    @Parcelize
    data object Splash : BillboardScreen

    @Parcelize
    data object Home : BillboardScreen
}