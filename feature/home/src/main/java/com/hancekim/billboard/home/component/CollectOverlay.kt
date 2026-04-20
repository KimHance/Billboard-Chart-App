package com.hancekim.billboard.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import com.hancekim.billboard.core.designsystem.componenet.card.SparkleEffect
import com.hancekim.billboard.core.domain.model.Chart

@Composable
fun CollectOverlay(
    visible: Boolean,
    chart: Chart?,
    isAlreadyCollected: Boolean,
    modifier: Modifier = Modifier,
    onCollect: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && chart != null,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(200)),
    ) {
        chart ?: return@AnimatedVisibility
        var showSparkle by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 110.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(360.dp)
                            .blur(12.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFC8DCF0).copy(alpha = 0.3f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                    HoloCard(
                        albumArtUrl = chart.image,
                        cardSize = 240.dp,
                        interactive = true,
                    )
                    SparkleEffect(trigger = showSparkle)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = chart.title,
                    style = BillboardTheme.typography.titleMd(),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = chart.artist,
                    style = BillboardTheme.typography.bodySm(),
                    color = Color(0xFFD0D5DD),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (isAlreadyCollected) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable { onRemove() }
                            .padding(horizontal = 28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "REMOVE FROM COLLECTION",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .background(
                                BillboardTheme.colorScheme.accent,
                                RoundedCornerShape(24.dp),
                            )
                            .clickable {
                                showSparkle = true
                                onCollect()
                            }
                            .padding(horizontal = 28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ADD TO COLLECTION",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "drag card to rotate · tap backdrop to cancel",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF98A2B3),
                )
            }
        }
    }
}
