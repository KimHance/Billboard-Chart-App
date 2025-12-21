package com.hancekim.billboard.core.designfoundation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun throttledProcess(
    throttledTimeMillis: Long = DefaultThrottlingTime,
    id: String = DefaultProcessorId,
    onProcessed: () -> Unit,
): () -> Unit {
    val multipleEventsCutter = remember { EventProcessor.getThrottlingProcessor(id) }
    val newOnClick: () -> Unit = {
        multipleEventsCutter.processEvent(throttledTimeMillis) { onProcessed() }
    }
    return newOnClick
}

@Composable
fun <T> throttledProcess(
    throttledTimeMillis: Long = DefaultThrottlingTime,
    id: String = DefaultProcessorId,
    onProcessed: (T) -> Unit,
): (T) -> Unit {
    val multipleEventsCutter = remember { EventProcessor.getThrottlingProcessor(id) }
    val newOnClick: (T) -> Unit = {
        multipleEventsCutter.processEvent(throttledTimeMillis) { onProcessed(it) }
    }
    return newOnClick
}

internal interface EventProcessor {
    fun processEvent(
        timeMillis: Long,
        event: () -> Unit,
    )

    companion object {
        val eventProcessorMap = mutableMapOf<String, EventProcessor>()
    }
}

private class ThrottlingProcessorImpl : EventProcessor {
    private val now: Long
        get() = System.currentTimeMillis()
    private var lastEventTimeMs: Long = 0

    override fun processEvent(
        timeMillis: Long,
        event: () -> Unit,
    ) {
        if (now - lastEventTimeMs >= timeMillis) {
            event.invoke()
            lastEventTimeMs = now
        }
    }
}

internal fun EventProcessor.Companion.getThrottlingProcessor(id: String): EventProcessor =
    eventProcessorMap.getOrPut(id) {
        ThrottlingProcessorImpl()
    }

private const val DefaultProcessorId = "980225"
private const val DefaultThrottlingTime = 500L