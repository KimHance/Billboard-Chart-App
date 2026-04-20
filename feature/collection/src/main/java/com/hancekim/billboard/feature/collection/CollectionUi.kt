package com.hancekim.billboard.feature.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.icon.ArrowBack
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designsystem.componenet.header.BillboardHeader
import com.hancekim.billboard.feature.collection.component.OrbitLayout
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0F1729), Color(0xFF020618)),
                    radius = 1200f,
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BillboardHeader(
                title = "COLLECTION",
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { state.eventSink(CollectionEvent.OnBackClick) },
            )

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
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF98A2B3),
                letterSpacing = 1.5.sp,
            )
        }
    }
}
