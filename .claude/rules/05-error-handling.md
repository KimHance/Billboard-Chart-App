# Error Handling

## Async Operations in Presenters
Wrap every IO / use-case call that can fail in `runCatching` and split the success / failure paths:

```kotlin
scope.launch {
    runCatching { removeFromCollectionUseCase(key) }
        .onSuccess { navigator.pop() }
        .onFailure { Timber.e(it, "Failed to remove card: $key") }
}
```

- Never let an exception silently fall through `scope.launch { ... }` — at minimum log via `Timber.e`.
- Network calls use Retrofit's `ResultCall` / `ResultCallAdapter` (already configured in `:core:network`) so callsites receive `Result<T>` rather than throwing.

## User-Facing Error Surfaces
- Show recoverable errors via `SnackbarHostState` exposed in State; trigger from Presenter via `rememberRetained` SnackbarHostState passed into State.
- **Never** use `android.widget.Toast` in feature modules.
- For fatal/empty states, render a dedicated empty/error composable — do not show raw exception messages to users.

## Logging
- Use `Timber` exclusively (already initialized in `MainApplication`). Never `Log.d/e` directly.
- Production logs are stripped by R8 rules; verbose logging is fine in source.

## ProGuard / R8
- Custom keep rules live in `app/proguard-rules.pro`. Do not disable R8 without project owner approval.
- Result-typed APIs (Retrofit `ResultCall`) require explicit keep rules for `kotlin.Result` — already documented in proguard rules.
