package com.hancekim.billboard.feature.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.domain.model.CollectedCard
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.Menu
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.core.resource.R
import com.hancekim.billboard.feature.collection.component.CollectionDivider
import com.hancekim.billboard.feature.collection.component.CollectionSubline
import com.hancekim.billboard.feature.collection.component.GroupSidebar
import com.hancekim.billboard.feature.collection.component.MiniRail
import com.hancekim.billboard.feature.collection.component.MiniRailEmpty
import com.hancekim.billboard.feature.collection.component.NowPlayingDeck
import com.hancekim.billboard.feature.collection.component.NowPlayingDeckEmpty
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    val colorScheme = BillboardTheme.colorScheme
    // 사이드바 열림 상태면 백프레스가 사이드바만 닫는다 — 화면 자체 pop 은 두 번째 백프레스에서.
    BackHandler {
        if (state.sidebarOpen) {
            state.eventSink(CollectionEvent.OnSidebarToggle(false))
        } else {
            state.eventSink(CollectionEvent.OnBackClick)
        }
    }

    // DismissibleNavigationDrawer 는 좌측에서 열림이 기본 — RTL 트릭으로 우측 배치.
    // drawer 내용물은 다시 Ltr 로 되돌려 일반 레이아웃 유지.
    val drawerState = rememberDrawerState(
        if (state.sidebarOpen) DrawerValue.Open else DrawerValue.Closed,
    )

    // state.sidebarOpen → drawerState 동기화 (외부 트리거)
    LaunchedEffect(state.sidebarOpen) {
        if (state.sidebarOpen && !drawerState.isOpen) drawerState.open()
        if (!state.sidebarOpen && drawerState.isOpen) drawerState.close()
    }
    // 사용자 제스처/탭으로 drawer 가 바뀌면 state 도 따라가게
    LaunchedEffect(drawerState.currentValue) {
        val open = drawerState.currentValue == DrawerValue.Open
        if (open != state.sidebarOpen) state.eventSink(CollectionEvent.OnSidebarToggle(open))
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            // 스와이프 제스처로 열고 닫는 동작은 차단 — 헤더의 메뉴 버튼으로만 토글.
            gesturesEnabled = false,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier.width(260.dp),
                        drawerContainerColor = colorScheme.bgCard,
                    ) {
                        GroupSidebar(
                            groups = state.groups,
                            currentGroupId = state.currentGroupId,
                            countsByGroupId = state.countsByGroupId,
                            pendingDeleteGroupId = state.pendingDeleteGroupId,
                            newGroupForm = state.newGroupForm,
                            onClose = { state.eventSink(CollectionEvent.OnSidebarToggle(false)) },
                            onSelectGroup = { state.eventSink(CollectionEvent.OnSelectGroup(it)) },
                            onRequestDelete = { state.eventSink(CollectionEvent.OnRequestDeleteGroup(it)) },
                            onConfirmDelete = { state.eventSink(CollectionEvent.OnConfirmDeleteGroup) },
                            onCancelDelete = { state.eventSink(CollectionEvent.OnCancelDeleteGroup) },
                            onNewGroupClick = { state.eventSink(CollectionEvent.OnNewGroupClick) },
                            onCancelNewGroup = { state.eventSink(CollectionEvent.OnCancelNewGroup) },
                            onNewGroupNameChange = { state.eventSink(CollectionEvent.OnNewGroupNameChange(it)) },
                            onNewGroupColorSelect = { state.eventSink(CollectionEvent.OnNewGroupColorSelect(it)) },
                            onSubmitNewGroup = { state.eventSink(CollectionEvent.OnSubmitNewGroup) },
                        )
                    }
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                CollectionContent(
                    state = state,
                    onOpenSidebar = { state.eventSink(CollectionEvent.OnSidebarToggle(true)) },
                )
            }
        }
    }
}

@Composable
private fun CollectionContent(
    state: CollectionState,
    onOpenSidebar: () -> Unit,
) {
    val colorScheme = BillboardTheme.colorScheme
    val openSidebarLabel = stringResource(R.string.cd_open_group_sidebar)
    Scaffold(
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = stringResource(R.string.collection_title),
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = BillboardIcons.Menu,
                trailingIconContentDescription = openSidebarLabel,
                onLeadingIconClick = { state.eventSink(CollectionEvent.OnBackClick) },
                onTrailingIconClick = onOpenSidebar,
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
                if (currentGroup != null) {
                    CollectionSubline(
                        totalCount = state.countsByGroupId.values.sum(),
                        inGroupCount = state.cardsInCurrentGroup.size,
                        group = currentGroup,
                    )
                }
                // [3]+[4] NowPlayingDeck + INSPECT, 세로 중앙 정렬을 위해 weight(1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    val currentCard = state.cardsInCurrentGroup
                        .firstOrNull { it.key == state.nowPlayingKey }
                        ?: state.cardsInCurrentGroup.firstOrNull()

                    if (currentCard != null && currentGroup != null) {
                        NowPlayingDeck(
                            card = currentCard,
                            group = currentGroup,
                            onInspect = { state.eventSink(CollectionEvent.OnInspectClick) },
                        )
                    } else {
                        NowPlayingDeckEmpty()
                    }
                }
                // [5] 디바이더
                CollectionDivider()
                // [6] MiniRail
                if (state.cardsInCurrentGroup.isNotEmpty()) {
                    MiniRail(
                        cards = state.cardsInCurrentGroup,
                        activeKey = state.nowPlayingKey,
                        onSelect = { state.eventSink(CollectionEvent.OnSelectCard(it)) },
                        onRemove = { state.eventSink(CollectionEvent.OnRemoveCard(it)) },
                    )
                } else if (currentGroup != null) {
                    MiniRailEmpty(group = currentGroup)
                }
            }
        }
    }
}

private val previewGroups = persistentListOf(
    Group(Group.DEFAULT_ID, "Starred", 0xFF00FF85.toInt(), 0L),
    Group(2L, "Workout", 0xFFFFA000.toInt(), 0L),
    Group(3L, "Chill", 0xFF8E7BFF.toInt(), 0L),
)

private val previewCards = persistentListOf(
    CollectedCard("a", "Song A", "Artist A", "", 0L, 1, 1, 4),
    CollectedCard("b", "Song B", "Artist B", "", 0L, 3, 2, 7),
    CollectedCard("c", "Song C", "Artist C", "", 0L, 5, 4, 12),
)

private val previewCounts = persistentMapOf(
    Group.DEFAULT_ID to 12,
    2L to 4,
    3L to 7,
)

@ThemePreviews
@Composable
private fun CollectionUiSidebarClosedPreview() {
    BillboardTheme {
        CollectionUi(
            state = CollectionState(
                groups = previewGroups,
                currentGroupId = Group.DEFAULT_ID,
                cardsInCurrentGroup = previewCards,
                countsByGroupId = previewCounts,
                nowPlayingKey = "a",
                sidebarOpen = false,
                newGroupForm = null,
                pendingDeleteGroupId = null,
                eventSink = {},
            ),
        )
    }
}

@ThemePreviews
@Composable
private fun CollectionUiSidebarOpenPreview() {
    BillboardTheme {
        CollectionUi(
            state = CollectionState(
                groups = previewGroups,
                currentGroupId = Group.DEFAULT_ID,
                cardsInCurrentGroup = previewCards,
                countsByGroupId = previewCounts,
                nowPlayingKey = "a",
                sidebarOpen = true,
                newGroupForm = null,
                pendingDeleteGroupId = null,
                eventSink = {},
            ),
        )
    }
}
