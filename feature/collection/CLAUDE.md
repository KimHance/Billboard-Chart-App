# :feature:collection Module

## Responsibility
Collection screen — lets users browse their collected cards by **group**, play the holographic NowPlayingDeck, swap which card is "now playing", and delete cards inline. Also hosts the CardDetail full-screen view.

## Screen Definitions
- `BillboardScreen.Collection` — collection screen with groups sidebar.
- `BillboardScreen.CardDetail(cardKey)` — detail view for a single card (parcelized `data class`).

Both registered in `:core:circuit`.

## Key Files
| File | Role |
|------|------|
| `CollectionState.kt` | `CollectionState : CircuitUiState`, `CollectionEvent : CircuitUiEvent` — groups/cards/sidebar/newGroupForm/pendingDelete state |
| `CollectionPresenter.kt` | Loads groups + cards via flows; group CRUD + card removal + sidebar/new-group form state |
| `CollectionUi.kt` | Top-level `@CircuitInject` composable — Scaffold + `ModalNavigationDrawer` (RTL trick for right-side sidebar) |
| `CardDetailState.kt` | `CardDetailState : CircuitUiState`, `CardDetailEvent : CircuitUiEvent` |
| `CardDetailPresenter.kt` | Loads single card + group via combined flow; removes via `RemoveFromCollectionUseCase` |
| `CardDetailUi.kt` | Top-level `@CircuitInject` composable for card detail |
| `component/NowPlayingDeck.kt` | 224dp `HoloCard` + group glow + chip + title/artist + INSPECT pill + `NowPlayingDeckEmpty` + `CollectionSubline` + `CollectionDivider` |
| `component/MiniRail.kt` | Edge-to-edge `LazyRow` of 100dp thumbnails with always-visible ✕ delete badge; pulsing skeleton in `MiniRailEmpty` |
| `component/GroupSidebar.kt` | Drawer-sheet content — group list, delete confirmation, "+ NEW GROUP" trigger |
| `component/NewGroupForm.kt` | Name input + 5-color palette picker; submits new group |
| `component/NewGroupFormState.kt` | `NewGroupFormState(name, colorArgb, isDuplicate)` |

## State & Events

### CollectionState
- `groups: ImmutableList<Group>`
- `currentGroupId: Long` (`Group.DEFAULT_ID` for "Starred")
- `cardsInCurrentGroup: ImmutableList<CollectedCard>` — filtered + ordered by `collectedAt` desc
- `countsByGroupId: ImmutableMap<Long, Int>` — for badge counts in sidebar
- `nowPlayingKey: String?` — null when current group is empty
- `sidebarOpen: Boolean`
- `newGroupForm: NewGroupFormState?` — non-null while "+ NEW GROUP" form is shown
- `pendingDeleteGroupId: Long?` — non-null while delete confirmation dialog is shown
- **Events:** `OnBackClick`, `OnSelectCard(key)`, `OnRemoveCard(key)`, `OnInspectClick`, `OnSidebarToggle(open)`, `OnSelectGroup(id)`, `OnRequestDeleteGroup(id)`, `OnConfirmDeleteGroup`, `OnCancelDeleteGroup`, `OnNewGroupClick`, `OnCancelNewGroup`, `OnNewGroupNameChange(name)`, `OnNewGroupColorSelect(argb)`, `OnSubmitNewGroup`

### CardDetailState
- `card: CollectedCard?` — `null` while loading or when not found
- `group: Group?` — owning group for the card (for chip coloring)
- **Events:** `OnCloseClick`, `OnRemoveClick`

## Presenter Rules
- Use `produceRetainedState` for groups and cards flows; never `LaunchedEffect` for state population.
- `LaunchedEffect(currentGroupId, cardsInCurrentGroup)` re-validates `nowPlayingKey` on every list change — keying on `cardsInCurrentGroup` (not just the first key) is required so deleting a non-first active card still triggers the fallback.
- `CardDetailPresenter` uses `@Assisted BillboardScreen.CardDetail` to receive `cardKey`.
- Mutating operations (`AddGroup`, `RemoveGroup`, `RemoveFromCollection`) launch via `rememberRetained { MainScope() }` and wrap in `runCatching` with `Timber.e` on failure.
- Default group (`Group.DEFAULT_ID`, "Starred") is non-deletable — `OnRequestDeleteGroup(DEFAULT_ID)` is ignored.
- When deleting a group with 0 cards, deletion is immediate; with ≥1 cards, `pendingDeleteGroupId` is set to surface the confirmation dialog.
- Hilt component: `ActivityRetainedComponent`.

## UI Rules
- Access colors via `BillboardTheme.colorScheme` and typography via `BillboardTheme.typography` — **never** `MaterialTheme`, **never** hardcoded `Color.White.copy(...)`. All inline-alpha overlays use `textPrimary.copy(alpha = ...)` so light/dark theme both render correctly.
- Sidebar is rendered via `ModalNavigationDrawer` with the **RTL `CompositionLocalProvider` trick** so it opens from the right edge; drawer content is wrapped back to `Ltr` for layout sanity.
- `gesturesEnabled = false` on the drawer — sidebar opens **only** via the app-bar Menu icon (Material drawer scrim click still dismisses it).
- Back-press closes the sidebar first if open; second back press fires `OnBackClick`.
- `NowPlayingDeck` wraps `HoloCard` in `key(card.key) { ... }` so tapping a different card resets the rotation Animatable to 0°. `autoSpeed = 8f` for a calm idle spin.
- `MiniRail`'s outer Column has **no horizontal padding** — the `LazyRow` spans full width and supplies its own `contentPadding = PaddingValues(horizontal = 16.dp, ...)` so the ✕ badge of edge cards isn't clipped.
- ✕ badge fires through `throttledProcess` (from `:core:design-foundation/util/Throttled.kt`) to debounce repeated taps.
- Use `@ThemePreviews` for previews — wrap in `BillboardTheme { ... }`.

## Testing
- Presenter tests: `src/androidTest/` — `CollectionPresenterTest`, `CardDetailPresenterTest`.
- Use `circuit-test`'s `Presenter.test { }` builder with `FakeNavigator`.
- Inject fakes via `:core:data-test` (`FakeCollectionRepository`, `FakeGroupRepository`).

## Dependencies
- `:core:circuit` — `BillboardScreen.Collection`, `BillboardScreen.CardDetail`
- `:core:domain` — `GetCollectionFlowUseCase`, `GetCollectedCardFlowUseCase`, `GetGroupsFlowUseCase`, `AddGroupUseCase`, `RemoveGroupUseCase`, `RemoveFromCollectionUseCase`, `CollectedCard` model
- `:core:data` — `Group` data model (sidebar/chip rendering)
- `:core:design-system` — `BillboardTheme`, `HoloCard`, `GroupChip`, `GroupDot`, `BillboardHeader`
- `:core:resource` — strings (transitively via `:core:design-system`)
