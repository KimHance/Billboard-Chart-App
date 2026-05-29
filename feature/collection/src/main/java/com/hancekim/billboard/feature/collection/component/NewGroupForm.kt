package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.resource.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// 그룹 색상 팔레트: BillboardColor 의 강조 색 중 시각적으로 구분 가능한 5개.
private val GroupColorPalette: ImmutableList<Color> = persistentListOf(
    BillboardColor.HoloGreen,   // 초록
    BillboardColor.Red500,      // 빨강
    BillboardColor.HoloAmber,   // 주황/노랑
    BillboardColor.HoloMagenta, // 보라
    BillboardColor.HoloBlue,    // 파랑
)

@Composable
fun NewGroupForm(
    form: NewGroupFormState,
    onNameChange: (String) -> Unit,
    onColorSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme
    val canSubmit = form.name.trim().isNotEmpty() && form.colorArgb != null && !form.isDuplicate

    Column(
        modifier = modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 제목 + 이름 입력 섹션
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.new_group_section_name),
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textSecondary,
            )
            BasicTextField(
                value = form.name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = BillboardTheme.typography.bodyMd()
                    .copy(color = colorScheme.textPrimary),
                cursorBrush = SolidColor(colorScheme.textPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.bgApp, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
            if (form.isDuplicate) {
                Text(
                    text = stringResource(R.string.new_group_duplicate_error),
                    color = colorScheme.error,
                    style = BillboardTheme.typography.labelMd(),
                )
            }
        }

        // 색상 선택 섹션
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.new_group_section_color),
                style = BillboardTheme.typography.labelMd(),
                color = colorScheme.textSecondary,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(GroupColorPalette) { color ->
                    val argb = color.toArgb()
                    val selected = form.colorArgb == argb
                    val selectLabel = stringResource(R.string.cd_select_color)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) colorScheme.textPrimary else Color.Transparent,
                                shape = CircleShape,
                            )
                            .noRippleClickable { onColorSelect(argb) }
                            .semantics {
                                role = Role.RadioButton
                                this.selected = selected
                                contentDescription = selectLabel
                            },
                    )
                }
            }
        }

        // 액션 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_cancel), color = colorScheme.textSecondary)
            }
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.textPrimary,
                    contentColor = colorScheme.bgApp,
                    disabledContainerColor = colorScheme.textSecondary.copy(alpha = 0.3f),
                    disabledContentColor = colorScheme.textSecondary,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_add))
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
@ThemePreviews
private fun NewGroupFormEmptyPreview() {
    BillboardTheme {
        NewGroupForm(
            form = NewGroupFormState(name = "", colorArgb = null, isDuplicate = false),
            onNameChange = {}, onColorSelect = {}, onSubmit = {}, onCancel = {},
        )
    }
}

@Composable
@ThemePreviews
private fun NewGroupFormFilledPreview() {
    BillboardTheme {
        NewGroupForm(
            form = NewGroupFormState(
                name = "Workout",
                colorArgb = BillboardColor.HoloAmber.toArgb(),
                isDuplicate = false,
            ),
            onNameChange = {}, onColorSelect = {}, onSubmit = {}, onCancel = {},
        )
    }
}

@Composable
@ThemePreviews
private fun NewGroupFormDuplicatePreview() {
    BillboardTheme {
        NewGroupForm(
            form = NewGroupFormState(
                name = "Starred",
                colorArgb = BillboardColor.HoloBlue.toArgb(),
                isDuplicate = true,
            ),
            onNameChange = {}, onColorSelect = {}, onSubmit = {}, onCancel = {},
        )
    }
}
