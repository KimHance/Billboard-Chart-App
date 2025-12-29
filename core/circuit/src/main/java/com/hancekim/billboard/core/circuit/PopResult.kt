package com.hancekim.billboard.core.circuit

import com.slack.circuit.runtime.screen.PopResult
import kotlinx.parcelize.Parcelize

object PopResult {
    @Parcelize
    data object QuitAppResult : PopResult
}