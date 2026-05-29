package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun EmptyGroupPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("이 그룹에 카드가 없어요",
            style = BillboardTheme.typography.bodyMd(),
            color = BillboardTheme.colorScheme.textPrimary)
        Text("홈에서 곡을 길게 눌러 추가하세요",
            style = BillboardTheme.typography.labelMd(),
            color = BillboardTheme.colorScheme.textSecondary)
    }
}

@Composable
@com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
private fun EmptyGroupPlaceholderPreview() {
    BillboardTheme {
        EmptyGroupPlaceholder()
    }
}
