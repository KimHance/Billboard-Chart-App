package com.hancekim.billboard.feature.collection.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

private val SidebarEasing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

@Composable
fun GroupSidebar(
    isOpen: Boolean,
    groups: ImmutableList<Group>,
    currentGroupId: Long,
    countsByGroupId: ImmutableMap<Long, Int>,
    pendingDeleteGroupId: Long?,
    newGroupForm: NewGroupFormState?,
    onToggle: (Boolean) -> Unit,
    onSelectGroup: (Long) -> Unit,
    onRequestDelete: (Long) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onNewGroupClick: () -> Unit,
    onCancelNewGroup: () -> Unit,
    onNewGroupNameChange: (String) -> Unit,
    onNewGroupHexChange: (String) -> Unit,
    onSubmitNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) {
        // 닫힌 상태: 14dp 세로 컬러 바 + 탭으로 열기
        val current = groups.firstOrNull { it.id == currentGroupId } ?: return
        Box(
            modifier = modifier
                .width(14.dp)
                .fillMaxHeight()
                .noRippleClickable { onToggle(true) },
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp)
                    .background(Color(current.colorArgb), RoundedCornerShape(2.dp)),
            )
        }
        return
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(animationSpec = tween(280, easing = SidebarEasing)) { it },
        exit = slideOutHorizontally(animationSpec = tween(280, easing = SidebarEasing)) { it },
    ) {
        Column(
            modifier = modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(BillboardTheme.colorScheme.bgCard)
                .border(1.dp, Color.White.copy(alpha = 0.08f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("GROUPS", style = BillboardTheme.typography.labelMd())
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
                        modifier = Modifier.weight(1f),
                    )
                    if (g.id != Group.DEFAULT_ID) {
                        Icon(
                            imageVector = BillboardIcons.IcoDelete,
                            contentDescription = "${g.name} 삭제",
                            modifier = Modifier
                                .size(20.dp)
                                .noRippleClickable { onRequestDelete(g.id) },
                            tint = BillboardTheme.colorScheme.error,
                        )
                    }
                }
                if (pendingDeleteGroupId == g.id) {
                    Column(
                        modifier = Modifier
                            .background(
                                BillboardTheme.colorScheme.error.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp),
                            )
                            .padding(8.dp),
                    ) {
                        Text(
                            "${countsByGroupId[g.id] ?: 0}개 카드가 함께 삭제됩니다",
                            style = BillboardTheme.typography.labelMd(),
                            color = BillboardTheme.colorScheme.error,
                        )
                        Row {
                            TextButton(onClick = onCancelDelete) { Text("CANCEL") }
                            TextButton(onClick = onConfirmDelete) {
                                Text("DELETE", color = BillboardTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (newGroupForm != null) {
                NewGroupForm(
                    form = newGroupForm,
                    onNameChange = onNewGroupNameChange,
                    onHexChange = onNewGroupHexChange,
                    onSubmit = onSubmitNewGroup,
                    onCancel = onCancelNewGroup,
                )
            } else {
                TextButton(onClick = onNewGroupClick) { Text("+ NEW GROUP") }
            }
        }
    }
}
