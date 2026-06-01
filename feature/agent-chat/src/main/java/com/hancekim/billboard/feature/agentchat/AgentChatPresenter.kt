package com.hancekim.billboard.feature.agentchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
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
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
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
            "getCurrentHot100TopSong" -> {
                val overview = getBillboardHot100UseCase()
                val top = overview.chartList.firstOrNull { it.rank == 1 }
                    ?: throw IllegalStateException("Hot 100 has no rank-1 entry")
                mapOf(
                    "title" to top.title,
                    "artist" to top.artist,
                    "rank" to 1,
                )
            }

            "getHot100SongByRank" -> {
                // LLM 이 보낸 rank 인자를 검증 + 차트에서 해당 순위 곡을 찾는다.
                val rank = args["rank"]?.jsonPrimitive?.int
                    ?: throw IllegalArgumentException("rank argument missing")
                require(rank in 1..100) { "rank must be in 1..100, got $rank" }
                val overview = getBillboardHot100UseCase()
                val entry = overview.chartList.firstOrNull { it.rank == rank }
                    ?: throw IllegalStateException("Hot 100 has no rank-$rank entry")
                mapOf(
                    "title" to entry.title,
                    "artist" to entry.artist,
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
