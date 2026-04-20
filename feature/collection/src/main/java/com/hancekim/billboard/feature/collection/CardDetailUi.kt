package com.hancekim.billboard.feature.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoClose
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(BillboardScreen.CardDetail::class, ActivityRetainedComponent::class)
@Composable
fun CardDetailUi(state: CardDetailState, modifier: Modifier = Modifier) {
    val card = state.card ?: return

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
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .noRippleClickable { state.eventSink(CardDetailEvent.OnCloseClick) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BillboardIcons.IcoClose,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(420.dp)
                        .blur(14.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFC8DCF0).copy(alpha = 0.3f), Color.Transparent),
                            ),
                        ),
                )
                HoloCard(
                    albumArtUrl = card.albumArtUrl,
                    cardSize = 280.dp,
                    interactive = true,
                    autoSpeed = 20f,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(label = "LW", value = card.lastWeek.toString())
                StatItem(label = "PEAK", value = card.peakPosition.toString())
                StatItem(label = "WEEKS", value = card.weeksOnChart.toString())
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                    .clickable { state.eventSink(CardDetailEvent.OnRemoveClick) }
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "REMOVE FROM COLLECTION",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color(0xFF98A2B3), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
