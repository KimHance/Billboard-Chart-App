# :core:data-impl Module

## Responsibility
Repository implementations. Implements the interfaces from `:core:data` by delegating to DataSources from `:core:data-source`. Also provides Hilt bindings that wire interfaces to implementations.

## Structure
```
data-impl/
  repository/
    ChartRepositoryImpl.kt
    YoutubeRepositoryImpl.kt
    PreferenceRepositoryImpl.kt
    CollectionRepositoryImpl.kt
  di/
    RepositoryModule.kt   ← @Binds Hilt module
```

Repositories delegate to DataSources from `:core:data-source` (including Room-backed `CollectionDataSource` in the `prod` flavor). This module never applies the Room plugin.

## Rules
- Each `*RepositoryImpl` takes its corresponding `*DataSource` as a constructor parameter.
- Implementations are thin delegators — no business logic here (that belongs in `:core:domain`).
- `RepositoryModule.kt` uses `@Binds` (not `@Provides`) for all repository bindings.
- This module is consumed via `runtimeOnly(projects.core.dataImpl)` in `:core:domain` — it must **never** be a direct `implementation` dependency of feature modules.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`.
