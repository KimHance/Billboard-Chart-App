package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.ArrowDown
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.resource.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// 디자인 시스템에 갇힌 ViewObject — 호출 측에서 도메인/데이터 모델을 이 형식으로 매핑해 넘긴다.
// :core:design-system 이 도메인/데이터 레이어를 알지 못하도록 격리.
data class GroupBadge(
    val id: Long,
    val name: String,
    val colorArgb: Int,
)

@Composable
fun GroupDropdown(
    items: ImmutableList<GroupBadge>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    // items 가 비어있을 수 있는 시점(첫 emit 전, 전체 삭제 직후) 방어
    val selected = items.firstOrNull { it.id == selectedId } ?: items.firstOrNull()
    // animateFloatAsState 결과 자체를 들고, graphicsLayer 람다(draw phase) 안에서 .value read
    // → 회전 동안 컴포지션 비용 0 (03-compose-state.md deferred read).
    val rotationState = animateFloatAsState(if (open) 180f else 0f, label = "dropdown-chevron")

    if (selected == null) {
        Box(modifier = modifier)
        return
    }

    // 그룹이 2개 이상일 때만 펼침 가능 — 디폴트만 있을 땐 칩만 표시
    val canExpand = items.size >= 2

    Box(modifier = modifier) {
        Row(
            modifier = if (canExpand) {
                Modifier.noRippleClickable { open = !open }
            } else {
                Modifier
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GroupChip(name = selected.name, colorArgb = selected.colorArgb)
            if (canExpand) {
                Icon(
                    imageVector = BillboardIcons.ArrowDown,
                    contentDescription = stringResource(
                        if (open) R.string.cd_collapse_group_selector
                        else R.string.cd_expand_group_selector,
                    ),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotationState.value },
                    tint = BillboardTheme.colorScheme.textPrimary,
                )
            }
        }

        if (canExpand) {
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name, style = BillboardTheme.typography.bodyMd()) },
                        leadingIcon = { GroupDot(colorArgb = item.colorArgb) },
                        onClick = {
                            open = false
                            onSelect(item.id)
                        },
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun GroupDropdownMultiplePreview() {
    BillboardTheme {
        GroupDropdown(
            items = persistentListOf(
                GroupBadge(1L, "Starred", BillboardColor.Green400.toArgb()),
                GroupBadge(2L, "Workout", BillboardColor.HoloAmber.toArgb()),
            ),
            selectedId = 1L,
            onSelect = {},
        )
    }
}

@ThemePreviews
@Composable
private fun GroupDropdownSinglePreview() {
    BillboardTheme {
        GroupDropdown(
            items = persistentListOf(
                GroupBadge(1L, "Starred", BillboardColor.Green400.toArgb()),
            ),
            selectedId = 1L,
            onSelect = {},
        )
    }
}
