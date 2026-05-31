# :core:resource Module

## Responsibility
Single source of truth for **all user-facing strings** in the app. Holds `strings.xml` (and any other shared `res/values/*.xml` user-facing resource), exposes the generated `R` class to consumers.

The app is **English-only**. No localized variants live here, and there is no plan to add any.

## Rules
- This module contains **only `res/`** plus a stub `AndroidManifest.xml`. No Kotlin sources, no Hilt, no Compose.
- Every user-facing literal in the codebase (`Text`, `contentDescription`, snackbar messages, alert dialogs, button labels, etc.) **must** resolve through this module's `R.string`.
- Reuse keys aggressively — duplicate literals are forbidden. If two features need the same wording, share the key (`action_*`, `cd_*`).
- Follow the naming convention defined in `.claude/rules/08-string-resources.md`:
  - `app_*` — brand
  - `action_*` — reusable verbs
  - `cd_*` — accessibility / `contentDescription`
  - `<feature>_*` — feature-scoped strings
- Use positional format arguments (`%1$s`, `%1$d`) for parameterized strings.

## Consumers
- `:core:design-system` declares `api(projects.core.resource)`, so the `R` class flows transitively to every feature module that depends on `:core:design-system` (which is everyone, via the `billboard.android.feature` convention plugin).
- Therefore feature `build.gradle.kts` files **must not** add `implementation(projects.core.resource)` themselves.
- Non-Compose call sites (Presenters) read strings via `context.getString(R.string.*)`.

## Build Configuration
Plugins: `billboard.android.library` only. No Compose / Hilt / Room plugins — this is a pure resource bundle.
```kotlin
plugins {
    alias(libs.plugins.billboard.android.library)
}

android {
    namespace = "com.hancekim.billboard.core.resource"
}
```

## Adding a new string
1. Add the `<string>` entry to `src/main/res/values/strings.xml` with a key that follows the prefix convention.
2. Reference it from the call site as `stringResource(com.hancekim.billboard.core.resource.R.string.<key>, ...)` or `context.getString(...)`.
3. Run `./gradlew :core:resource:assembleDebug` to catch malformed XML / duplicate keys.
