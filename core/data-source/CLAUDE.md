# :core:data-source Module

## Responsibility
DataSource layer. Provides concrete data access implementations: Retrofit API calls for `prod` flavor and stub/fake implementations for `demo` flavor. Uses product flavors to switch implementations without compile-time coupling.

## Rules
- `prodImplementation` is used for Retrofit and DataStore — these are **not** available in the `demo` flavor.
- Provide a `demo`-flavor stub for every DataSource interface so the app builds and runs without a real backend.
- DataSource interfaces are defined here (or in `:core:data`).
- Do **not** apply domain logic here — DataSources return raw DTOs only.

## Flavor-Specific Files
```
src/main/       ← shared DataSource interfaces
src/prod/       ← Retrofit implementations, real DataStore, Room (CollectionDataSource)
  db/             ← Room Entity / DAO / Database (prod-only)
  di/DatabaseModule.kt ← Room provider
src/demo/       ← in-memory / hardcoded stub implementations
```

## Persistence
- Room belongs to this module (prod flavor only). `CollectionDataSource` interface lives in `src/main/`; the Room-backed implementation lives in `src/prod/`. Demo flavor uses an in-memory `MutableStateFlow` implementation.
- Schema export directory: `core/data-source/schemas/`.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`, `billboard.android.room`.
```kotlin
prodImplementation(projects.core.network)
prodImplementation(libs.retrofit.core)
prodImplementation(libs.androidx.datastore)
```
