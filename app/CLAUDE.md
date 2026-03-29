# :app Module

## Responsibility
Application entry point. Owns `MainActivity`, `MainViewModel`, Circuit initialization, and global theme/font wiring. Contains no business logic.

## Key Files
- `MainActivity.kt` — single Activity; installs splash screen, sets up Circuit `NavigableCircuitContent`, observes `AppTheme`/`AppFont` from `MainViewModel`, wires edge-to-edge system bars.
- `MainViewModel.kt` — collects `AppTheme` and `AppFont` flows from domain UseCases.
- `MainApplication.kt` — Hilt app entry point, Timber initialization.

## Rules
- This module must **not** contain any business logic.
- `Circuit` instance is injected via Hilt (`@Inject lateinit var circuit: Circuit`) — never constructed manually.
- Navigation root is always `BillboardScreen.Splash`. Do **not** change the root screen without updating splash logic.
- Back-press handling that exits the app uses `PopResult.QuitAppResult` → `forceExit()` (`finishAndRemoveTask()` + `exitProcess(0)`).
- System bar style reacts to `AppTheme` via `enableEdgeToEdge()` — always use `SystemBarStyle` constants, never hardcode colors here.
- `testTagsAsResourceId = true` is set on the root `Surface` — required for UI Automator tests in `:benchmark`.

## Build Configuration
- Plugins: `billboard.android.application`, `billboard.android.application.compose`, `billboard.android.hilt`, `baselineprofile`.
- Build type `benchmark` extends `release` with debug signing — do **not** modify this.
- Baseline Profile: `automaticGenerationDuringBuild = false`, `dexLayoutOptimization = true`.
- Test runner: `BillboardTestRunner` from `:core:testing`.

## Dependencies
- `:core:circuit` — screens and Circuit instance
- `:core:design-system` — `BillboardTheme`
- `:core:domain` — `GetAppFontFlowUseCase`, `GetAppThemeFlowUseCase`
- `:core:image-loader` — Coil initialization
- `:feature:splash`, `:feature:home`, `:feature:setting`
- `androidTestImplementation`: `:core:testing`, `:core:data-test`
