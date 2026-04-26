# Testing

## Test Locations
| Type | Location | Stack |
|---|---|---|
| Unit (domain, data) | `src/test/` | JUnit4, MockK, Turbine |
| Instrumented (feature UI, Presenter) | `src/androidTest/` | Circuit test utils + Hilt + Compose UI test |
| Macrobenchmark | `:benchmark/src/main/` | Macrobenchmark + UIAutomator |

## Required
- Test runner: `BillboardTestRunner` (from `:core:testing`).
- Presenter tests: use `circuit-test`'s `Presenter.test { }` builder with `FakeNavigator`.
- Use `:core:data-test` for fake repositories — never instantiate `Fake<X>Repository` directly outside this module.
- Use fixture factories from `:core:data-test/fixture/` (e.g., `fakeCollectedCard("key")`) — **never** import `:core:data` models in test code directly.

## Module Build Configuration
- Feature modules use `androidTestImplementation(projects.core.dataTest)` — `:core:data` interfaces are exposed transitively via `api(projects.core.data)` in `:core:data-test`.
- Do **not** declare `androidTestImplementation(projects.core.data)` in feature modules; `:core:data-test` already provides it.

## Conventions
- Test names in Korean backticks are encouraged for readability: `\`OnRemoveClick으로 navigator pop이 호출된다\`()`.
- Each Presenter test file mirrors its target Presenter (e.g., `CollectionPresenterTest`).
- Use `composeTestRule.waitUntil { ... }` instead of `Thread.sleep`.

## Running Locally
```bash
./gradlew :feature:<name>:connectedDemoDebugAndroidTest   # instrumented
./gradlew :core:domain:test                               # unit
./gradlew :app:generateBaselineProfile                    # baseline profile
```
