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
- **Theme-aware colors → semantic tokens (`BillboardTheme.colorScheme.*`).**
  Anything that should look different in light vs. dark mode (backgrounds, foregrounds, dividers, surfaces, text on theme-aware surfaces, etc.) MUST come from `BillboardColorScheme`.
- **Theme-independent colors → primitive tokens (`BillboardColor.*`) are fine.**
  Brand-fixed colors that look the same regardless of theme — shader stops, fixed scrim/glow effects, decorative gradients — may reference `BillboardColor.*` directly. No need to invent a semantic alias just to satisfy a rule.
- **`Color(0xFFXXXXXX)` literals in feature / design-system code are forbidden.**
  If a hex value doesn't fit any existing primitive, add a named constant to `BillboardColor` first, then use it. Inline hex is only acceptable inside `:core:design-foundation` itself when defining the palette.
- Both `BillboardLightColorScheme` and `BillboardDarkColorScheme` must define every semantic token.

### Decision Flow
```
새 색상이 필요하다
    │
    ▼
이 색이 light/dark 에서 다르게 보여야 하나?
    │
    ├─ Yes → BillboardColorScheme 에 semantic 토큰 추가
    │         (필요하면 BillboardColor 에 primitive 도 같이 추가)
    │         사용처: BillboardTheme.colorScheme.X
    │
    └─ No  → BillboardColor 에 primitive 토큰만 추가
              사용처: BillboardColor.X 직접 참조 OK
```

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
