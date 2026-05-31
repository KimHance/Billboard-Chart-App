package com.hancekim.billboard.core.domain

// 도메인 계약상의 그룹 검증 실패 — 사용자 노출 메시지는 Presenter 에서 R.string 매핑한다.
sealed class GroupValidationError(message: String) : Throwable(message) {
    data object Empty : GroupValidationError("Group name must not be empty") {
        private fun readResolve(): Any = Empty
    }
    data object TooLong : GroupValidationError("Group name must be 20 characters or fewer") {
        private fun readResolve(): Any = TooLong
    }
    data object DuplicateName : GroupValidationError("Group name already exists") {
        private fun readResolve(): Any = DuplicateName
    }
    data object DefaultGroupNotDeletable : GroupValidationError("Default group cannot be deleted") {
        private fun readResolve(): Any = DefaultGroupNotDeletable
    }
}
