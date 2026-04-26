# Design System

## Theme Access
- Always use `BillboardTheme.colorScheme.<token>` and `BillboardTheme.typography.<style>()`.
- **Never** use `MaterialTheme` directly. **Never** import `androidx.compose.material3.MaterialTheme` for theming purposes.

## Color Token Hierarchy
```
:core:design-foundation
  ├── BillboardColor          ← raw palette (Grey50, Slate900, HoloBlue, …)
  └── BillboardColorScheme    ← semantic tokens (bgApp, textPrimary, scrim, holoGlow, …)

:core:design-system
  └── BillboardTheme          ← entry point, provides colorScheme via CompositionLocal
```

### Hard Rules
- In feature modules and `:core:design-system` components: **only semantic tokens** (`colorScheme.*`).
- Raw palette (`BillboardColor.*`) is allowed **only inside `:core:design-foundation`** (defining the scheme).
- `Color(0xFFXXXXXX)` literals in feature/design-system code are forbidden — add a primitive token to `BillboardColor`, then expose it via a semantic token in `BillboardColorScheme`.
- If a color is theme-independent (e.g., shader effect colors), still go through `BillboardColor.X` named constants — never inline hex.
- Both `BillboardLightColorScheme` and `BillboardDarkColorScheme` must define every semantic token.

## Typography
- Tokens are defined in `:core:design-foundation/typography/BillboardTypographyTokens.kt`.
- Two variants: `AppTypographyTokens` (custom font) and `DeviceTypographyTokens` (system font).
- Access: `BillboardTheme.typography.titleMd()` etc. — note the parentheses (returns `TextStyle`).

## Previews
- Every new `@Composable` (especially screens, overlays, cards) MUST include a `@ThemePreviews`-annotated preview function.
- `@ThemePreviews` (from `:core:design-foundation/preview/ThemePreview.kt`) covers light + dark.
- Preview function should be `private` and wrap content in `BillboardTheme { ... }`.

## Accessibility
- Clickable `Box` / non-`Button` widgets MUST attach semantics:
  ```kotlin
  Modifier.semantics {
      role = Role.Button
      contentDescription = "닫기"  // user-facing label
  }
  ```
- `Icon`s used purely for decoration take `contentDescription = null`. Icons that convey meaning need a description.
- Tap targets ≥ 36.dp; honor `WindowInsets` for system-bar-aware UI (status, navigation).

## Component Placement
| Layer | Examples |
|---|---|
| `:core:design-foundation` | Color tokens, typography, icons (`BillboardIcons`, `Ico*.kt`), modifiers (`noRippleClickable`, `throttledProcess`), `@ThemePreviews` |
| `:core:design-system` | `BillboardTheme`, shared composables (`HoloCard`, `SparkleEffect`, `BillboardHeader`, `RankingList`, `FilterRow`, …) |
| `:feature:*` | screen-specific composables only — no re-usable widgets |

## Throttling
- `throttledProcess` (from `:core:design-foundation/util/Throttled.kt`) wraps click handlers to prevent rapid-fire taps. Use for actions with side effects (REMOVE, navigation, payment).
