package com.hancekim.billboard.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(BillboardScreen.Home::class, ActivityRetainedComponent::class)
@Composable
fun HomeUi(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = BillboardTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader { }
        },
        content = {}
    )
}

@ThemePreviews
@Composable
private fun HomeUiPreview() {
    BillboardTheme {
        HomeUi(
            state = HomeState,
            modifier = Modifier.fillMaxSize()
        )
    }
}