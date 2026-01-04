package com.hancekim.billboard.core.domain.model

enum class AppTheme(val code: Int) {
    Dark(0), Light(1), System(2);

    companion object {
        fun fromCode(code: Int): AppTheme =
            entries.first { it.code == code }
    }
}

