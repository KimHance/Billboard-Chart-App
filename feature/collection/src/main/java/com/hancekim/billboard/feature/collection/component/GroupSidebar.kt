package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoDelete
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.group.GroupDot
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

// DismissibleDrawerSheet 안에 들어가는 본문. open/close 애니메이션은 Drawer 가 담당.
@Composable
fun GroupSidebar(
    groups: ImmutableList<Group>,
    currentGroupId: Long,
    countsByGroupId: ImmutableMap<Long, Int>,
    pendingDeleteGroupId: Long?,
    newGroupForm: NewGroupFormState?,
    onClose: () -> Unit,
    onSelectGroup: (Long) -> Unit,
    onRequestDelete: (Long) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onNewGroupClick: () -> Unit,
    onCancelNewGroup: () -> Unit,
    onNewGroupNameChange: (String) -> Unit,
    onNewGroupColorSelect: (Int) -> Unit,
    onSubmitNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "GROUPS",
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textPrimary,
            )
            Text(
                text = "✕",
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textSecondary,
                modifier = Modifier
                    .noRippleClickable { onClose() }
                    .padding(4.dp),
            )
        }
        groups.forEach { g ->
            Row(
                modifier = Modifier
                    .background(
                        if (g.id == currentGroupId) Color(g.colorArgb).copy(alpha = 0.12f)
                        else Color.Transparent,
                        RoundedCornerShape(6.dp),
                    )
                    .noRippleClickable { onSelectGroup(g.id) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GroupDot(colorArgb = g.colorArgb)
                Text(
                    "${g.name}  (${countsByGroupId[g.id] ?: 0})",
                    style = BillboardTheme.typography.bodyMd(),
                    color = colorScheme.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (g.id != Group.DEFAULT_ID) {
                    Icon(
                        imageVector = BillboardIcons.IcoDelete,
                        contentDescription = "${g.name} 삭제",
                        modifier = Modifier
                            .size(20.dp)
                            .noRippleClickable { onRequestDelete(g.id) },
                        tint = colorScheme.error,
                    )
                }
            }
            if (pendingDeleteGroupId == g.id) {
                Column(
                    modifier = Modifier
                        .background(
                            colorScheme.error.copy(alpha = 0.12f),
                            RoundedCornerShape(6.dp),
                        )
                        .padding(8.dp),
                ) {
                    Text(
                        "${countsByGroupId[g.id] ?: 0}개 카드가 함께 삭제됩니다",
                        style = BillboardTheme.typography.labelMd(),
                        color = colorScheme.error,
                    )
                    Row {
                        TextButton(onClick = onCancelDelete) {
                            Text("CANCEL", color = colorScheme.textSecondary)
                        }
                        TextButton(onClick = onConfirmDelete) {
                            Text("DELETE", color = colorScheme.error)
                        }
                    }
                }
            }
        }
        if (newGroupForm != null) {
            NewGroupForm(
                form = newGroupForm,
                onNameChange = onNewGroupNameChange,
                onColorSelect = onNewGroupColorSelect,
                onSubmit = onSubmitNewGroup,
                onCancel = onCancelNewGroup,
            )
        } else {
            TextButton(onClick = onNewGroupClick) {
                Text("+ NEW GROUP", color = colorScheme.textPrimary)
            }
        }
    }
}

@Composable
@com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
private fun GroupSidebarPreview() {
    BillboardTheme {
        GroupSidebar(
            groups = kotlinx.collections.immutable.persistentListOf(
                Group(Group.DEFAULT_ID, "Starred", 0xFF00FF85.toInt(), 0L),
                Group(2L, "Workout", 0xFFFFA000.toInt(), 0L),
            ),
            currentGroupId = Group.DEFAULT_ID,
            countsByGroupId = kotlinx.collections.immutable.persistentMapOf(Group.DEFAULT_ID to 12, 2L to 4),
            pendingDeleteGroupId = null,
            newGroupForm = null,
            onClose = {}, onSelectGroup = {}, onRequestDelete = {}, onConfirmDelete = {},
            onCancelDelete = {}, onNewGroupClick = {}, onCancelNewGroup = {},
            onNewGroupNameChange = {}, onNewGroupColorSelect = {}, onSubmitNewGroup = {},
        )
    }
}
