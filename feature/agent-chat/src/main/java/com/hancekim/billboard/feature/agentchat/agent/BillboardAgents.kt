package com.hancekim.billboard.feature.agentchat.agent

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.models.Gemini
import com.hancekim.billboard.feature.agentchat.BuildConfig
import com.hancekim.billboard.feature.agentchat.tools.ChartTools
import com.hancekim.billboard.feature.agentchat.tools.CollectionTools
// ADK KSP 가 tools 패키지에 생성한 extension function (`ChartTools.generatedTools()` / `CollectionTools.generatedTools()`).
import com.hancekim.billboard.feature.agentchat.tools.generatedTools
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ADK 기반 Billboard 멀티 에이전트 정의.
 *
 * - chart_lookup: 4 종 차트 조회 전용 (read-only).
 * - collection_manager: 사용자 컬렉션 추가/제거/그룹 관리 (writes).
 * - billboard_concierge: 위 두 sub-agent 로 라우팅하는 최상위 컨시어지.
 *
 * 모델은 모두 gemini-2.5-flash (GA + 무료 + tool calling 안정).
 * BuildConfig.GEMINI_API_KEY 가 빈 문자열이면 ADK 호출 시점에 에러가 발생,
 * Presenter 의 runCatching 에서 Error 메시지로 표시된다.
 */
@Singleton
class BillboardAgents @Inject constructor(
    private val chartTools: ChartTools,
    private val collectionTools: CollectionTools,
) {

    private val chartLookupAgent: LlmAgent by lazy {
        LlmAgent(
            name = "chart_lookup",
            description = "Reads Billboard chart entries (Hot 100, Billboard 200, Global 200, Artist 100). Read-only.",
            model = Gemini(name = MODEL_NAME, apiKey = BuildConfig.GEMINI_API_KEY),
            instruction = Instruction(
                """
                You look up Billboard chart entries.
                - Use getSongChartByRank for song charts (hot100 | billboard200 | global200).
                - Use getArtist100ByRank for the artist-level chart.
                - If the user gives an out-of-range rank, explain the valid range and ask them to retry.
                - If the chart type is ambiguous, ask the user to choose between hot100, billboard200, global200, artist100.
                Do not perform writes — collection_manager handles that.
                """.trimIndent()
            ),
            tools = chartTools.generatedTools(),
        )
    }

    private val collectionManagerAgent: LlmAgent by lazy {
        LlmAgent(
            name = "collection_manager",
            description = "Adds songs to the user's personal collection, removes them, lists groups, and creates new groups. Performs writes.",
            model = Gemini(name = MODEL_NAME, apiKey = BuildConfig.GEMINI_API_KEY),
            instruction = Instruction(
                """
                You manage the user's song collection grouped by user-defined categories.
                Workflow for adding a song:
                  1. If the user named a group, call getCollectionGroups; if an exact match exists, use it directly.
                  2. If no exact match, call findSimilarGroups(query=<user name>) and ASK the user to pick one
                     of the suggestions or create a new group.
                  3. Only after user confirmation, call addSongToCollection with the chosen groupId.
                  4. If the user wants a brand-new group, call createCollectionGroup with a sensible default
                     color (0xFFCCCCCC) and then addSongToCollection.
                Constraint:
                  - artist100 chart entries cannot be added to collection — they are artists, not songs.
                Workflow for removing:
                  - Always confirm with the user (echo the song title) before calling removeSongFromCollection.
                If a tool throws, surface the error message verbatim and suggest a recovery action.
                """.trimIndent()
            ),
            tools = collectionTools.generatedTools(),
        )
    }

    /** 최상위 컨시어지. NavigableCircuitContent / Presenter 가 이 에이전트를 Runner 에 등록한다. */
    val mainConcierge: LlmAgent by lazy {
        LlmAgent(
            name = "billboard_concierge",
            description = "Top-level Billboard chart + collection assistant.",
            model = Gemini(name = MODEL_NAME, apiKey = BuildConfig.GEMINI_API_KEY),
            instruction = Instruction(
                """
                You are the user's Billboard chart and collection assistant. Route requests:
                  - Chart lookups (e.g. "Hot 100 #1", "Artist 100 ranking 5") -> chart_lookup sub-agent.
                  - Anything that adds, removes, or lists collection entries -> collection_manager sub-agent.
                  - Ambiguous request -> ask the user a single clarifying question first.
                Style:
                  - Reply in the user's language (Korean if Korean was used).
                  - Keep replies concise and grounded in tool results — never fabricate chart positions or group names.
                  - For write operations always confirm before invoking the destructive tool.
                """.trimIndent()
            ),
            subAgents = listOf(chartLookupAgent, collectionManagerAgent),
        )
    }

    private companion object {
        // gemini-2.5-flash: GA + 무료 + tool calling 안정 (2.0-flash 는 free tier limit:0, 3-preview 는 thought_signature 강제).
        const val MODEL_NAME = "gemini-2.5-flash"
    }
}
