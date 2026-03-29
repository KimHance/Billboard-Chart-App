# :core:testing Module

## Responsibility
Shared test infrastructure for instrumented (Android) tests. Provides a custom Hilt-aware test runner.

## Key Files
| File | Role |
|------|------|
| `BillboardTestRunner.kt` | Custom `AndroidJUnitRunner` with Hilt support |

## BillboardTestRunner
Declared as `testInstrumentationRunner` in `:app/build.gradle.kts`:
```kotlin
testInstrumentationRunner = "com.hancekim.billboard.core.testing.BillboardTestRunner"
```
Enables Hilt injection in instrumented tests. All `androidTest` configurations must use this runner.

## Rules
- This module is `androidTestImplementation` only — never link it as a runtime dependency.
- Do **not** add test data (fakes/stubs) here — those belong in `:core:data-test`.

## Build Configuration
Plugins: `billboard.android.library`.
