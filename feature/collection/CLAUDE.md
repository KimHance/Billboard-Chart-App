# :feature:collection Module

## Responsibility
Collection screen — displays the user's collected cards as an orbiting layout, and a card detail screen for viewing/removing a single card.

## Screen Definitions
- `BillboardScreen.Collection` — list of collected cards.
- `BillboardScreen.CardDetail(cardKey)` — detail view for a single card.

Both registered in `:core:circuit`.

## Key Files
| File | Role |
|------|------|
| `CollectionState.kt` | `CollectionState : CircuitUiState`, `CollectionEvent : CircuitUiEvent` |
| `CollectionPresenter.kt` | Loads collected cards via `GetCollectionFlowUseCase`; navigates to `CardDetail` |
| `CollectionUi.kt` | Top-level `@CircuitInject` composable |
| `CardDetailState.kt` | `CardDetailState : CircuitUiState`, `CardDetailEvent : CircuitUiEvent` |
| `CardDetailPresenter.kt` | Loads single card via `GetCollectedCardFlowUseCase`; removes via `RemoveFromCollectionUseCase` |
| `CardDetailUi.kt` | Top-level `@CircuitInject` composable for card detail |
| `component/OrbitLayout.kt` | Custom orbit layout for arranging cards in a circular pattern |
| `component/EmptySlot.kt` | Placeholder slot rendered when no cards are collected |

## State & Events

### CollectionState
- `cards: ImmutableList<CollectedCard>` — collected cards from the repository flow
- **Events:** `OnCardClick(cardKey)`, `OnBackClick`

### CardDetailState
- `card: CollectedCard?` — `null` while loading or when not found
- **Events:** `OnCloseClick`, `OnRemoveClick`

## Presenter Rules
- Use `produceRetainedState` to collect from UseCase flows; never `LaunchedEffect` for state population.
- `CardDetailPresenter` uses `@Assisted BillboardScreen.CardDetail` to receive `cardKey` from navigation.
- Removal in `CardDetailPresenter` must be launched via `rememberCoroutineScope()` and pop after completion.
- Hilt component: `ActivityRetainedComponent`.

## UI Rules
- Access colors via `BillboardTheme.colorScheme`, typography via `BillboardTheme.typography`.
- Orbit layout (`OrbitLayout`) is presentation-only — keep layout math out of the Presenter.
- Use `@ThemePreviews` for previews.

## Testing
- Presenter tests: `src/androidTest/` — `CollectionPresenterTest`, `CardDetailPresenterTest`.
- Use `circuit-test`'s `Presenter.test { }` builder.
- Inject fakes via `:core:data-test`.

## Dependencies
- `:core:circuit` — `BillboardScreen.Collection`, `BillboardScreen.CardDetail`
- `:core:domain` — `GetCollectionFlowUseCase`, `GetCollectedCardFlowUseCase`, `RemoveFromCollectionUseCase`, `CollectedCard` model
- `:core:design-system` — `BillboardTheme`, components
- `:core:data` — data model access
