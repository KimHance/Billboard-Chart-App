# :core:data-test Module

## Responsibility
Fake/in-memory implementations of `:core:data` repository interfaces for use in tests. Provides predictable, controllable test doubles without network or disk access.

## Rules
- Each fake must implement the corresponding interface from `:core:data`.
- Fakes should expose mutable properties so tests can set up specific return values.
- This module is `androidTestImplementation` only — **never** link it as a runtime production dependency.
- Use `api(projects.core.data)` so consumers get the data interfaces transitively.

## Usage in Tests
```kotlin
// In HiltTestModule, rebind repositories to fakes:
@BindValue val chartRepository: ChartRepository = FakeChartRepository()
```

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`.
```kotlin
api(projects.core.data)
implementation(projects.core.dataImpl)
implementation(libs.hilt.android.testing)
```
