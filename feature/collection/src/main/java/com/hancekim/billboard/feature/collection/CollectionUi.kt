package com.hancekim.billboard.feature.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.group.GroupChip
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.feature.collection.component.EmptyGroupPlaceholder
import com.hancekim.billboard.feature.collection.component.GroupSidebar
import com.hancekim.billboard.feature.collection.component.MiniRail
import com.hancekim.billboard.feature.collection.component.NowPlayingPlayer
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    val colorScheme = BillboardTheme.colorScheme
    BackHandler { state.eventSink(CollectionEvent.OnBackClick) }

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = "COLLECTION",
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { state.eventSink(CollectionEvent.OnBackClick) },
            )
        },
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(Modifier.fillMaxSize()) {
                val currentGroup = state.groups.firstOrNull { it.id == state.currentGroupId }
                Text(
                    text = "${state.cardsInCurrentGroup.size} IN ${currentGroup?.name?.uppercase() ?: "—"}",
                    style = BillboardTheme.typography.labelMd(),
                    color = currentGroup?.colorArgb?.let { Color(it) } ?: colorScheme.textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (state.cardsInCurrentGroup.isEmpty()) {
                    NowPlayingPlayer(playerState = null)
                    Spacer(Modifier.weight(1f))
                    EmptyGroupPlaceholder()
                } else {
                    val currentCard = state.cardsInCurrentGroup.firstOrNull { it.key == state.nowPlayingKey }
                        ?: state.cardsInCurrentGroup.first()
                    NowPlayingPlayer(playerState = state.playerState)
                    Spacer(Modifier.height(8.dp))
                    currentGroup?.let { GroupChip(it, Modifier.padding(horizontal = 16.dp)) }
                    Text(
                        text = currentCard.title,
                        style = BillboardTheme.typography.titleMd(),
                        color = colorScheme.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Text(
                        text = currentCard.artist,
                        style = BillboardTheme.typography.bodyMd(),
                        color = colorScheme.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${state.cardsInCurrentGroup.size} CARDS",
                        style = BillboardTheme.typography.labelMd(),
                        color = colorScheme.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    MiniRail(
                        cards = state.cardsInCurrentGroup,
                        activeKey = state.nowPlayingKey,
                        onSelect = { state.eventSink(CollectionEvent.OnSelectCard(it)) },
                    )
                }
            }
            if (state.nowPlayingKey != null) {
                Text(
                    text = "INSPECT",
                    style = BillboardTheme.typography.labelMd(),
                    color = colorScheme.textPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 72.dp, end = 16.dp)
                        .noRippleClickable { state.eventSink(CollectionEvent.OnInspectClick) }
                        .semantics {
                            role = Role.Button
                            contentDescription = "카드 상세 보기"
                        },
                )
            }
            GroupSidebar(
                modifier = Modifier.align(Alignment.CenterEnd),
                isOpen = state.sidebarOpen,
                groups = state.groups,
                currentGroupId = state.currentGroupId,
                countsByGroupId = state.countsByGroupId,
                pendingDeleteGroupId = state.pendingDeleteGroupId,
                newGroupForm = state.newGroupForm,
                onToggle = { state.eventSink(CollectionEvent.OnSidebarToggle(it)) },
                onSelectGroup = { state.eventSink(CollectionEvent.OnSelectGroup(it)) },
                onRequestDelete = { state.eventSink(CollectionEvent.OnRequestDeleteGroup(it)) },
                onConfirmDelete = { state.eventSink(CollectionEvent.OnConfirmDeleteGroup) },
                onCancelDelete = { state.eventSink(CollectionEvent.OnCancelDeleteGroup) },
                onNewGroupClick = { state.eventSink(CollectionEvent.OnNewGroupClick) },
                onCancelNewGroup = { state.eventSink(CollectionEvent.OnCancelNewGroup) },
                onNewGroupNameChange = { state.eventSink(CollectionEvent.OnNewGroupNameChange(it)) },
                onNewGroupHexChange = { state.eventSink(CollectionEvent.OnNewGroupHexChange(it)) },
                onSubmitNewGroup = { state.eventSink(CollectionEvent.OnSubmitNewGroup) },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CollectionUiPreview() {
    BillboardTheme {
        CollectionUi(
            state = CollectionState(
                groups = persistentListOf(),
                currentGroupId = Group.DEFAULT_ID,
                cardsInCurrentGroup = persistentListOf(),
                countsByGroupId = persistentMapOf(),
                nowPlayingKey = null,
                playerState = null,
                sidebarOpen = false,
                newGroupForm = null,
                pendingDeleteGroupId = null,
                eventSink = {},
            ),
        )
    }
}
