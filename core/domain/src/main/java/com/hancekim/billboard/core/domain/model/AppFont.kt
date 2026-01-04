package com.hancekim.billboard.core.domain.model

enum class AppFont(val code: Int) {
    App(0), System(1);

    companion object {
        fun fromCode(code: Int): AppFont =
            AppFont.entries.first { it.code == code }
    }
}