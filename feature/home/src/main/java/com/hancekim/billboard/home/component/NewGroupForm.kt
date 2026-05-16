package com.hancekim.billboard.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun NewGroupForm(
    form: NewGroupFormState,
    onNameChange: (String) -> Unit,
    onHexChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(12.dp)) {
        BasicTextField(
            value = form.name, onValueChange = onNameChange,
            textStyle = BillboardTheme.typography.bodyMd().copy(color = BillboardTheme.colorScheme.textPrimary),
            modifier = Modifier.background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(6.dp)).padding(8.dp),
        )
        if (form.isDuplicate) {
            Text(
                "이미 같은 이름의 그룹이 있어요",
                color = BillboardTheme.colorScheme.error,
                style = BillboardTheme.typography.bodyMd(),
            )
        }
        BasicTextField(
            value = form.hex, onValueChange = onHexChange,
            textStyle = BillboardTheme.typography.bodyMd().copy(color = BillboardTheme.colorScheme.textPrimary),
            modifier = Modifier.background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(6.dp)).padding(8.dp),
        )
        Row {
            if (form.isHexValid) {
                androidx.compose.foundation.layout.Box(
                    Modifier.size(20.dp).background(Color(android.graphics.Color.parseColor(form.hex)), RoundedCornerShape(50)),
                )
            }
        }
        Row {
            TextButton(onClick = onCancel) { Text("CANCEL") }
            TextButton(
                onClick = onSubmit,
                enabled = !form.isDuplicate && form.isHexValid && form.name.trim().isNotEmpty(),
            ) { Text("ADD") }
        }
    }
}
