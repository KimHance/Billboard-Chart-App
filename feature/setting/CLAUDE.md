# :feature:setting Module

## Responsibility
Settings screen. Allows the user to change the app theme (Dark / Light / System) and app font (App / System).

## Screen Definition
`BillboardScreen.Setting` — navigated to from `HomeEvent.OnSettingIconClick`.

## Key Files
| File | Role |
|------|------|
| `SettingState.kt` | `SettingState : CircuitUiState`, `SettingEvent : CircuitUiEvent` |
| `SettingPresenter.kt` | Reads current preferences, dispatches update UseCases |
| `SettingUi.kt` | Settings list UI |

## State & Events
**Key state fields:**
- `appTheme: AppTheme` — current theme (Dark / Light / System)
- `appFont: AppFont` — current font (App / System)

**Events:** `OnThemeChanged(AppTheme)`, `OnFontChanged(AppFont)`, `OnBackPressed`

## Rules
- Use `UpdateAppThemeUseCase` and `UpdateAppFontUseCase` from `:core:domain` for writes.
- Use `GetAppThemeFlowUseCase` / `GetAppFontFlowUseCase` for reads via `produceRetainedState`.
- Navigate back via `navigator.pop()` on `OnBackPressed`.
- No local state caching for preferences — always read from the domain flow.

## Build Configuration
Plugins: `billboard.android.feature`, `billboard.android.library.compose`, `billboard.android.hilt`, `billboard.circuit`.

## Dependencies
- `:core:circuit` — `BillboardScreen`
- `:core:domain` — preference UseCases
- `:core:design-system` — `BillboardTheme`
