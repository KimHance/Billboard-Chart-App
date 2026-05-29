package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.resource.R

@Composable
fun EmptyGroupPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.collection_empty_title),
            style = BillboardTheme.typography.bodyMd(),
            color = BillboardTheme.colorScheme.textPrimary,
        )
        Text(
            text = stringResource(R.string.collection_empty_subtitle),
            style = BillboardTheme.typography.labelMd(),
            color = BillboardTheme.colorScheme.textSecondary,
        )
    }
}

@Composable
@com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
private fun EmptyGroupPlaceholderPreview() {
    BillboardTheme {
        EmptyGroupPlaceholder()
    }
}
