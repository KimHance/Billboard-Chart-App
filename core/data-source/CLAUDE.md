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
                  (Chart, Collection, Group, Preference, Youtube)
src/prod/       ← Retrofit + real DataStore + Room (Collection + Group)
  db/
    CollectionDatabase.kt
    CollectedCardEntity.kt + CollectionDao.kt
    GroupEntity.kt + GroupDao.kt
  di/DatabaseModule.kt   ← Room provider, seeds the default "Starred" group
                            via `addCallback { onOpen → INSERT OR IGNORE }`
  di/NetworkServiceModule.kt
  di/DataSourceModule.kt ← prod @Binds
src/demo/       ← in-memory / hardcoded stub implementations
  di/DataSourceModule.kt ← demo @Binds (no Room, no Retrofit)
```

## Persistence
- Room belongs to this module (prod flavor only). `CollectionDataSource` and `GroupDataSource` interfaces live in `src/main/`; Room-backed implementations live in `src/prod/`. Demo flavor uses in-memory `MutableStateFlow` implementations seeded with the same default group.
- Database name: `billboard_collection.db`. Two tables: `collected_cards`, `groups`.
- Default group ("Starred", `Group.DEFAULT_ID = 1`, color Green400) is seeded **idempotently** via `RoomDatabase.Callback.onOpen` + `INSERT OR IGNORE` so existing devices that already created the DB still get the seed.
- `fallbackToDestructiveMigration(dropAllTables = true)` is enabled — bump the DB version freely; schemas are not preserved.
- Schema export directory: `core/data-source/schemas/`.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`, `billboard.android.room`.
```kotlin
prodImplementation(projects.core.network)
prodImplementation(libs.retrofit.core)
prodImplementation(libs.androidx.datastore)
```
