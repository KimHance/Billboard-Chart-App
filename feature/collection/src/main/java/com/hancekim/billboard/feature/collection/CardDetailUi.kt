package com.hancekim.billboard.feature.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.designfoundation.icon.BillboardIcons
import com.hancekim.billboard.core.designfoundation.icon.IcoClose
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designfoundation.util.throttledProcess
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityRetainedComponent

@CircuitInject(BillboardScreen.CardDetail::class, ActivityRetainedComponent::class)
@Composable
fun CardDetailUi(state: CardDetailState, modifier: Modifier = Modifier) {
    BackHandler { state.eventSink(CardDetailEvent.OnCloseClick) }

    val card = state.card ?: return
    val colorScheme = BillboardTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
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
            // 닫기 버튼
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 12.dp)
                    .size(36.dp)
                    .background(colorScheme.textPrimary.copy(alpha = 0.12f), CircleShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "닫기"
                    }
                    .noRippleClickable { state.eventSink(CardDetailEvent.OnCloseClick) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BillboardIcons.IcoClose,
                    contentDescription = null,
                    tint = colorScheme.textPrimary,
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
                                    colors = listOf(
                                        colorScheme.holoGlow.copy(alpha = 0.3f),
                                        Color.Transparent,
                                    ),
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
                        .border(
                            1.dp,
                            colorScheme.textPrimary.copy(alpha = 0.25f),
                            RoundedCornerShape(22.dp),
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "REMOVE FROM COLLECTION"
                        }
                        .noRippleClickable(
                            onClick = throttledProcess(id = "card_detail_remove") {
                                state.eventSink(CardDetailEvent.OnRemoveClick)
                            },
                        )
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "REMOVE FROM COLLECTION",
                        style = BillboardTheme.typography.buttonMd(),
                        color = colorScheme.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colorScheme = BillboardTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = BillboardTheme.typography.headingLg(),
            color = colorScheme.textPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = BillboardTheme.typography.dateSm(),
            color = colorScheme.textTertiary,
        )
    }
}
