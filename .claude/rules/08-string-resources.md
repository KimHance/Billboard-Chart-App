# String Resources

All user-facing strings live in `:core:resource/src/main/res/values/strings.xml`. The app is **English-only** — there are no localized variants and no plan to add any.

## Hard Rules
- **No hardcoded user-facing string literals** in `feature/*` or `core/design-system/*` Kotlin code. Always reference `R.string.<key>` via `stringResource(...)` in `@Composable` or `context.getString(...)` in non-composable code (Presenters).
- All strings are written in English. Korean UI strings are forbidden — Korean is reserved for code comments and AI conversation only (see root `CLAUDE.md` language rules).
- `:core:resource` is consumed **transitively** through `:core:design-system` (`api(projects.core.resource)`), so any module already depending on `:core:design-system` (every feature module via the `billboard.android.feature` convention plugin) gets `R` for free. Do not declare `implementation(projects.core.resource)` in feature/build files.
- Import `R` as `com.hancekim.billboard.core.resource.R` — never re-export it from feature modules.
- Resource files live exclusively in `:core:resource`. Do **not** add `strings.xml` (or any `values/*.xml` user-facing string file) inside feature or other core modules.

## Naming Convention
| Prefix | Use for | Example |
|---|---|---|
| `app_*` | App-wide brand (app name, etc.) | `app_name` |
| `action_*` | Reusable verb labels (CANCEL / ADD / DELETE / OK / Close) | `action_cancel` |
| `cd_*` | Accessibility / `contentDescription` labels | `cd_open_collection`, `cd_delete_group` |
| `<feature>_*` | Strings owned by a single feature | `home_overlay_hint`, `collection_inspect`, `settings_theme_title` |
| `<feature>_section_*` | Section headers inside a screen | `new_group_section_color` |

Reuse an existing key before adding a new one. If two features render the same literal, promote it to a shared prefix (`action_*`, `cd_*`).

## Format Strings
- Use positional arguments (`%1$s`, `%1$d`) so order-of-appearance is explicit even in a single-locale codebase.
- Build with `stringResource(R.string.key, arg1, arg2)` or `context.getString(R.string.key, arg1, arg2)`.
- Example: `<string name="collection_count_in_group">%1$d IN %2$s</string>` → `stringResource(R.string.collection_count_in_group, count, group.name.uppercase())`.

## When to use which call site
- `@Composable` UI → `stringResource(R.string.key)`. If the value is consumed inside a `semantics { ... }` lambda or other non-composable scope, capture it into a `val` first.
- Presenter / non-composable code (snackbars, error fallbacks) → inject `LocalContext.current` once at the top of `present()` and call `context.getString(R.string.key)`.
- Tests should reference `R.string.*` too — never re-hardcode the literal.

## Allowed exceptions
- Logging tags / `Timber` messages / debug-only `testTag` identifiers (`"home"`, `"dropdown-chevron"`, `"setting_button"` style internal IDs that never reach the user) stay inline.
- Preview-only placeholder data inside `@ThemePreviews` (e.g., dummy `Chart("Preview Title", "Preview Artist")`) stays inline — previews are dev-only artifacts.
- Decorative glyphs that act as icons (`"✕"`) are allowed inline; their meaning is conveyed via `contentDescription` from a string resource.

## Adding a new string
1. Add the `<string>` entry to `core/resource/src/main/res/values/strings.xml` with a descriptive, prefix-following key.
2. Use it from the call site via `stringResource` / `getString`.
3. Build with `./gradlew :core:resource:assembleDebug` to surface schema errors early.
