package com.hancekim.billboard.feature.agentchat

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// 채팅 메시지의 발화 주체. 색상 / 정렬을 구분하기 위해 enum 으로 분리.
enum class Role {
    User,
    Assistant,
    Tool,
    Error,
}

@Stable
data class ChatMessage(
    val role: Role,
    val text: String,
    val id: String,
)

@Stable
data class AgentChatState(
    val messages: ImmutableList<ChatMessage> = persistentListOf(),
    val input: String = "",
    val isSending: Boolean = false,
    // eventSink 는 반드시 맨 마지막에 위치 (Circuit 컨벤션).
    val eventSink: (AgentChatEvent) -> Unit,
) : CircuitUiState

sealed interface AgentChatEvent : CircuitUiEvent {
    data object OnBackClick : AgentChatEvent
    data class OnInputChange(val text: String) : AgentChatEvent
    data object OnSendClick : AgentChatEvent
}
