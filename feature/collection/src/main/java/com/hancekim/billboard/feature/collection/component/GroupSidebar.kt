package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoDelete
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.group.GroupDot
import com.hancekim.billboard.core.domain.model.Group
import com.hancekim.billboard.core.resource.R
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
                text = stringResource(R.string.groups_header),
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textPrimary,
            )
            val closeLabel = stringResource(R.string.cd_close_group_sidebar)
            Text(
                text = "✕",
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textSecondary,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { role = Role.Button; contentDescription = closeLabel }
                    .noRippleClickable { onClose() }
                    .wrapContentSize(Alignment.Center),
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
                    val throttledDelete = throttledProcess(id = "groupDelete-${g.id}") { onRequestDelete(g.id) }
                    Icon(
                        imageVector = BillboardIcons.IcoDelete,
                        contentDescription = stringResource(R.string.cd_delete_group, g.name),
                        modifier = Modifier
                            .size(20.dp)
                            .noRippleClickable(onClick = throttledDelete),
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
                        text = stringResource(R.string.groups_delete_warning, countsByGroupId[g.id] ?: 0),
                        style = BillboardTheme.typography.labelMd(),
                        color = colorScheme.error,
                    )
                    Row {
                        TextButton(onClick = onCancelDelete) {
                            Text(stringResource(R.string.action_cancel), color = colorScheme.textSecondary)
                        }
                        TextButton(onClick = onConfirmDelete) {
                            Text(stringResource(R.string.action_delete), color = colorScheme.error)
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
                Text(stringResource(R.string.groups_new), color = colorScheme.textPrimary)
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
                Group(Group.DEFAULT_ID, Group.DEFAULT_NAME, Group.DEFAULT_COLOR_ARGB, 0L),
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
