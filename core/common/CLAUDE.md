# :core:common Module

## Responsibility
Shared Kotlin utilities used across modules. Provides coroutine helpers and other pure Kotlin utilities that don't fit in a domain-specific module.

## Rules
- No Android framework types — this module must remain pure Kotlin where possible.
- If an Android-specific utility is needed, consider `:core:design-foundation` instead.
- Hilt is available (`billboard.android.hilt`) — use only for infrastructure-level DI, not business logic.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`.
Dependency: `kotlinx.coroutines.core`.
