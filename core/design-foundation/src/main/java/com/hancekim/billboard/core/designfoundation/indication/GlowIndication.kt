package com.hancekim.billboard.core.designfoundation.indication

import androidx.compose.animation.Animatable
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OffscreenIndication(
    private val color: Color = Color.White,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): OffscreenIndicationNode {
        return OffscreenIndicationNode(interactionSource, color)
    }

    override fun equals(other: Any?) = other is OffscreenIndication && color == other.color
    override fun hashCode(): Int {
        return color.hashCode()
    }
}

class OffscreenIndicationNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
) : Modifier.Node(), DrawModifierNode {

    val animatedColor = Animatable(Color.Transparent)

    private suspend fun animateColor() {
        animatedColor.animateTo(color)
    }

    private suspend fun releaseColor() {
        animatedColor.animateTo(Color.Transparent)
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> animateColor()
                    is PressInteraction.Release, is PressInteraction.Cancel -> releaseColor()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawIntoCanvas { canvas ->
            canvas.saveLayer(Rect(Offset.Zero, size), Paint())
            drawContent()
            drawRect(
                color = animatedColor.value,
                blendMode = BlendMode.SrcAtop
            )
            canvas.restore()
        }
    }
}