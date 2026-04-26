# Circuit (Slack Circuit) Pattern

This project uses [Slack Circuit](https://slackhq.github.io/circuit/) as its presentation framework.
Every screen follows this exact structure:

```
Screen (sealed interface, Parcelable) — defined in :core:circuit
    ├── State (data class : CircuitUiState) — holds all UI state + eventSink
    ├── Event (sealed interface : CircuitUiEvent) — all user interactions
    ├── Presenter (@CircuitInject, @AssistedInject) — business logic, returns State
    └── Ui (@CircuitInject, @Composable) — renders State, calls eventSink
```

## File Layout (per feature)
```
feature/<name>/
  <Name>Screen.kt     ← register in BillboardScreen sealed interface (:core:circuit)
  <Name>State.kt      ← State data class + Event sealed interface
  <Name>Presenter.kt  ← @AssistedInject constructor, @CircuitInject factory
  <Name>Ui.kt         ← @CircuitInject @Composable fun
  component/          ← sub-composables (not injected)
```

## Naming Conventions
- Screen: `<Name>Screen` (Parcelable `data object` in `BillboardScreen`)
- Presenter: `<Name>Presenter` — implements `Presenter<State>`
- UI: `<Name>Ui` — top-level `@Composable`
- State: `<Name>State` — implements `CircuitUiState`
- Event: `<Name>Event` — implements `CircuitUiEvent`, sealed interface
- UseCase: `Get<Resource>UseCase`, `Update<Resource>UseCase` (verb prefix)
- Repository interface: `<Resource>Repository` in `:core:data`
- Repository impl: `<Resource>RepositoryImpl` in `:core:data-impl`
- DataSource: `<Resource>DataSource` in `:core:data-source`

## Hard Rules
- Never use `ViewModel` directly in feature modules.
- All UI entry points (screens) use `@CircuitInject` — not manual factory wiring.
- Presenter constructors use `@AssistedInject` + `@AssistedFactory` to receive `Navigator`.
- Hilt component scope for Presenters: `ActivityRetainedComponent`.
- `BillboardScreen` is the single source of truth for navigable screens — never define ad-hoc screens elsewhere.
- Screens that pass parameters use `@Parcelize data class` (not `data object`); see `BillboardScreen.CardDetail(cardKey)`.
