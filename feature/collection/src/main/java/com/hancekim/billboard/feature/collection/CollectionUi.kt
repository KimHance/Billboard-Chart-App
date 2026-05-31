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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    val colorScheme = BillboardTheme.colorScheme
    // drawerState 가 사이드바 단일 SoT — Presenter 는 sidebarOpen 을 들고 있지 않다.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openSidebar = remember(drawerState, scope) { { scope.launch { drawerState.open() } } }
    val closeSidebar = remember(drawerState, scope) { { scope.launch { drawerState.close() } } }

    // 사이드바 열림 상태면 백프레스가 사이드바만 닫는다 — 화면 자체 pop 은 두 번째 백프레스에서.
    BackHandler {
        if (drawerState.isOpen) {
            closeSidebar()
        } else {
            state.eventSink(CollectionEvent.OnBackClick)
        }
    }

    // 그룹 변경(선택/새 그룹 생성 성공) 시 사이드바 자동 닫기 — 최초 컴포지션 때는 drawer 가 닫혀 있어 no-op.
    LaunchedEffect(state.currentGroupId) {
        if (drawerState.isOpen) drawerState.close()
    }

    // ModalNavigationDrawer 는 좌측에서 열림이 기본 — RTL 트릭으로 우측 배치.
    // drawer 내용물은 다시 Ltr 로 되돌려 일반 레이아웃 유지.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            // 스와이프 제스처로 열고 닫는 동작은 차단 — 메뉴 버튼 / 스크림 탭 / 백프레스만 토글.
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
                            onClose = { closeSidebar() },
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
                    onOpenSidebar = { openSidebar() },
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
                newGroupForm = null,
                pendingDeleteGroupId = null,
                eventSink = {},
            ),
        )
    }
}

// 사이드바 열린 상태 프리뷰는 drawerState 가 UI 내부 SoT 라 더 이상 의미가 없어 제거.

