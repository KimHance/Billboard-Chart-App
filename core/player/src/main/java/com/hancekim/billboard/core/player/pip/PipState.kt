package com.hancekim.billboard.core.player.pip

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class PipAnchor {
    TopLeft, TopRight, BottomLeft, BottomRight
}

@Stable
class PipState(
    private val initialAnchor: PipAnchor = PipAnchor.BottomRight,
    private val animationDurationMs: Int = 300
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

    private fun getAnchorPosition(anchor: PipAnchor): Pair<Float, Float> {
        val maxX = (containerBounds.width - boxSize.width).coerceAtLeast(0f)
        val maxY = (containerBounds.height - boxSize.height).coerceAtLeast(0f)
        return when (anchor) {
            PipAnchor.TopLeft -> 0f to 0f
            PipAnchor.TopRight -> maxX to 0f
            PipAnchor.BottomLeft -> 0f to maxY
            PipAnchor.BottomRight -> maxX to maxY
        }
    }

    private fun findClosestAnchor(x: Float, y: Float): PipAnchor {
        return PipAnchor.entries.minBy { anchor ->
            val (ax, ay) = getAnchorPosition(anchor)
            hypot(x - ax, y - ay)
        }
    }

    internal fun initializeIfNeeded() {
        if (!isInitialized && boxSize != Size.Zero && containerBounds != Size.Zero) {
            val (initX, initY) = getAnchorPosition(initialAnchor)
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

    fun onDragEnd() {
        val closest = findClosestAnchor(dragOffsetX, dragOffsetY)
        scope?.launch {
            animatedX.snapTo(dragOffsetX)
            animatedY.snapTo(dragOffsetY)
            isDragging = false
            snapToAnchor(closest)
        }
    }

    fun onDragCancel() {
        scope?.launch {
            animatedX.snapTo(dragOffsetX)
            animatedY.snapTo(dragOffsetY)
            isDragging = false
            snapToAnchor(currentAnchor)
        }
    }

    private suspend fun snapToAnchor(anchor: PipAnchor) {
        val (targetX, targetY) = getAnchorPosition(anchor)
        currentAnchor = anchor
        coroutineScope {
            launch { animatedX.animateTo(targetX, tween(animationDurationMs)) }
            launch { animatedY.animateTo(targetY, tween(animationDurationMs)) }
        }
    }

    fun moveToAnchor(anchor: PipAnchor) {
        if (!isDragging) {
            scope?.launch { snapToAnchor(anchor) }
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
        detectDragGestures(
            onDragStart = { state.onDragStart() },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragCancel() },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount.x, dragAmount.y)
            }
        )
    }
