package com.hancekim.billboard.core.designsystem.componenet.title

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

enum class TitleSize {
    Medium, Large
}

@Composable
fun TitleSection(
    title: String,
    size: TitleSize,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(start = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            color = BillboardTheme.colorScheme.textHeading,
            style = if (size == TitleSize.Medium) BillboardTheme.typography.headingMd() else BillboardTheme.typography.headingXl()
        )
    }
}

private class TitleSizePreviewParameterProvider : PreviewParameterProvider<TitleSize> {
    override val values: Sequence<TitleSize>
        get() = TitleSize.entries.asSequence()
}

@ThemePreviews
@Composable
private fun TitleSectionPreview(
    @PreviewParameter(TitleSizePreviewParameterProvider::class) size: TitleSize
) {
    BillboardTheme {
        TitleSection(
            title = "BILLBOARD HOT 100™",
            size = size
        )
    }
}