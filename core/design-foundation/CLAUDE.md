# :core:design-foundation Module

## Responsibility
Low-level design tokens and primitives. This is the foundation layer of the design system — it has no dependency on `:core:design-system`. Contains raw color values, typography tokens, icons, modifiers, and preview annotations.

## Structure
```
design-foundation/
  color/
    BillboardColor.kt       ← raw color palette (BillboardColor object)
    SemanticColor.kt        ← semantic color mapping
    BillboardColorScheme.kt ← ColorScheme interface + Light/Dark implementations
    LocalColorScheme.kt     ← CompositionLocal<BillboardColorScheme>
  typography/
    BillboardFont.kt        ← font family definitions
    BillboardTypography.kt  ← typography style extensions
    BillboardTypographyTokens.kt ← App/Device token sets
    LocalTypographyTokens.kt     ← CompositionLocal
  icon/
    BillboardIcons.kt       ← icon registry
    Ico*.kt                 ← individual icon vector definitions
  modifier/
    Clickable.kt            ← custom clickable modifier (with ripple/no-ripple variants)
  indication/
    GlowIndication.kt       ← glow press indication
  util/
    ScaleExtensions.kt      ← scale/size utilities
    Throttled.kt            ← throttled click handler
  preview/
    ThemePreview.kt         ← @ThemePreviews multipreview annotation
```

## Color System
- `BillboardColor` — raw static color values (Slate/Grey palette, accent Green400).
- `SemanticColor` / `BillboardColorScheme` — semantic roles (e.g., `bgApp`, `textPrimary`).
- Always use `BillboardTheme.colorScheme.<token>` in composables — **never** use `BillboardColor.*` directly in feature/design-system code.

## Typography System
- `BillboardTypographyTokens` has two instances: `AppTypographyTokens` (custom font) and `DeviceTypographyTokens` (system font).
- Access via `BillboardTheme.typography.<style>()` — the parentheses call returns a `TextStyle`.

## Rules
- This module must **not** depend on `:core:design-system` — the dependency flows upward only.
- Do not add Compose UI components (Buttons, Cards, etc.) here — those belong in `:core:design-system`.
- New icons should be added as `ImageVector` properties in `BillboardIcons` and as individual `Ico*.kt` files.
- Use `@ThemePreviews` for all new composable previews in both this module and `:core:design-system`.

## Build Configuration
Plugins: `billboard.android.library.compose`.
