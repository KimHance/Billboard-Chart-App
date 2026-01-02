package com.hancekim.billboard.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import timber.log.Timber

private object StateDiffLogger {

    private const val MAX_VALUE_LENGTH = 200
    private const val MAX_DEPTH = 10
    private const val MAX_PREVIEW_ITEMS = 5

    fun <T : Any> logDiff(
        old: T?,
        new: T,
        tag: String
    ) {
        val state = new::class.simpleName ?: "State"

        if (old == null) {
            Timber.tag(tag).d("[$state] 🆕 Initial state")
            return
        }

        if (old === new) return
        if (old == new) return

        val changes = findChanges(old, new, "", 0)
        if (changes.isNotEmpty()) {
            Timber.tag(tag).d(
                buildString {
                    appendLine("[$state] 🔄 ${changes.size} change(s):")
                    changes.forEach { appendLine("  $it") }
                }
            )
        }
    }

    private fun findChanges(old: Any?, new: Any?, path: String, depth: Int): List<String> {
        // 빠른 체크
        if (old === new) return emptyList()
        if (depth > MAX_DEPTH) return listOf("${path.displayPath()}: ⚠️ max depth")

        // null 체크
        if (old == null) return listOf("${path.displayPath()}: null → ${new.toShortString()}")
        if (new == null) return listOf("${path.displayPath()}: ${old.toShortString()} → null")

        // 타입 체크
        if (old::class != new::class) {
            return listOf("${path.displayPath()}: 🔀 ${old::class.simpleName} → ${new::class.simpleName}")
        }

        // equals 체크 (primitive, String, Enum 등은 여기서 처리됨)
        if (old == new) return emptyList()

        // 타입별 비교
        return when (old) {
            is Map<*, *> -> compareMap(old, new as Map<*, *>, path, depth)

            is Collection<*> -> compareCollection(old, new as Collection<*>, path)

            is Array<*> -> compareCollection(old.toList(), (new as Array<*>).toList(), path)

            else -> {
                if (old.isDataClass()) {
                    compareDataClass(old, new, path, depth)
                } else {
                    listOf("${path.displayPath()}: ${old.toShortString()} → ${new.toShortString()}")
                }
            }
        }
    }

    private fun compareDataClass(old: Any, new: Any, path: String, depth: Int): List<String> {
        val changes = mutableListOf<String>()

        old::class.java.declaredFields.asSequence()
            .filter { field ->
                !field.name.startsWith("$") &&
                        field.name != "CREATOR" &&
                        field.name != "Companion" &&
                        !java.lang.reflect.Modifier.isStatic(field.modifiers)
            }
            .forEach { field ->
                try {
                    field.isAccessible = true
                    val oldValue = field.get(old)
                    val newValue = field.get(new)

                    if (oldValue === newValue) return@forEach
                    if (oldValue == newValue) return@forEach

                    changes.addAll(
                        findChanges(
                            oldValue,
                            newValue,
                            path.appendPath(field.name),
                            depth + 1
                        )
                    )
                } catch (_: Exception) {
                }
            }

        return changes
    }

    private fun compareCollection(
        old: Collection<*>,
        new: Collection<*>,
        path: String
    ): List<String> {
        val displayPath = path.displayPath()

        // 빠른 체크
        if (old === new) return emptyList()
        if (old == new) return emptyList()

        val changes = mutableListOf<String>()

        val oldSize = old.size
        val newSize = new.size

        if (oldSize != newSize) {
            changes.add("$displayPath.size: $oldSize → $newSize")
        }

        // Set으로 변환해서 추가/삭제 비교
        val oldSet = old.toSet()
        val newSet = new.toSet()

        val added = newSet - oldSet
        val removed = oldSet - newSet

        if (added.isNotEmpty()) {
            changes.add("$displayPath: ➕ ${added.size} added ${added.preview()}")
        }

        if (removed.isNotEmpty()) {
            changes.add("$displayPath: ➖ ${removed.size} removed ${removed.preview()}")
        }

        // 추가/삭제가 있으면 내부 비교는 무의미
        if (added.isNotEmpty() || removed.isNotEmpty()) {
            return changes
        }

        // 추가/삭제 없이 순서나 내부값만 변경된 경우
        val oldList = old.toList()
        val newList = new.toList()

        if (oldList != newList) {
            // 순서 변경 체크
            val orderChanged = oldSet == newSet && oldList != newList
            if (orderChanged) {
                changes.add("$displayPath: ↕️ order changed")
            }
        }

        return changes
    }

    private fun compareMap(old: Map<*, *>, new: Map<*, *>, path: String, depth: Int): List<String> {
        val displayPath = path.displayPath()

        if (old === new) return emptyList()
        if (old == new) return emptyList()

        val changes = mutableListOf<String>()

        val oldKeys = old.keys
        val newKeys = new.keys

        val added = newKeys - oldKeys
        val removed = oldKeys - newKeys

        if (added.isNotEmpty()) {
            changes.add("$displayPath: ➕ ${added.size} keys added ${added.take(MAX_PREVIEW_ITEMS)}")
        }

        if (removed.isNotEmpty()) {
            changes.add(
                "$displayPath: ➖ ${removed.size} keys removed ${
                    removed.take(
                        MAX_PREVIEW_ITEMS
                    )
                }"
            )
        }

        // 추가/삭제가 있으면 내부 비교 스킵
        if (added.isNotEmpty() || removed.isNotEmpty()) {
            return changes
        }

        // 공통 키의 값 변경만 체크
        oldKeys.forEach { key ->
            val oldValue = old[key]
            val newValue = new[key]

            if (oldValue === newValue) return@forEach
            if (oldValue == newValue) return@forEach

            changes.addAll(findChanges(oldValue, newValue, "$path[$key]", depth + 1))
        }

        return changes
    }

    // === 확장 함수 ===

    private fun String.displayPath(): String = ifEmpty { "root" }

    private fun String.appendPath(field: String): String = if (isEmpty()) field else "$this.$field"

    private fun Any?.toShortString(): String = when {
        this == null -> "null"
        this is String -> if (length > MAX_VALUE_LENGTH) "\"${take(MAX_VALUE_LENGTH)}...\"" else "\"$this\""
        this is Collection<*> -> "[$size items]"
        this is Map<*, *> -> "{$size entries}"
        this is Array<*> -> "[$size items]"
        else -> toString().let { if (it.length > MAX_VALUE_LENGTH) "${it.take(MAX_VALUE_LENGTH)}..." else it }
    }

    private fun Set<*>.preview(): String {
        val total = size
        val items = take(MAX_PREVIEW_ITEMS).map { item ->
            when {
                item == null -> "null"
                item is String -> if (item.length > 20) "\"${item.take(20)}...\"" else "\"$item\""
                item.isDataClass() -> item::class.simpleName ?: "?"
                else -> item::class.simpleName ?: "?"
            }
        }

        return if (total > MAX_PREVIEW_ITEMS) {
            "$items, ... +${total - MAX_PREVIEW_ITEMS} more"
        } else {
            items.toString()
        }
    }

    private fun Any.isDataClass(): Boolean = try {
        this::class.java.declaredMethods.any { it.name == "copy" }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun <T : Any> StateDiffLogEffect(
    state: T,
    enabled: Boolean,
    tag: String
) {
    if (!enabled) return

    val previousState = remember { mutableStateOf<T?>(null) }

    LaunchedEffect(state) {
        StateDiffLogger.logDiff(
            old = previousState.value,
            new = state,
            tag = tag
        )
        previousState.value = state
    }
}
