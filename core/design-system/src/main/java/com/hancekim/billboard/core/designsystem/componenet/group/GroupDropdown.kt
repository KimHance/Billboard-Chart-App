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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.icon.ArrowDown
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun GroupDropdown(
    groups: ImmutableList<Group>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    // groups 가 비어있을 수 있는 시점(첫 emit 전, 전체 삭제 직후) 방어
    val selected = groups.firstOrNull { it.id == selectedId } ?: groups.firstOrNull()
    val rotation by animateFloatAsState(if (open) 180f else 0f, label = "dropdown-chevron")

    if (selected == null) {
        Box(modifier = modifier)
        return
    }

    // 그룹이 2개 이상일 때만 펼침 가능 — 디폴트만 있을 땐 칩만 표시
    val canExpand = groups.size >= 2

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
            GroupChip(group = selected)
            if (canExpand) {
                Icon(
                    imageVector = BillboardIcons.ArrowDown,
                    contentDescription = if (open) "그룹 선택 닫기" else "그룹 선택 열기",
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotation },
                    tint = BillboardTheme.colorScheme.textPrimary,
                )
            }
        }

        if (canExpand) {
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name, style = BillboardTheme.typography.bodyMd()) },
                        leadingIcon = { GroupDot(colorArgb = group.colorArgb) },
                        onClick = {
                            open = false
                            onSelect(group.id)
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
            groups = persistentListOf(
                Group(1L, "Starred", BillboardColor.Green400.toArgb(), 0L),
                Group(2L, "Workout", BillboardColor.HoloAmber.toArgb(), 0L),
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
            groups = persistentListOf(
                Group(1L, "Starred", BillboardColor.Green400.toArgb(), 0L),
            ),
            selectedId = 1L,
            onSelect = {},
        )
    }
}
