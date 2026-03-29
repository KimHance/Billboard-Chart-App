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

## Domain Models
- `Chart` — single chart entry (rank, title, artist, etc.)
- `ChartOverview` — `topTen: List<Chart>` + `chartList: List<Chart>`
- `YoutubeVideoDetail` — `videoId`, `thumbnailUrl`, `isPlayable`
- `AppTheme` — `Dark | Light | System`
- `AppFont` — `App | System`

## Rules
- All UseCases are `suspend operator fun invoke()` style — single public method, no parameters except those injected via constructor.
  - Exception: `GetYoutubeVideoDetailUseCase(title, artist)` takes call-site parameters.
- UseCases only `@Inject` constructor — **never** `@HiltViewModel` or `@AndroidEntryPoint`.
- Data models in `:core:data` are mapped to domain models via `mapper/` extension functions — **never** expose data-layer models to feature modules.
- `runtimeOnly(projects.core.dataImpl)` is declared in this module's `build.gradle.kts` — do **not** move impl to a direct `implementation` dependency.

## Testing
- Located in `src/test/` (unit tests, no Android emulator required).
- Uses MockK for mocking repositories.
- Uses Turbine for Flow testing.
- Uses `:core:data-test` for fake repository implementations.
