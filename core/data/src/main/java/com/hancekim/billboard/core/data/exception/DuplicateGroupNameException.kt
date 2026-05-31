package com.hancekim.billboard.core.data.exception

// UNIQUE 인덱스 충돌(혹은 동등한 race condition)을 데이터 계층에서 도메인-친화 타입으로 표면화.
// SQLite 의존을 도메인 레이어로 누설하지 않기 위한 어댑터 예외.
class DuplicateGroupNameException(name: String) : Exception("Duplicate group name: $name")
