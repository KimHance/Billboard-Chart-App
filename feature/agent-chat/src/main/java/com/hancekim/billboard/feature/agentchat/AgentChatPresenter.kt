package com.hancekim.billboard.feature.agentchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.feature.agentchat.gemini.GeminiAgentClient
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.util.UUID

class AgentChatPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    // BillboardFunctions 의 @AppFunction 과 동일한 4 개 차트 UseCase 를 그대로 활용.
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
    private val getBillboardGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboardArtist100UseCase: GetBillboardArtist100UseCase,
    private val geminiClient: GeminiAgentClient,
) : Presenter<AgentChatState> {

    @Composable
    override fun present(): AgentChatState {
        val scope = rememberCoroutineScope()
        // rememberRetained 로 회전/구성 변경 시에도 채팅 이력 유지.
        var messages: ImmutableList<ChatMessage> by rememberRetained {
            mutableStateOf(persistentListOf())
        }
        var input: String by rememberRetained { mutableStateOf("") }
        var isSending: Boolean by rememberRetained { mutableStateOf(false) }

        return AgentChatState(
            messages = messages,
            input = input,
            isSending = isSending,
        ) { event ->
            when (event) {
                AgentChatEvent.OnBackClick -> navigator.pop()

                is AgentChatEvent.OnInputChange -> {
                    input = event.text
                }

                AgentChatEvent.OnSendClick -> {
                    val trimmed = input.trim()
                    // 빈 입력이거나 진행 중이면 무시.
                    if (trimmed.isEmpty() || isSending) return@AgentChatState

                    val userMessage = ChatMessage(
                        role = Role.User,
                        text = trimmed,
                        id = UUID.randomUUID().toString(),
                    )
                    // 1) 사용자 메시지 추가 + 입력 비우기 + 송신 상태로 전환.
                    messages = (messages + userMessage).toPersistentList()
                    input = ""
                    isSending = true

                    val historyForLlm = messages
                    scope.launch {
                        val reply = runCatching {
                            geminiClient.chat(historyForLlm, ::resolveTool)
                        }.getOrElse { error ->
                            Timber.e(error, "agent chat failed")
                            ChatMessage(
                                role = Role.Error,
                                text = error.message ?: "Unknown error",
                                id = UUID.randomUUID().toString(),
                            )
                        }
                        messages = (messages + reply).toPersistentList()
                        isSending = false
                    }
                }
            }
        }
    }

    /**
     * Gemini 가 호출한 function call 을 인앱 데이터로 해석.
     * BillboardFunctions 의 @AppFunction 과 동일한 데이터 경로 (GetBillboardHot100UseCase) 사용.
     */
    private suspend fun resolveTool(name: String, args: JsonObject): Map<String, Any?> {
        return when (name) {
            "getSongChartByRank" -> {
                // chartType 검증 → 적절한 UseCase 선택 → rank 검증 → 차트에서 해당 순위 곡 추출.
                val rawChartType = args["chartType"]?.jsonPrimitive?.content
                    ?: throw IllegalArgumentException("chartType argument missing")
                val chartType = rawChartType.lowercase()
                val maxRank = when (chartType) {
                    "hot100" -> 100
                    "billboard200", "global200" -> 200
                    else -> throw IllegalArgumentException(
                        "Unknown chartType '$rawChartType'. Use 'hot100', 'billboard200', or 'global200'."
                    )
                }
                val rank = args["rank"]?.jsonPrimitive?.int
                    ?: throw IllegalArgumentException("rank argument missing")
                require(rank in 1..maxRank) { "rank must be in 1..$maxRank for $chartType, got $rank" }

                val overview = when (chartType) {
                    "hot100" -> getBillboardHot100UseCase()
                    "billboard200" -> getBillboard200UseCase()
                    "global200" -> getBillboardGlobal200UseCase()
                    else -> error("unreachable")
                }
                val entry = overview.chartList.firstOrNull { it.rank == rank }
                    ?: throw IllegalStateException("$chartType has no rank-$rank entry")
                mapOf(
                    "title" to entry.title,
                    "artist" to entry.artist,
                    "rank" to rank,
                    "chartType" to chartType,
                )
            }

            "getArtist100ByRank" -> {
                val rank = args["rank"]?.jsonPrimitive?.int
                    ?: throw IllegalArgumentException("rank argument missing")
                require(rank in 1..100) { "rank must be in 1..100, got $rank" }
                val overview = getBillboardArtist100UseCase()
                val entry = overview.chartList.firstOrNull { it.rank == rank }
                    ?: throw IllegalStateException("Artist 100 has no rank-$rank entry")
                // 도메인 매퍼 에 따라 artist 또는 title 한쪽에 아티스트 이름이 들어옴.
                val artistName = entry.artist.ifBlank { entry.title }
                mapOf(
                    "name" to artistName,
                    "rank" to rank,
                )
            }

            else -> throw IllegalArgumentException("Unknown tool: $name")
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.AgentChat::class, ActivityRetainedComponent::class)
    fun interface Factory {
        fun create(navigator: Navigator): AgentChatPresenter
    }
}
