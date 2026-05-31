# :core:domain Module

## Responsibility
Business logic layer. Contains UseCases that orchestrate data access and apply domain transformations. Has zero Android framework dependencies — only `javax.inject`.

## Structure
```
domain/
  Get<Resource>UseCase.kt      ← read UseCases
  Update<Resource>UseCase.kt   ← write UseCases
  mapper/
    <Resource>Mapper.kt        ← extension functions: data model → domain model
  model/
    <Resource>.kt              ← domain model classes (pure Kotlin data classes)
```

## Current UseCases
| UseCase | Description |
|---------|-------------|
| `GetBillboardHot100UseCase` | Fetches Hot 100 chart → `ChartOverview` |
| `GetBillboardArtist100UseCase` | Fetches Artist 100 chart → `ChartOverview` |
| `GetBillboardGlobal200UseCase` | Fetches Global 200 chart → `ChartOverview` |
| `GetBillboard200UseCase` | Fetches Billboard 200 chart → `ChartOverview` |
| `GetYoutubeVideoDetailUseCase` | Searches YouTube by title+artist → `YoutubeVideoDetail` |
| `GetAppThemeFlowUseCase` | Returns `Flow<AppTheme>` from DataStore |
| `GetAppFontFlowUseCase` | Returns `Flow<AppFont>` from DataStore |
| `UpdateAppThemeUseCase` | Persists `AppTheme` to DataStore |
| `UpdateAppFontUseCase` | Persists `AppFont` to DataStore |
| `GetCollectionFlowUseCase` | `Flow<List<CollectedCard>>` of all collected cards |
| `GetCollectedCardFlowUseCase(key)` | `Flow<CollectedCard?>` for a single card key |
| `IsCollectedUseCase(key)` | `Flow<Boolean>` — whether the given track key is in the collection |
| `AddToCollectionUseCase(card, groupId)` | Persists a card to a group |
| `RemoveFromCollectionUseCase(key)` | Removes a single card by key |
| `RemoveAllFromCollectionUseCase` | Clears the entire collection |
| `GetGroupsFlowUseCase` | `Flow<List<Group>>` — ordered group list |
| `AddGroupUseCase(name, colorArgb)` | Validates + persists a new group, returns `Result<Long>` (new group id). Validation errors typed via `GroupValidationError` |
| `RemoveGroupUseCase(id)` | Removes a non-default group (and cascade-deletes its cards) |

## Domain Models
- `Chart` — single chart entry (rank, title, artist, etc.)
- `ChartOverview` — `topTen: List<Chart>` + `chartList: List<Chart>`
- `YoutubeVideoDetail` — `videoId`, `thumbnailUrl`, `isPlayable`
- `AppTheme` — `Dark | Light | System`
- `AppFont` — `App | System`
- `CollectedCard` — `key`, `title`, `artist`, `albumArtUrl`, `collectedAt`, chart stats, plus `groupId`

## Validation
- `GroupValidationError` is a sealed type (`EmptyName`, `Duplicate`, …) returned inside `AddGroupUseCase`'s `Result.failure`. Presenters surface these through state (e.g. `NewGroupFormState.isDuplicate`) rather than user-facing exception text.

## Rules
- All UseCases are `suspend operator fun invoke()` (or `operator fun invoke(): Flow<…>` for read-streams) — single public method, no parameters except those injected via constructor.
  - Exceptions for call-site params: `GetYoutubeVideoDetailUseCase(title, artist)`, `GetCollectedCardFlowUseCase(key)`, `IsCollectedUseCase(key)`, `AddToCollectionUseCase(card, groupId)`, `RemoveFromCollectionUseCase(key)`, `AddGroupUseCase(name, colorArgb)`, `RemoveGroupUseCase(id)`.
- UseCases only `@Inject` constructor — **never** `@HiltViewModel` or `@AndroidEntryPoint`.
- Data models in `:core:data` are mapped to domain models via `mapper/` extension functions — **never** expose data-layer models to feature modules.
- `runtimeOnly(projects.core.dataImpl)` is declared in this module's `build.gradle.kts` — do **not** move impl to a direct `implementation` dependency.

## Testing
- Located in `src/test/` (unit tests, no Android emulator required).
- Uses MockK for mocking repositories.
- Uses Turbine for Flow testing.
- Uses `:core:data-test` for fake repository implementations.
