package com.hancekim.billboard.core.domain

sealed class GroupValidationError(message: String) : Throwable(message) {
    data object Empty : GroupValidationError("이름을 입력하세요") {
        private fun readResolve(): Any = Empty
    }
    data object TooLong : GroupValidationError("20자 이하로 입력하세요") {
        private fun readResolve(): Any = TooLong
    }
    data object DuplicateName : GroupValidationError("이미 같은 이름의 그룹이 있어요") {
        private fun readResolve(): Any = DuplicateName
    }
}
