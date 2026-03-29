# :core:network Module

## Responsibility
Network infrastructure. Provides the Retrofit instance, OkHttp client, and a type-safe `Result`-based call adapter so all API calls return `Result<T>` instead of throwing exceptions.

## Key Files
| File | Role |
|------|------|
| `NetworkModule.kt` | Hilt module — provides `OkHttpClient`, `Retrofit` |
| `JsonModule.kt` | Hilt module — provides `Json` (kotlinx.serialization) |
| `NetworkMonitor.kt` | Connectivity monitoring utility |
| `retrofit/ResultCall.kt` | `Call<T>` wrapper that catches HTTP errors |
| `retrofit/ResultCallAdapter.kt` | `CallAdapter.Factory` for `Result<T>` return types |
| `retrofit/NetworkFactory.kt` | Convenience factory for creating Retrofit instances |

## Rules
- All Retrofit API interfaces must declare `suspend` functions returning `Result<T>`.
- Do **not** throw exceptions from DataSources — return `Result.failure(...)` instead.
- `OkHttp` logging interceptor is included — ensure it is **only** active in debug builds.
- Base URL and API keys must come from `BuildConfig` fields injected at build time — **never** hardcode them.
- This module is `prodImplementation` only in `:core:data-source` — it is not linked in the `demo` flavor.

## Testing
- `ResultCallTest.kt` in `src/test/` — unit tests for the call adapter.

## Build Configuration
Plugins: `billboard.android.library`, `billboard.android.hilt`.
