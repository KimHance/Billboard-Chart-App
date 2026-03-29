# :core:player Module

## Responsibility
YouTube video playback abstraction. Wraps `AndroidYouTubePlayer` library with a state-driven Compose-friendly API and provides PiP (picture-in-picture) mode support.

## Key Files
| File | Role |
|------|------|
| `PlayerState.kt` | Holds all player state — playback status, video ID, thumbnail, mute, etc. |
| `PlayerController.kt` | Interface for controlling playback (`play`, `pause`, `load`, `mute`) |
| `YoutubePlayer.kt` | `@Composable` that renders the YouTube player view |
| `pip/PipState.kt` | PiP overlay state management |
| `pip/ListPipPlayer.kt` | PiP mini-player composable rendered in the list |

## PlayerState
- Created with `Context` parameter for `AndroidYouTubePlayer` initialization.
- Must be created via `rememberRetained { PlayerState(context) }` in Presenters — **not** in Composables.
- Exposes: `isPlaying`, `isPlayable`, `isMuted`, `videoId`, `thumbnailUrl`.
- Mutated via: `play()`, `pause()`, `load(videoId, thumbnailUrl)`, `changePlayable(Boolean)`.

## PiP Mode
- `isPipMode` in `HomeState` is `true` when the filter row has scrolled past the player.
- When `isPipMode = true`, `ListPipPlayer` renders a small floating player in the list.
- `PipState` manages coroutine scope — call `pipState.setScope(scope)` in a `LaunchedEffect`.

## Rules
- `PlayerState` holds a reference to `Context` — always create with `rememberRetained` to survive configuration changes without leaking.
- Pause the player when navigating away from Home (`OnBackPressed`, `OnSettingIconClick`).
- Do **not** expose `AndroidYouTubePlayer` types outside this module — keep the API surface limited to `PlayerState` and `PlayerController`.

## Build Configuration
Plugins: `billboard.android.library.compose`, `billboard.android.hilt`.
Dependency: `libs.youtube.player` (com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0).
