# :feature:home Module

## Responsibility
Home screen — the main screen of the app. Displays Billboard chart rankings with a YouTube video player and PiP (picture-in-picture) mode for the selected track.

## Screen Definition
`BillboardScreen.Home` — registered in `:core:circuit`.

## Key Files
| File | Role |
|------|------|
| `HomeState.kt` | `HomeState : CircuitUiState`, `HomeEvent : CircuitUiEvent` |
| `HomePresenter.kt` | Business logic; loads chart data and YouTube video detail |
| `HomeUi.kt` | Top-level `@CircuitInject` composable |
| `IgnoreHorizontalPadding.kt` | Layout modifier for full-bleed sections |
| `component/PlayerWithPager.kt` | YouTube player + chart pager |
| `component/TrendingSection.kt` | Trending (top-10) carousel section |

## State & Events
**Key state fields:**
- `topTen: ImmutableList<Chart>` — top-10 for the active filter
- `chartList: ImmutableList<Chart>` — full chart list for the active filter
- `chartFilter: ChartFilter` — active filter (Hot100 / Billboard200 / Global200 / Artist100)
- `expandedIndex: Int?` — which list item is expanded
- `currentVideo: YoutubeVideoDetail?` — currently playing video
- `playerState: PlayerState` — YouTube player lifecycle state
- `pipState: PipState` — PiP overlay state
- `isPipMode: Boolean` — derived: `listOffsetY > 0 && scrollState >= listOffsetY`

**Events:** `OnFilterClick`, `OnExpandButtonClick`, `OnBackPressed`, `OnSettingIconClick`, `OnListPositioned`, `OnItemClick`

## Presenter Rules
- Uses `produceRetainedState` to load all four chart types simultaneously on first render.
- Video loading triggered by `OnItemClick` or automatically on filter change (first item).
- `loadVideo` is a local lambda — do **not** extract to a separate class.
- Back-press shows a snackbar ("Press back again to exit"); second press fires `PopResult.QuitAppResult`.
- Hilt component: `ActivityRetainedComponent`.

## UI Rules
- Access colors via `BillboardTheme.colorScheme`, typography via `BillboardTheme.typography`.
- Root `Scaffold` uses `containerColor = colorScheme.bgApp`.
- `StateDiffLogEffect` is enabled with tag `"home"` — keep it in place for debug builds.
- Top-level `Scaffold` must have `testTag("home")`.

## Testing
- Presenter tests: `src/androidTest/` using `circuit-test`'s `Presenter.test { }`.
- UI tests: `src/androidTest/` using `HomeUiTest`.
- Inject fakes via `:core:data-test`.

## Dependencies
- `:core:circuit` — `BillboardScreen`, `PopResult`
- `:core:domain` — `GetBillboard*UseCase`, `GetYoutubeVideoDetailUseCase`
- `:core:design-system` — `BillboardTheme`, components
- `:core:player` — `PlayerState`, `PipState`, `YoutubePlayer`
