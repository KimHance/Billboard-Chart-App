# :core:design-system Module

## Responsibility
Shared Compose UI component library and theme entry point. Builds on top of `:core:design-foundation`. Provides `BillboardTheme` and all reusable UI components used across feature modules.

## Key Files
| File | Role |
|------|------|
| `BillboardTheme.kt` | Theme entry point — provides `colorScheme` and `typography` via CompositionLocals |
| `StateDiffLogger.kt` | Debug utility — `StateDiffLogEffect` logs state diffs to Logcat |
| `component/header/BillboardHeader.kt` | Top app bar with title; supports leading/trailing icons (Settings, Menu, ArrowBack…) and an optional collection-count badge. `trailingIconContentDescription` overrides the default a11y label for non-Settings trailing icons. |
| `component/carousel/TopCarousel.kt` | Horizontal pager carousel for top-10 |
| `component/carousel/CarouselItem.kt` | Individual carousel card |
| `component/list/RankingList.kt` | Full chart ranking list |
| `component/list/RankingInfo.kt` | Row item with rank, title, artist |
| `component/list/ExpandInfo.kt` | Expandable detail row |
| `component/list/TredingIndicator.kt` | Trending arrow indicator |
| `component/filter/FilterRow.kt` | Chart filter tab row |
| `component/dialog/BillboardAlert.kt` | Shared alert dialog |
| `component/title/TitleSection.kt` | Section title with styling |
| `component/card/HoloCard.kt` | Holographic 3D card (auto-spin + horizontal drag + AGSL shader on API 33+, Compose Canvas fallback below). Caller-tunable `autoSpeed` (default 24f) and `borderRadius`. Reset rotation by wrapping in `key(...) { HoloCard(...) }`. |
| `component/card/HoloCardShader.kt` | AGSL RuntimeShader source for HoloCard's metallic sheen. |
| `component/card/SparkleEffect.kt` | Sparkle/particle overlay used by the Home collect overlay. |
| `component/group/GroupChip.kt` | Pill chip showing a group's name colored by its `colorArgb`. |
| `component/group/GroupDot.kt` | Small filled circle in a group's color. |
| `component/group/GroupDropdown.kt` | Group-selector dropdown (auto-hides the chevron when only the default group exists). |

## BillboardTheme
```kotlin
BillboardTheme(
    isDarkTheme: Boolean,
    isSystemFont: Boolean,
    content: @Composable () -> Unit,
)

// Access in any composable:
BillboardTheme.colorScheme.<token>
BillboardTheme.typography.<style>()
```
**Never** use `MaterialTheme` directly in this project — always use `BillboardTheme`.

## StateDiffLogEffect
A debug composable effect that logs whenever the state object changes. Keep `enabled = true` in development, set to `BuildConfig.DEBUG` before release.
```kotlin
StateDiffLogEffect(state = state, enabled = true, tag = "screen_name")
```

## Rules
- All components must support both light and dark themes — use `BillboardTheme.colorScheme` exclusively.
- New components must include a `@ThemePreviews` preview.
- Components must be stateless — they receive data and `eventSink` as parameters.
- Avoid business logic in components — they render data only.
- `ChartFilter` enum is defined here (in `component/filter/`) since it drives UI behavior.

## Build Configuration
Plugins: `billboard.android.library.compose`.
Depends on `:core:design-foundation` for tokens.
