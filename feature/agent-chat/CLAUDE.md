# :feature:agent-chat Module

## Responsibility
Test-only agent chat surface. Invokes an external LLM (Gemini REST) with the same Hot 100 capability exposed as `@AppFunction` in `:app`, and renders the LLM reply as chat bubbles. Demonstrates the full tool-calling loop that AppFunctions Studio's Agent Demo cannot complete (preview limitation).

## Screen Definition
`BillboardScreen.AgentChat` — navigated to from `SettingEvent.OnAgentChatClick`.

## Key Files
| File | Role |
|------|------|
| `AgentChatState.kt` | `AgentChatState : CircuitUiState`, `AgentChatEvent : CircuitUiEvent`, `ChatMessage` |
| `AgentChatPresenter.kt` | Owns Gemini REST agent loop + `GetBillboardHot100UseCase` invocation |
| `AgentChatUi.kt` | Compose chat surface |
| `gemini/GeminiAgentClient.kt` | OkHttp + kotlinx.serialization Gemini REST client with `tools[]` + `function_call` round-trip |

## Tool-Call Loop (in Presenter)
1. User submits prompt.
2. Presenter calls `GeminiAgentClient.complete(prompt, tools=[getCurrentHot100TopSong])`.
3. If response has `functionCall`:
    - Resolve `getCurrentHot100TopSong` locally via `GetBillboardHot100UseCase` (same path as the `@AppFunction`).
    - Send a follow-up request with the previous turn + `functionResponse` part.
    - Receive final natural-language answer.
4. Append final answer as `ChatMessage` and update state.

## Rules
- This module is **test-only**. Do not import it from any production flow other than the Settings entry point.
- API key lives in `local.properties` as `GEMINI_API_KEY` and is injected via `buildConfigField`. Never commit the value.
- Use `GetBillboardHot100UseCase` for data — do not duplicate Retrofit calls here.
- Error handling: every IO call wrapped in `runCatching` + `Timber.e`. Surface failures as a chat bubble with `role = Error`.
- All user-facing strings live in `:core:resource/strings.xml` with `agent_chat_*` prefix.

## Build Configuration
Plugins: `billboard.android.feature`, `billboard.android.hilt`, `billboard.circuit`, `kotlin-serialization`.
`buildConfig = true`, `GEMINI_API_KEY` field from `local.properties`.

## Dependencies
- `:core:circuit` — `BillboardScreen.AgentChat`
- `:core:domain` — `GetBillboardHot100UseCase` (via the `billboard.android.feature` convention)
- `:core:design-system` — `BillboardTheme`
- `:core:design-foundation` — icons, throttledProcess
- `androidx.appfunctions` — schema reuse (optional)
- OkHttp + kotlinx.serialization — Gemini REST client
