# :core:circuit Module

## Responsibility
Central registry of all Circuit screens and shared navigation primitives. This module is the single source of truth for screen definitions.

## Key Files
| File | Role |
|------|------|
| `Screens.kt` | `BillboardScreen` sealed interface — all app screens defined here |
| `PopResult.kt` | `PopResult` sealed interface — typed navigation results |
| `CircuitModule.kt` | Hilt module providing the `Circuit` instance |

## BillboardScreen
```kotlin
sealed interface BillboardScreen : Screen {
    @Parcelize data object Splash : BillboardScreen
    @Parcelize data object Home : BillboardScreen
    @Parcelize data object Setting : BillboardScreen
}
```
Every new screen added to the app **must** be declared here as a `@Parcelize data object`.

## PopResult
```kotlin
sealed interface PopResult : PopResult {
    data object QuitAppResult : PopResult  // triggers forceExit() in MainActivity
}
```
Add a new `PopResult` subtype when a screen needs to communicate a result back to its caller.

## Rules
- **Never** add screen-specific UI or business logic here — this module is a pure contract.
- All feature modules depend on this module via `implementation(projects.core.circuit)`.
- `:app` depends on this module to initialize `Circuit` and set the navigation root.
- Circuit codegen mode is `hilt` (configured in `AndroidCircuitConventionPlugin` via KSP arg).

## Build Configuration
Plugins: `billboard.android.library.compose`, `billboard.android.hilt`, `kotlin-parcelize`.
Dependencies: `api(libs.bundles.circuit)`, `api(libs.circuit.codegen.annotations)` — exposed as `api` so all consumers get the Circuit runtime transitively.
