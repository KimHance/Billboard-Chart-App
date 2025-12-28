package com.hancekim.billboard.core.designsystem.componenet.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun BillboardAlert(
    onClick: () -> Unit,
    title: String = "",
    body: String = "",
    buttonLabel: String = "",
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = false),
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BillboardTheme.colorScheme.bgCard)
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = BillboardTheme.typography.headingLg(),
                    color = BillboardTheme.colorScheme.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }

            if (body.isNotEmpty()) {
                Text(
                    text = body,
                    modifier = Modifier.fillMaxWidth(),
                    style = BillboardTheme.typography.titleMd(),
                    color = BillboardTheme.colorScheme.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            TextButton(
                onClick = throttledProcess(onProcessed = onClick),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = true,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonColors(
                    containerColor = BillboardTheme.colorScheme.borderButton,
                    contentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified
                ),
            ) {
                Text(
                    text = buttonLabel,
                    style = BillboardTheme.typography.buttonMd(),
                    color = BillboardTheme.colorScheme.textPrimary
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BillboardAlertPreview() {
    BillboardTheme {
        Box(Modifier.fillMaxSize()) {
            BillboardAlert(
                onClick = {},
                title = "네트워크 연결 확인",
                body = "네트워크 연결을 확인해주세요",
                buttonLabel = "확인",
                onDismissRequest = {}
            )
        }
    }
}