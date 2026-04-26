package com.hancekim.billboard.feature.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.feature.collection.component.OrbitLayout
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf

@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    val colorScheme = BillboardTheme.colorScheme

    BackHandler { state.eventSink(CollectionEvent.OnBackClick) }

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = "COLLECTION",
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { state.eventSink(CollectionEvent.OnBackClick) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colorScheme.bgCard, colorScheme.bgApp),
                        radius = 1200f,
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    OrbitLayout(
                        cards = state.cards,
                        onCardClick = { state.eventSink(CollectionEvent.OnCardClick(it)) },
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    text = if (state.cards.isEmpty()) "Long-press a track to collect it as a card."
                    else "TAP A CARD TO INSPECT",
                    textAlign = TextAlign.Center,
                    style = BillboardTheme.typography.labelMd(),
                    color = colorScheme.textTertiary,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CollectionUiPreview() {
    BillboardTheme {
        CollectionUi(
            state = CollectionState(cards = persistentListOf(), eventSink = {}),
        )
    }
}
