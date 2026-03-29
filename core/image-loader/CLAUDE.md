# :core:image-loader Module

## Responsibility
Image loading setup. Configures and provides the Coil 3 `ImageLoader` singleton with GIF, SVG, and network (OkHttp) support.

## Rules
- The `ImageLoader` is provided as a Hilt singleton — **never** create `ImageLoader` instances manually.
- GIF and SVG decoders are registered — use `AsyncImage` from `coil-compose` directly in composables.
- OkHttp is used as the network fetcher — ensure it shares the same OkHttp instance as `:core:network` where possible.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`.
Dependencies: `coil`, `coil-gif`, `coil-svg`, `coil-network`.
