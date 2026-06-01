package com.hancekim.billboard.feature.agentchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.feature.agentchat.agent.BillboardAgents
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import com.google.adk.kt.types.Role as AdkRole

class AgentChatPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val billboardAgents: BillboardAgents,
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

        // ADK 세션/러너는 채팅이 살아있는 동안 유지 — rememberRetained.
        val sessionService = rememberRetained { InMemorySessionService() }
        val runner = rememberRetained {
            InMemoryRunner(
                agent = billboardAgents.mainConcierge,
                sessionService = sessionService,
            )
        }
        val sessionId = rememberRetained { UUID.randomUUID().toString() }

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

                    scope.launch {
                        runCatching {
                            val reply = StringBuilder()
                            // ADK 0.2.0 InMemoryRunner 는 자체 디스패처 전환을 하지 않아
                            // Compose 의 Main 디스패처에서 OkHttp 호출 -> NetworkOnMainThreadException.
                            // flowOn(Dispatchers.IO) 로 upstream(LLM 호출/tool 실행)을 IO 로 옮긴다.
                            runner.runAsync(
                                userId = USER_ID,
                                sessionId = sessionId,
                                newMessage = Content(
                                    role = AdkRole.USER,
                                    parts = listOf(Part(text = trimmed)),
                                ),
                            ).flowOn(Dispatchers.IO).collect { event ->
                                val text = event.content?.parts?.firstOrNull()?.text
                                if (!text.isNullOrBlank()) reply.append(text)
                            }
                            reply.toString().trim()
                        }.onSuccess { text ->
                            val assistantMsg = ChatMessage(
                                role = Role.Assistant,
                                text = text.ifBlank { "(empty response)" },
                                id = UUID.randomUUID().toString(),
                            )
                            messages = (messages + assistantMsg).toPersistentList()
                        }.onFailure { error ->
                            Timber.e(error, "agent run failed")
                            messages = (messages + ChatMessage(
                                role = Role.Error,
                                text = error.message ?: "Unknown error",
                                id = UUID.randomUUID().toString(),
                            )).toPersistentList()
                        }
                        isSending = false
                    }
                }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.AgentChat::class, ActivityRetainedComponent::class)
    fun interface Factory {
        fun create(navigator: Navigator): AgentChatPresenter
    }

    private companion object {
        // 단일 사용자 데모. 세션 격리는 sessionId 로만 처리.
        const val USER_ID = "billboard_user"
    }
}
