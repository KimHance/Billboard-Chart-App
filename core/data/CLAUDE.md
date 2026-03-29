# :core:data Module

## Responsibility
Data layer contracts. Defines repository interfaces, data models (DTOs), DataStore serializers, and exception types. Contains **no** implementations — only interfaces and models.

## Structure
```
data/
  repository/
    ChartRepository.kt       ← Billboard chart data
    YoutubeRepository.kt     ← YouTube search + video info
    PreferenceRepository.kt  ← App preferences (theme, font)
  model/
    BillboardResponse.kt     ← Chart API response DTO
    Chart.kt                 ← Chart item DTO
    YoutubeSearchResponse.kt ← YouTube search DTO
    VideoListResponse.kt     ← YouTube video info DTO
    YoutubeVideoInfo.kt      ← YouTube video detail DTO
    YoutubeSearchResult.kt   ← YouTube search result DTO
    BillboardPreference.kt   ← DataStore preference model
  datastore/
    serializer/
      BillboardPreferenceSerializer.kt
    di/
      DataStoreModule.kt
  exception/
    YoutubeException.kt
```

## Rules
- This module declares **interfaces only** — zero implementation classes.
- Data models are raw DTOs (serializable). They are mapped to domain models in `:core:domain/mapper/`.
- **Never** import Compose or Android UI types here.
- `DataStoreModule.kt` provides the `DataStore<BillboardPreference>` singleton — this is the only Android-specific code allowed in this module.
- The module is depended on by both `:core:domain` (interfaces) and `:core:data-impl` (implementations).

## Build Configuration
Plugins: `billboard.android.library`.
No Hilt plugin — DI wiring for repositories is in `:core:data-impl`.
DataStore and serialization are provided via `:core:data-source` using `prodImplementation`.
