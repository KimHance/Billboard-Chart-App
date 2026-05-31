package com.hancekim.billboard.home.component

sealed interface OverlayCollectState {
    data object Uncollected : OverlayCollectState
    data class Collected(val groupId: Long) : OverlayCollectState
}
