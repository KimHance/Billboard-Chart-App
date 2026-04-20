package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.designsystem.componenet.card.HoloCard
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class SlotSpec(val angle: Float, val radius: Dp, val size: Dp)

private val CENTER_SLOT = SlotSpec(0f, 0.dp, 150.dp)
private val INNER_SLOTS = listOf(
    SlotSpec(-90f, 130.dp, 82.dp),
    SlotSpec(-150f, 130.dp, 82.dp),
    SlotSpec(-30f, 130.dp, 82.dp),
)
private val OUTER_SLOTS = listOf(
    SlotSpec(-170f, 180.dp, 70.dp),
    SlotSpec(-115f, 180.dp, 70.dp),
    SlotSpec(-60f, 180.dp, 70.dp),
    SlotSpec(30f, 180.dp, 70.dp),
    SlotSpec(150f, 180.dp, 70.dp),
)
private val ALL_SLOTS = listOf(CENTER_SLOT) + INNER_SLOTS + OUTER_SLOTS

@Composable
fun OrbitLayout(
    cards: ImmutableList<CollectedCard>,
    modifier: Modifier = Modifier,
    onCardClick: (String) -> Unit,
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ALL_SLOTS.forEachIndexed { index, slot ->
            val offsetX = with(density) {
                if (slot.radius > 0.dp) {
                    (cos(Math.toRadians(slot.angle.toDouble())).toFloat() * slot.radius.toPx()).roundToInt()
                } else 0
            }
            val offsetY = with(density) {
                if (slot.radius > 0.dp) {
                    (sin(Math.toRadians(slot.angle.toDouble())).toFloat() * slot.radius.toPx() * 0.75f).roundToInt()
                } else 0
            }

            Box(modifier = Modifier.offset { IntOffset(offsetX, offsetY) }) {
                if (index < cards.size) {
                    val card = cards[index]
                    HoloCard(
                        albumArtUrl = card.albumArtUrl,
                        cardSize = slot.size,
                        interactive = false,
                        modifier = Modifier.clickable { onCardClick(card.key) },
                    )
                } else {
                    EmptySlot(size = slot.size, index = index)
                }
            }
        }
    }
}
