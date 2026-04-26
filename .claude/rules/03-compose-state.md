# Compose & State Management

## State Class Rules
- Annotate State classes with `@Stable` (`@Immutable` if all fields are immutable).
- Use `ImmutableList` / `persistentListOf()` from `kotlinx-collections-immutable` for list state — **never** `List<T>`.
- `eventSink: (Event) -> Unit` MUST be the last property in every State class.
- Avoid heavy default values that allocate on every `copy()` (e.g., `SnackbarHostState()`, `LazyListState()` defaults). Inject from Presenter via `rememberRetained`.

## Presenter Rules
- Use `rememberRetained { }` for all mutable state in Presenters (survives config changes).
- Use `produceRetainedState { ... }` for async data loading — replaces `LaunchedEffect + setState`.
- Local lambdas inside `present()` (e.g., `loadVideo`) are preferred over private classes.

## Recomposition / Phase Discipline
**Reading animated state in composition phase causes per-frame recomposition.** Always isolate to draw phase:

```kotlin
// ❌ Bad — recomposition every frame
val angle = animatable.value
Modifier.graphicsLayer { rotationY = angle }

// ✅ Good — read inside graphicsLayer lambda (deferred to draw phase)
Modifier.graphicsLayer { rotationY = animatable.value }

// ✅ Good — pass as () -> Float provider into nested Composables
private fun Child(angleProvider: () -> Float) {
    Modifier.graphicsLayer { rotationY = angleProvider() }
}
```

- Wrap derived booleans in `derivedStateOf` when only specific thresholds matter (e.g., `showFront = angle in flipRange`).
- For animated `alpha`, prefer `Modifier.graphicsLayer { alpha = state.value }` over recomputing `Color.copy(alpha = …)` per recomposition.

## LaunchedEffect Keys
- `LaunchedEffect(Unit)` only when the effect must run exactly once for the lifetime of the composition.
- For animations that should re-run when content changes, key by the content identity (e.g., `LaunchedEffect(item.title, item.artist)`) and `snapTo(initial)` before re-animating.

## Caching Expensive Computations
- Wrap density-dependent calculations in `remember(density) { ... }` (e.g., orbit slot offsets, layout grids).
- Anything that doesn't change across recompositions belongs in `remember`.

## Compose Imports / Theme
- Access theme via `BillboardTheme.colorScheme` and `BillboardTheme.typography` — **never** `MaterialTheme` directly. See `07-design-system.md` for token rules.
- Use `testTag()` on top-level screen containers for UI tests; `testTagsAsResourceId = true` is already set in `MainActivity`.
- Click handlers that may fire repeatedly (REMOVE, etc.) should be wrapped in `throttledProcess` from `:core:design-foundation`.

## Side Effects
- `StateDiffLogEffect` (from `:core:design-system`) is enabled with a screen-specific tag for debugging — keep it in development.
- `BackHandler` placement: at the top of the `@Composable` Ui function, dispatching the corresponding `Event`.
