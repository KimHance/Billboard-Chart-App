# Billboard — Claude Code Instructions

## Language Rules
- All conversational responses from Claude must be in Korean (한국어).
- All documentation and config files (CLAUDE.md, README, etc.) must be written in English.
- All code comments must be in Korean (한국어).

---

## Project Overview

Billboard is an Android app that displays Billboard chart data (Hot 100, Billboard 200, Global 200, Artist 100) and plays YouTube videos for the selected track. The app targets API 32+ and is written entirely in Kotlin with Jetpack Compose.

**Package root:** `com.hancekim.billboard`
**Min SDK:** 32 | **Compile/Target SDK:** 36
**Version:** 1.0.0 (code: 1)

---

## Architecture

### Pattern: Circuit (UDF — Unidirectional Data Flow)
This project uses [Slack Circuit](https://slackhq.github.io/circuit/) as its presentation framework. Every screen follows this exact structure:

```
Screen (sealed interface, Parcelable) — defined in :core:circuit
    ├── State (data class : CircuitUiState) — holds all UI state + eventSink
    ├── Event (sealed interface : CircuitUiEvent) — all user interactions
    ├── Presenter (@CircuitInject, @AssistedInject) — business logic, returns State
    └── Ui (@CircuitInject, @Composable) — renders State, calls eventSink
```

**Never** use ViewModel directly in feature modules. State is managed via `rememberRetained` and `produceRetainedState` inside Presenters.

### Module Graph (dependency direction: → means "depends on")
```
:app
  → :feature:home, :feature:splash, :feature:setting
  → :core:circuit, :core:design-system, :core:image-loader, :core:domain

:feature:*
  → :core:circuit, :core:design-system, :core:domain, :core:player (home only)

:core:domain
  → :core:data (interfaces)
  runtimeOnly → :core:data-impl

:core:data-impl
  → :core:data-source

:core:data-source
  → :core:data (interfaces)
  prodImplementation → :core:network

:core:design-system
  → :core:design-foundation

:core:testing, :core:data-test — test utilities only
```

---

## Module Reference

| Module | Role |
|--------|------|
| `:app` | Application entry point, Circuit setup, theme/font wiring |
| `:core:circuit` | `BillboardScreen` sealed interface, `CircuitModule`, `PopResult` |
| `:core:domain` | UseCases (no framework deps, only `javax.inject`) |
| `:core:data` | Repository interfaces, data models, DataStore serializers |
| `:core:data-impl` | Repository implementations (Hilt bindings) |
| `:core:data-source` | DataSource interfaces + prod/demo flavor implementations (including Room for `CollectionDataSource` in prod) |
| `:core:network` | Retrofit, OkHttp, `ResultCall` / `ResultCallAdapter` |
| `:core:design-foundation` | Color tokens, typography tokens, icons, modifiers, utilities |
| `:core:design-system` | `BillboardTheme`, shared Compose components |
| `:core:player` | `PlayerState`, `PlayerController`, `YoutubePlayer`, PiP support |
| `:core:image-loader` | Coil setup (GIF, SVG, network) |
| `:core:common` | Shared coroutine utilities |
| `:core:testing` | `BillboardTestRunner`, shared test helpers |
| `:core:data-test` | Fake repository implementations for tests |
| `:feature:home` | Home screen — chart list, YouTube player, PiP |
| `:feature:splash` | Splash screen |
| `:feature:setting` | Settings screen — theme and font preferences |
| `:benchmark` | Macrobenchmark + Baseline Profile generation |
| `:build-logic` | Convention plugins for all modules |

---

## Build System

### Convention Plugins (always use these — never configure manually)
| Plugin alias | Class | Use for |
|---|---|---|
| `billboard.android.application` | `AndroidApplicationConventionPlugin` | `:app` only |
| `billboard.android.application.compose` | `AndroidApplicationComposeConventionPlugin` | `:app` Compose setup |
| `billboard.android.library` | `AndroidLibraryConventionPlugin` | All library modules |
| `billboard.android.library.compose` | `AndroidLibraryComposeConventionPlugin` | Library modules with Compose |
| `billboard.android.feature` | `AndroidFeatureConventionPlugin` | All `:feature:*` modules |
| `billboard.android.hilt` | `HiltConventionPlugin` | Any module with Hilt DI |
| `billboard.android.room` | `AndroidRoomConventionPlugin` | Modules using Room |
| `billboard.circuit` | `AndroidCircuitConventionPlugin` | Any module with Circuit screens |
| `billboard.jvm.library` | `JvmLibraryConventionPlugin` | Pure Kotlin/JVM modules |
| `billboard.android.test` | `AndroidTestConventionPlugin` | `:benchmark` |

### Product Flavors
- `prod` — production build, uses real network data sources
- `demo` — demo build, uses fake/stub data sources (`.demo` app ID suffix)

Use `prodImplementation` / `demoImplementation` in `build.gradle.kts` when a dependency must be flavor-specific.

### Version Catalog
All dependency versions are declared in `/gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts` files.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material3 |
| Presentation | Slack Circuit |
| DI | Hilt |
| Navigation | Circuit's `NavigableCircuitContent` + `BackStack` |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Local Storage | DataStore Preferences |
| Database | Room (available, used in data-source) |
| Image Loading | Coil |
| Video Playback | AndroidYouTubePlayer |
| Animation | Lottie |
| Async | Kotlin Coroutines |
| Collections | kotlinx-collections-immutable |
| Logging | Timber |
| Performance | Baseline Profile + Macrobenchmark |
| Code Gen | KSP, KotlinPoet, Circuit codegen (Hilt mode) |
| Testing | JUnit4, MockK, Turbine, Espresso, UIAutomator |

---

## Coding Conventions

### Circuit Screen Pattern
Every new screen must follow this exact file layout:
```
feature/<name>/
  <Name>Screen.kt     ← add to BillboardScreen in :core:circuit
  <Name>State.kt      ← State data class + Event sealed interface
  <Name>Presenter.kt  ← @AssistedInject constructor, @CircuitInject factory
  <Name>Ui.kt         ← @CircuitInject @Composable fun
  component/          ← sub-composables (not injected)
```

### State Management Rules
- Use `rememberRetained { }` for all mutable state in Presenters (survives config changes).
- Use `produceRetainedState` for async data loading (replaces `LaunchedEffect + setState`).
- State classes must be annotated with `@Stable`.
- Use `ImmutableList` / `persistentListOf()` from `kotlinx-collections-immutable` for list state — never `List<T>`.
- `eventSink: (Event) -> Unit` must be the last property in every State class.

### Compose Rules
- All UI entry points (screens) use `@CircuitInject` — not manual factory wiring.
- Access theme via `BillboardTheme.colorScheme` and `BillboardTheme.typography` — never MaterialTheme directly.
- Previews use `@ThemePreviews` from `:core:design-foundation` (covers light/dark).
- Use `testTag()` on top-level screen containers for UI tests; use `testTagsAsResourceId = true` (already set in `MainActivity`).

### Dependency Injection
- Hilt component scope for Presenters: `ActivityRetainedComponent` (via `@CircuitInject`).
- Use `@AssistedInject` + `@AssistedFactory` for Presenter constructors that receive `Navigator`.
- Repository bindings live in `:core:data-impl:di:RepositoryModule`.
- DataSource bindings are flavor-specific; use `prodImplementation` / `demoImplementation`.

### Naming Conventions
- Screens: `<Name>Screen` (Parcelable data object in `BillboardScreen`)
- Presenter: `<Name>Presenter` — implements `Presenter<State>`
- UI: `<Name>Ui` — top-level `@Composable`
- State: `<Name>State` — implements `CircuitUiState`
- Events: `<Name>Event` — implements `CircuitUiEvent`, sealed interface
- UseCase: `Get<Resource>UseCase`, `Update<Resource>UseCase` (verb prefix)
- Repository interface: `<Resource>Repository` in `:core:data`
- Repository impl: `<Resource>RepositoryImpl` in `:core:data-impl`
- DataSource: `<Resource>DataSource` in `:core:data-source`

### Error Handling
- Use `runCatching { }.onSuccess { }.onFailure { }` in Presenters for async operations.
- Show errors via `SnackbarHostState` from state — never `Toast` in feature modules.

### Testing
- **Unit tests** (domain, data): in `src/test/`, use MockK + Turbine.
- **Instrumented tests** (feature UI): in `src/androidTest/`, use Circuit test utils + Hilt.
- Test runner: `BillboardTestRunner` (from `:core:testing`).
- Fake data: use `:core:data-test` which provides in-memory repository fakes.
- Presenter tests: use `circuit-test`'s `Presenter.test { }` builder.

---

## Adding a New Feature Module

1. Create directory `feature/<name>/`.
2. Add `build.gradle.kts` with plugins: `billboard.android.feature`, `billboard.android.library.compose`, `billboard.android.hilt`, `billboard.circuit`.
3. Add `include(":feature:<name>")` to `settings.gradle.kts`.
4. Add `@Parcelize data object <Name> : BillboardScreen` to `Screens.kt` in `:core:circuit`.
5. Create `<Name>State.kt`, `<Name>Presenter.kt`, `<Name>Ui.kt`.
6. Add module dependency in `:app:build.gradle.kts`.

## Adding a New Core Module

1. Choose the appropriate convention plugin.
2. Keep the module focused: one responsibility per module.
3. Prefer `api()` only when consumers genuinely need transitive access; use `implementation()` otherwise.

---

## Performance

- Baseline Profiles are generated via `:benchmark` (demo flavor, Pixel 6 API 32/36).
- Run `./gradlew :app:generateBaselineProfile` to regenerate.
- `dexLayoutOptimization = true` is enabled in `:app`.
- `automaticGenerationDuringBuild = false` — profiles are generated manually.
- Scroll benchmarks exist in `ScrollChartBenchmark` — run before major list UI changes.
