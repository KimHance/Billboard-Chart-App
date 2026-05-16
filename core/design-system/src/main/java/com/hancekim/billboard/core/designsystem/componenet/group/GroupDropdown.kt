package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.icon.ArrowDown
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun GroupDropdown(
    groups: ImmutableList<Group>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val selected = groups.firstOrNull { it.id == selectedId } ?: groups.first()
    val rotation by animateFloatAsState(if (open) 180f else 0f, label = "dropdown-chevron")

    Row(
        modifier = modifier.noRippleClickable { open = !open },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GroupChip(group = selected)
        Icon(
            imageVector = BillboardIcons.ArrowDown,
            contentDescription = if (open) "그룹 선택 닫기" else "그룹 선택 열기",
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { rotationZ = rotation },
            tint = BillboardTheme.colorScheme.textPrimary,
        )
    }

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
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("+ NEW GROUP", style = BillboardTheme.typography.labelMd()) }, // labelSm 미존재 → labelMd 대체
            onClick = {
                open = false
                onCreateNew()
            },
        )
    }
}
