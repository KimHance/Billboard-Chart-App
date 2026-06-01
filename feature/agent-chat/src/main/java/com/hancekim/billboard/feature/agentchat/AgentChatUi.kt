package com.hancekim.billboard.feature.agentchat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.resource.R
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.util.UUID

@CircuitInject(BillboardScreen.AgentChat::class, ActivityRetainedComponent::class)
@Composable
fun AgentChatUi(
    state: AgentChatState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val eventSink = state.eventSink

    BackHandler {
        eventSink(AgentChatEvent.OnBackClick)
    }

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = stringResource(R.string.agent_chat_title),
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { eventSink(AgentChatEvent.OnBackClick) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        ) {
            MessagesList(
                messages = state.messages,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            ChatInputBar(
                input = state.input,
                isSending = state.isSending,
                onInputChange = { eventSink(AgentChatEvent.OnInputChange(it)) },
                onSendClick = { eventSink(AgentChatEvent.OnSendClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: ImmutableList<ChatMessage>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val listState = rememberLazyListState()

    // 새 메시지 추가 시 마지막 항목으로 스크롤.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        Box(
            modifier = modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.agent_chat_empty),
                color = colorScheme.textTertiary,
                style = BillboardTheme.typography.bodyMd(),
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme

    // 역할별 정렬.
    val alignment: Alignment = when (message.role) {
        Role.User -> Alignment.CenterEnd
        Role.Assistant, Role.Tool -> Alignment.CenterStart
        Role.Error -> Alignment.Center
    }
    // 역할별 색. Tool 은 고정 의미(브랜드 amber) 라 BillboardColor primitive 사용.
    val bgColor: Color = when (message.role) {
        Role.User -> colorScheme.accent
        Role.Assistant -> colorScheme.bgCard
        Role.Tool -> BillboardColor.HoloAmber
        Role.Error -> colorScheme.error
    }
    val textColor: Color = when (message.role) {
        Role.User -> colorScheme.onAccent
        Role.Assistant -> colorScheme.textPrimary
        Role.Tool -> BillboardColor.Black
        Role.Error -> BillboardColor.White
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Text(
            text = message.text,
            color = textColor,
            style = BillboardTheme.typography.bodyMd(),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val sendLabel = stringResource(R.string.cd_agent_chat_send)
    // throttledProcess 로 짧은 시간 내 중복 송신 방지.
    val throttledSend = throttledProcess(onProcessed = onSendClick)
    val sendEnabled = input.isNotBlank() && !isSending

    Row(
        modifier = modifier
            .background(colorScheme.bgAppbar)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = stringResource(R.string.agent_chat_input_hint),
                    style = BillboardTheme.typography.bodyMd(),
                    color = colorScheme.textTertiary,
                )
            },
            singleLine = false,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colorScheme.textPrimary,
                unfocusedTextColor = colorScheme.textPrimary,
                focusedContainerColor = colorScheme.bgCard,
                unfocusedContainerColor = colorScheme.bgCard,
                focusedBorderColor = colorScheme.borderButton,
                unfocusedBorderColor = colorScheme.borderButton,
                cursorColor = colorScheme.accent,
            ),
        )

        if (isSending) {
            Box(
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = colorScheme.accent,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            // 송신 버튼: enabled 일 때만 clickable 부착.
            val backgroundColor =
                if (sendEnabled) colorScheme.accent else colorScheme.bgImageFallback
            val sendTextColor =
                if (sendEnabled) colorScheme.onAccent else colorScheme.textTertiary
            val clickModifier =
                if (sendEnabled) Modifier.clickable { throttledSend() } else Modifier
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 64.dp, minHeight = 48.dp)
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .semantics {
                        role = SemanticsRole.Button
                        contentDescription = sendLabel
                    }
                    .then(clickModifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.agent_chat_send),
                    color = sendTextColor,
                    style = BillboardTheme.typography.buttonMd(),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun AgentChatUiPreview() {
    BillboardTheme {
        AgentChatUi(
            state = AgentChatState(
                messages = persistentListOf(
                    ChatMessage(
                        Role.User,
                        "What's the #1 song right now?",
                        UUID.randomUUID().toString(),
                    ),
                    ChatMessage(
                        Role.Assistant,
                        "The current #1 on Billboard Hot 100 is 'Lose Control' by Teddy Swims.",
                        UUID.randomUUID().toString(),
                    ),
                    ChatMessage(
                        Role.Error,
                        "GEMINI_API_KEY not set in local.properties",
                        UUID.randomUUID().toString(),
                    ),
                ).toPersistentList(),
                input = "Tell me more",
                isSending = false,
            ) {},
        )
    }
}
