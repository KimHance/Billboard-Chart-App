# Architecture & Module Boundaries

## Module Graph (dependency direction: → means "depends on")

```
:app
  → :feature:home, :feature:splash, :feature:setting, :feature:collection
  → :core:circuit, :core:design-system, :core:image-loader, :core:domain

:feature:*
  → :core:circuit, :core:design-system, :core:domain   (auto-injected by feature plugin)
  → :core:player                                       (home only)

:core:domain
  → :core:data (interfaces)
  runtimeOnly → :core:data-impl

:core:data-impl
  → :core:data, :core:data-source

:core:data-source
  → :core:data
  prodImplementation → :core:network

:core:design-system → :core:design-foundation
:core:testing, :core:data-test — test utilities only
```

## Hard Rules
- Feature modules MUST NOT depend on `:core:data` directly. Use `:core:domain` UseCases.
- `:core:data-impl` is consumed via `runtimeOnly` from `:core:domain` — never as `implementation` in feature modules.
- No reverse dependencies (e.g., `:core:design-foundation` MUST NOT depend on `:core:design-system`).
- No `feature ↔ feature` dependencies. Cross-feature data flows through `:core:domain`.
- `:core:data-test` is `androidTestImplementation` only — never a runtime production dependency.

## Convention Plugins (always use these — never configure manually)

| Plugin alias | Use for |
|---|---|
| `billboard.android.application` | `:app` only |
| `billboard.android.application.compose` | `:app` Compose setup |
| `billboard.android.library` | All library modules |
| `billboard.android.library.compose` | Library modules with Compose |
| `billboard.android.feature` | All `:feature:*` modules (auto-adds `:core:design-system`, `:core:circuit`, `:core:domain`, lifecycle, coroutines, Timber) |
| `billboard.android.hilt` | Any module with Hilt DI |
| `billboard.android.room` | Modules using Room (currently `:core:data-source`) |
| `billboard.circuit` | Any module with Circuit screens |
| `billboard.jvm.library` | Pure Kotlin/JVM modules |
| `billboard.android.test` | `:benchmark` |

## Product Flavors
- `prod` — production build, real network/DB DataSources
- `demo` — demo build (`.demo` app ID suffix), in-memory DataSources

Use `prodImplementation` / `demoImplementation` in `build.gradle.kts` when a dependency must be flavor-specific.

## Version Catalog
All dependency versions are declared in `/gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts` files.

## Adding a New Feature Module
1. Create directory `feature/<name>/`.
2. `build.gradle.kts` plugins: `billboard.android.feature`, `billboard.android.hilt`, `billboard.circuit`.
3. Add `include(":feature:<name>")` to `settings.gradle.kts`.
4. Add `@Parcelize data object <Name> : BillboardScreen` to `Screens.kt` in `:core:circuit`.
5. Create `<Name>State.kt`, `<Name>Presenter.kt`, `<Name>Ui.kt`.
6. Add module dependency in `:app:build.gradle.kts`.

## Adding a New Core Module
1. Choose the appropriate convention plugin.
2. One responsibility per module.
3. Prefer `implementation()`. Use `api()` only when consumers genuinely need transitive access.
