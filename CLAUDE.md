# Billboard — Claude Code Instructions

## Language Rules
- All conversational responses from Claude must be in Korean (한국어).
- All documentation and config files (CLAUDE.md, README, etc.) must be written in English.
- All code comments must be in Korean (한국어).

---

## Project Overview

Billboard is an Android app that displays Billboard chart data (Hot 100, Billboard 200, Global 200, Artist 100), plays YouTube videos for the selected track, and lets users collect tracks as holographic cards.

**Package root:** `com.hancekim.billboard`
**Min SDK:** 32 | **Compile/Target SDK:** 36

---

## Quick Navigation
- **Cross-cutting rules (auto-imported):** [.claude/rules/](.claude/rules/) — `01-architecture` … `07-design-system`
- **Module responsibilities:** each module directory's `CLAUDE.md` (auto-loaded when entering that path)
- **Automation:** [.github/workflows/](.github/workflows/) — `claude-review.yml`, `auto-release.yml`
- **Skills:** `pr-review`, `create-release-pr`, `sync-develop`, `generate-baseline-profile`
- **Subagents:** `billboard-reviewer`, `compose-reviewer`, `module-boundary-checker`

---

## Rules

@.claude/rules/01-architecture.md
@.claude/rules/02-circuit.md
@.claude/rules/03-compose-state.md
@.claude/rules/04-di-hilt.md
@.claude/rules/05-error-handling.md
@.claude/rules/06-testing.md
@.claude/rules/07-design-system.md

---

## Module Reference

| Module | Role |
|--------|------|
| `:app` | Application entry point, Circuit setup, theme/font wiring |
| `:core:circuit` | `BillboardScreen` sealed interface, `CircuitModule`, `PopResult` |
| `:core:domain` | UseCases (no framework deps, only `javax.inject`) |
| `:core:data` | Repository interfaces, data models, DataStore serializers |
| `:core:data-impl` | Repository implementations (Hilt bindings) |
| `:core:data-source` | DataSource interfaces + prod/demo flavor implementations (Room for `CollectionDataSource` in prod) |
| `:core:network` | Retrofit, OkHttp, `ResultCall` / `ResultCallAdapter` |
| `:core:design-foundation` | Color tokens, typography tokens, icons, modifiers, utilities |
| `:core:design-system` | `BillboardTheme`, shared Compose components |
| `:core:player` | `PlayerState`, `PlayerController`, `YoutubePlayer`, PiP support |
| `:core:image-loader` | Coil setup (GIF, SVG, network) |
| `:core:common` | Shared coroutine utilities |
| `:core:testing` | `BillboardTestRunner`, shared test helpers |
| `:core:data-test` | Fake repositories + fixture factories for tests |
| `:feature:home` | Home screen — chart list, YouTube player, PiP |
| `:feature:splash` | Splash screen |
| `:feature:setting` | Settings screen — theme and font preferences |
| `:feature:collection` | Card collection (orbit layout) + card detail |
| `:benchmark` | Macrobenchmark + Baseline Profile generation |
| `:build-logic` | Convention plugins for all modules |

Each module has its own `CLAUDE.md` describing its specific responsibility. Refer to those for module-internal details.

---

## Common Commands

| Task | Command |
|---|---|
| Build (prod debug) | `./gradlew assembleProdDebug` |
| Build (demo debug) | `./gradlew assembleDemoDebug` |
| Unit tests (domain) | `./gradlew :core:domain:test` |
| Instrumented tests (feature) | `./gradlew :feature:<name>:connectedDemoDebugAndroidTest` |
| Generate Baseline Profile | `./gradlew :app:generateBaselineProfile` (or `generate-baseline-profile` skill) |
| Open release PR | `create-release-pr` skill |
| Sync develop after merge | `sync-develop` skill |

---

## Workflow

```
Feature work
  develop → feature/<name> → PR → squash merge → develop

Release
  develop → release/v<X.Y.Z>   (create-release-pr skill)
        → PR (release label auto-attached)   (claude-review.yml runs auto-review)
        → squash merge → main
        → auto-release.yml runs: tag push + Baseline Profile + APK + GitHub Release
        → sync-develop skill: reset develop ← main, delete release branch
```

Tag push and APK build are handled automatically by `auto-release.yml` on PR merge — no manual tag push required.

---

## Tech Stack (reference)

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material3 |
| Presentation | Slack Circuit (UDF) |
| DI | Hilt |
| Navigation | Circuit `NavigableCircuitContent` + `BackStack` |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Local Storage | DataStore Preferences, Room (in `:core:data-source` prod) |
| Image Loading | Coil |
| Video Playback | AndroidYouTubePlayer |
| Animation | Lottie |
| Async | Kotlin Coroutines |
| Collections | kotlinx-collections-immutable |
| Logging | Timber |
| Performance | Baseline Profile + Macrobenchmark |
| Code Gen | KSP, KotlinPoet, Circuit codegen (Hilt mode) |
| Testing | JUnit4, MockK, Turbine, Espresso, UIAutomator |
