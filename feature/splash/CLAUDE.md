# :feature:splash Module

## Responsibility
Splash screen. Shown immediately on launch (backed by `SplashScreen API`). Handles initialization logic before navigating to Home.

## Screen Definition
`BillboardScreen.Splash` — navigation root; registered in `:core:circuit`.

## Key Files
| File | Role |
|------|------|
| `SplashState.kt` | `SplashState : CircuitUiState`, `SplashEvent : CircuitUiEvent` |
| `SplashPresenter.kt` | Initialization logic; navigates to Home when ready |
| `SplashUi.kt` | Minimal Compose UI (logo / loading indicator) |

## Rules
- This screen is the **navigation root** in `MainActivity`. It must always push `BillboardScreen.Home` — never pop.
- The native Android `SplashScreen` is installed in `MainActivity.onCreate()` via `installSplashScreen()` — this module handles post-splash initialization.
- Keep this screen thin. No business data fetching — only necessary app init.
- Navigate to Home via `navigator.goTo(BillboardScreen.Home)` after init completes.

## Build Configuration
Plugins: `billboard.android.feature`, `billboard.android.library.compose`, `billboard.android.hilt`, `billboard.circuit`.
