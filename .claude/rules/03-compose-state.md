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

## Custom Modifier Authoring
When implementing a reusable custom `Modifier`, prefer the Modifier.Node API. `Modifier.composed { }` is **forbidden** — it allocates a new instance on every recomposition, breaks Modifier equality, and prevents skipping.

Priority order (use the first one that fits):

1. **Plain `Modifier` extension that returns `this.then(...)` of stock modifiers** — when the behavior is purely composition of existing modifiers (e.g., `Modifier.padding(...).background(...)`). No Node needed.

2. **`Modifier.Node` + `ModifierNodeElement<T>`** — the default for custom drawing, layout, pointer input, focus, semantics, or any behavior that needs lifecycle / coroutine scope / `currentValueOf(LocalX)`. This is the modern, allocation-free path.
   ```kotlin
   private data class FooElement(val arg: Int) : ModifierNodeElement<FooNode>() {
       override fun create() = FooNode(arg)
       override fun update(node: FooNode) { node.arg = arg }
   }
   private class FooNode(var arg: Int) : Modifier.Node(), DrawModifierNode { … }
   fun Modifier.foo(arg: Int): Modifier = this then FooElement(arg)
   ```

3. **`@Composable fun Modifier.foo(): Modifier` (composable Modifier extension)** — only when the behavior fundamentally requires **composition-time access** that Node can't satisfy cleanly: e.g., calling other `@Composable` functions, observing `MutableState` reads via `remember`, or animating with `Animatable` returned from `remember`. Even here, prefer reading state inside Node lambdas (`graphicsLayer { … }`, `drawBehind { … }`) when possible, and pass the state in as a parameter or `() -> T` provider.

4. **`Modifier.composed { … }`** — **forbidden.** If you find yourself reaching for it, rewrite as Node (option 2) or composable Modifier (option 3). Existing usages should be migrated.

### Quick decision
- Need `LocalDensity` / `LocalLayoutDirection` only? → Node (`currentValueOf(LocalDensity)`).
- Need `remember { Animatable(...) }` driving the modifier? → Composable Modifier (option 3).
- Need to compose stock modifiers conditionally? → Plain `Modifier` extension.
- Tempted to write `composed { … }`? → Stop and pick option 2 or 3.
