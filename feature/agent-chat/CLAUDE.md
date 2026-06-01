# :feature:agent-chat Module

## Responsibility
Test-only agent chat surface. Hosts an in-app ADK (Agent Development Kit) multi-agent system over the same Billboard chart + collection domain that the public `@AppFunction` entry points expose. Renders the agent reply as chat bubbles.

## Screen Definition
`BillboardScreen.AgentChat` — navigated to from `SettingEvent.OnAgentChatClick`.

## Architecture
Agent loop = ADK `InMemoryRunner`. The legacy `GeminiAgentClient` (raw REST + tool-calling) is retained as deprecated fallback in case ADK setup fails — it is no longer wired into the active chat flow.

```
AgentChatPresenter
  └─ BillboardAgents
       ├─ billboard_concierge (top-level LlmAgent)
       │     subAgents:
       │       ├─ chart_lookup       → ChartTools.generatedTools()
       │       └─ collection_manager → CollectionTools.generatedTools()
       └─ Tools share :core:domain UseCases with :app/appfunctions/BillboardFunctions
          (single source of truth for chart + collection access).
```

## Key Files
| File | Role |
|------|------|
| `AgentChatState.kt` | `AgentChatState : CircuitUiState`, `AgentChatEvent : CircuitUiEvent`, `ChatMessage` |
| `AgentChatPresenter.kt` | Owns ADK `InMemoryRunner` lifecycle, dispatches user turns into `mainConcierge.runAsync(...)` |
| `AgentChatUi.kt` | Compose chat surface |
| `tools/ChartTools.kt` | ADK `@Tool` wrappers around Hot 100 / Billboard 200 / Global 200 / Artist 100 UseCases |
| `tools/CollectionTools.kt` | ADK `@Tool` wrappers around collection + group UseCases (get/find/add/remove/createGroup) |
| `agent/BillboardAgents.kt` | Defines `chart_lookup`, `collection_manager`, and `billboard_concierge` LlmAgents + their instructions |
| `gemini/GeminiAgentClient.kt` | **Deprecated.** Raw Gemini REST + tool-calling client. Kept as emergency rollback only. |

## Rules
- This module is **test-only**. Do not import it from any production flow other than the Settings entry point.
- API key lives in `local.properties` as `GEMINI_API_KEY` and is injected via `buildConfigField`. Never commit the value.
- Use `:core:domain` UseCases for all data access — do not duplicate Retrofit / Room calls here. Tool classes mirror `BillboardFunctions` (single SoT).
- Error handling: every IO call wrapped in `runCatching` + `Timber.e`. Surface failures as a chat bubble with `role = Error`.
- All user-facing strings live in `:core:resource/strings.xml` with `agent_chat_*` prefix.
- ADK session (`InMemorySessionService` + `InMemoryRunner` + `sessionId`) is wrapped in `rememberRetained` so it survives configuration changes within a single chat session.

## Build Configuration
Plugins: `billboard.android.feature`, `billboard.android.hilt`, `billboard.circuit`, `kotlin-serialization`.
`buildConfig = true`, `GEMINI_API_KEY` field from `local.properties`.

## Dependencies
- `:core:circuit` — `BillboardScreen.AgentChat`
- `:core:domain` — chart + collection UseCases (via the `billboard.android.feature` convention)
- `:core:design-system` — `BillboardTheme`
- `:core:design-foundation` — icons, throttledProcess
- `androidx.appfunctions` — schema reuse (optional)
- `libs.adk.core.android` (implementation) + `libs.adk.processor` (ksp) — ADK multi-agent runtime + `@Tool` annotation processor
- OkHttp + kotlinx.serialization — only consumed by the deprecated `GeminiAgentClient`
