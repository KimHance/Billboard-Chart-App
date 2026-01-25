package com.hancekim.billboard.core.player.pip

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PipHorizontalAnchor {
    Start, End
}

@Stable
class PipState(
    private val initialAnchor: PipHorizontalAnchor = PipHorizontalAnchor.End,
    private val velocityThreshold: Float = 500f,
    private val flingDurationFactor: Float = 0.2f
) {
    var containerBounds by mutableStateOf(Size.Zero)
    var boxSize by mutableStateOf(Size.Zero)
        internal set

    var currentAnchor by mutableStateOf(initialAnchor)
        private set

    var isDragging by mutableStateOf(false)
        private set

    private var dragOffsetX by mutableFloatStateOf(0f)
    private var dragOffsetY by mutableFloatStateOf(0f)

    internal val animatedX = Animatable(0f)
    internal val animatedY = Animatable(0f)

    private var isInitialized = false

    private var scope: CoroutineScope? = null

    val displayOffsetX: Float
        get() = if (isDragging) dragOffsetX else animatedX.value

    val displayOffsetY: Float
        get() = if (isDragging) dragOffsetY else animatedY.value

    private fun getAnchorX(anchor: PipHorizontalAnchor): Float {
        return when (anchor) {
            PipHorizontalAnchor.Start -> 0f
            PipHorizontalAnchor.End -> (containerBounds.width - boxSize.width).coerceAtLeast(0f)
        }
    }

    private fun findClosestAnchor(x: Float, velocityX: Float): PipHorizontalAnchor {
        return when {
            velocityX > velocityThreshold -> PipHorizontalAnchor.End
            velocityX < -velocityThreshold -> PipHorizontalAnchor.Start
            else -> {
                val midX = (containerBounds.width - boxSize.width) / 2f
                if (x < midX) PipHorizontalAnchor.Start else PipHorizontalAnchor.End
            }
        }
    }

    private fun clampY(y: Float): Float {
        val maxY = (containerBounds.height - boxSize.height).coerceAtLeast(0f)
        return y.coerceIn(0f, maxY)
    }

    internal fun initializeIfNeeded() {
        if (!isInitialized && boxSize != Size.Zero && containerBounds != Size.Zero) {
            val initX = getAnchorX(initialAnchor)
            val initY = clampY(containerBounds.height - boxSize.height)
            scope?.launch {
                animatedX.snapTo(initX)
                animatedY.snapTo(initY)
            }
            dragOffsetX = initX
            dragOffsetY = initY
            isInitialized = true
        }
    }

    fun setScope(scope: CoroutineScope) {
        this.scope = scope
    }

    fun onDragStart() {
        isDragging = true
        dragOffsetX = animatedX.value
        dragOffsetY = animatedY.value
    }

    fun onDrag(deltaX: Float, deltaY: Float) {
        val maxX = (containerBounds.width - boxSize.width).coerceAtLeast(0f)
        val maxY = (containerBounds.height - boxSize.height).coerceAtLeast(0f)
        dragOffsetX = (dragOffsetX + deltaX).coerceIn(0f, maxX)
        dragOffsetY = (dragOffsetY + deltaY).coerceIn(0f, maxY)
    }

    fun onDragEnd(velocity: Velocity) {
        val closest = findClosestAnchor(dragOffsetX, velocity.x)
        val targetX = getAnchorX(closest)
        val predictedY = dragOffsetY + velocity.y * flingDurationFactor
        val targetY = clampY(predictedY)

        scope?.launch {
            animatedX.snapTo(dragOffsetX)
            animatedY.snapTo(dragOffsetY)
            isDragging = false
            currentAnchor = closest
            coroutineScope {
                launch { animatedX.animateTo(targetX, spring()) }
                launch { animatedY.animateTo(targetY, spring()) }
            }
        }
    }

    fun onDragCancel() {
        scope?.launch {
            animatedX.snapTo(dragOffsetX)
            animatedY.snapTo(dragOffsetY)
            isDragging = false
            val targetX = getAnchorX(currentAnchor)
            animatedX.animateTo(targetX, spring())
        }
    }

    fun moveToAnchor(anchor: PipHorizontalAnchor) {
        if (!isDragging) {
            currentAnchor = anchor
            scope?.launch {
                animatedX.animateTo(getAnchorX(anchor), spring())
            }
        }
    }
}

fun Modifier.pipDraggable(state: PipState): Modifier = this
    .onPlaced { coordinates ->
        coordinates.parentLayoutCoordinates?.let { parent ->
            val containerBounds = Size(
                parent.size.width.toFloat(),
                parent.size.height.toFloat()
            )
            if (state.containerBounds != containerBounds) {
                state.containerBounds = containerBounds
            }
        }

        val boxSize = Size(
            coordinates.size.width.toFloat(),
            coordinates.size.height.toFloat()
        )
        if (state.boxSize != boxSize) {
            state.boxSize = boxSize
        }

        state.initializeIfNeeded()
    }
    .offset {
        IntOffset(
            state.displayOffsetX.roundToInt(),
            state.displayOffsetY.roundToInt()
        )
    }
    .pointerInput(state) {
        val velocityTracker = VelocityTracker()

        detectDragGestures(
            onDragStart = {
                velocityTracker.resetTracking()
                state.onDragStart()
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity()
                state.onDragEnd(velocity)
            },
            onDragCancel = { state.onDragCancel() },
            onDrag = { change, dragAmount ->
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                change.consume()
                state.onDrag(dragAmount.x, dragAmount.y)
            }
        )
    }