package com.hancekim.billboard.feature.agentchat.tools

import com.google.adk.kt.annotations.Param
import com.google.adk.kt.annotations.Tool
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.AddToCollectionUseCase
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.GroupValidationError
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.hancekim.billboard.core.domain.model.CollectedCard
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 컬렉션 관리 Tool 컬렉션.
 *
 * BillboardFunctions 의 collection 관련 5 개 @AppFunction 과 동일한 도메인 UseCase 를 사용해
 * 외부 에이전트(Gemini) 호출 경로와 인앱 ADK 에이전트 경로가 동일한 데이터 상태를 보장한다.
 */
class CollectionTools @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
    private val getBillboardGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getCollectionFlowUseCase: GetCollectionFlowUseCase,
    private val getGroupsFlowUseCase: GetGroupsFlowUseCase,
    private val addToCollectionUseCase: AddToCollectionUseCase,
    private val removeFromCollectionUseCase: RemoveFromCollectionUseCase,
    private val addGroupUseCase: AddGroupUseCase,
) {

    /** Returns the user's full song collection across all groups. Empty list when nothing collected. */
    @Tool
    suspend fun getCollection(): List<Map<String, Any?>> {
        val cards = getCollectionFlowUseCase().first()
        return cards.map { card ->
            mapOf(
                "key" to card.key,
                "title" to card.title,
                "artist" to card.artist,
                "groupId" to card.groupId,
                "collectedAt" to card.collectedAt,
            )
        }
    }

    /** Returns all collection groups the user has created (including the default group). */
    @Tool
    suspend fun getCollectionGroups(): List<Map<String, Any?>> {
        val groups = getGroupsFlowUseCase().first()
        return groups.map { group ->
            mapOf(
                "id" to group.id,
                "name" to group.name,
                "colorArgb" to group.colorArgb,
            )
        }
    }

    /**
     * Returns groups whose name contains the given query substring (case-insensitive).
     * Use this when the user names a group but you are not sure it exists exactly — prompt for confirmation.
     */
    @Tool
    suspend fun findSimilarGroups(
        @Param("Substring to match against group names (case-insensitive)") query: String,
    ): List<Map<String, Any?>> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return emptyList()
        val groups = getGroupsFlowUseCase().first()
        return groups
            .filter { it.name.lowercase().contains(needle) }
            .map { group ->
                mapOf(
                    "id" to group.id,
                    "name" to group.name,
                    "colorArgb" to group.colorArgb,
                )
            }
    }

    /**
     * Adds the song currently at [rank] on the given song chart into a user collection group.
     * Only song charts (hot100, billboard200, global200) are supported — Artist 100 is rejected.
     */
    @Tool
    suspend fun addSongToCollection(
        @Param("Chart identifier: hot100 | billboard200 | global200") chartType: String,
        @Param("Chart rank position (1..100 for hot100, 1..200 for others)") rank: Int,
        // ADK KSP 가 Long 파라미터 미지원 → Int 로 받고 내부에서 Long 변환.
        @Param("Target group id (call getCollectionGroups first to obtain)") groupId: Int,
    ): Map<String, Any?> {
        val groupIdLong: Long = groupId.toLong()
        val normalized = chartType.lowercase()
        if (normalized == "artist100") {
            throw IllegalArgumentException(
                "Artist 100 entries cannot be added; only song charts are supported."
            )
        }
        val maxRank = when (normalized) {
            "hot100" -> 100
            "billboard200", "global200" -> 200
            else -> throw IllegalArgumentException(
                "Unknown chartType '$chartType'. Use 'hot100', 'billboard200', or 'global200'."
            )
        }
        require(rank in 1..maxRank) { "rank must be in 1..$maxRank for $normalized, got $rank" }

        val overview = when (normalized) {
            "hot100" -> getBillboardHot100UseCase()
            "billboard200" -> getBillboard200UseCase()
            "global200" -> getBillboardGlobal200UseCase()
            else -> error("unreachable")
        }
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw IllegalStateException("$normalized has no rank-$rank entry")

        // groupId 사전 검증 — 존재하지 않는 그룹은 사용자가 의도한 그룹이 아닐 가능성이 높음.
        val groups = getGroupsFlowUseCase().first()
        if (groups.none { it.id == groupIdLong }) {
            throw IllegalArgumentException(
                "Group id $groupId not found. Call getCollectionGroups first or createCollectionGroup."
            )
        }

        val key = CollectedCard.createKey(entry.title, entry.artist)
        val card = CollectedCard(
            key = key,
            title = entry.title,
            artist = entry.artist,
            albumArtUrl = entry.image,
            collectedAt = System.currentTimeMillis(),
            lastWeek = entry.lastWeek,
            peakPosition = entry.peakPosition,
            weeksOnChart = entry.weekOnChart,
            groupId = groupIdLong,
        )
        val success = addToCollectionUseCase(card)
        return mapOf(
            "key" to key,
            "title" to entry.title,
            "artist" to entry.artist,
            "chartType" to normalized,
            "rank" to rank,
            "groupId" to groupId,
            "success" to success,
        )
    }

    /**
     * Removes a single collected song from the user's collection by its stable key.
     * Destructive — confirm with user before invoking.
     */
    @Tool
    suspend fun removeSongFromCollection(
        @Param("Unique collection key returned by getCollection (format: title::artist)") key: String,
    ): Map<String, Any?> {
        require(key.isNotBlank()) { "key must not be blank. Call getCollection to discover valid keys." }
        removeFromCollectionUseCase(key)
        return mapOf(
            "key" to key,
            "success" to true,
        )
    }

    /**
     * Creates a new collection group with the given name (1..20 chars, unique) and ARGB color.
     * Returns the new group's id so you can immediately add songs into it.
     */
    @Tool
    suspend fun createCollectionGroup(
        @Param("Group display name (1..20 chars)") name: String,
        @Param("ARGB color int (use 0xFFCCCCCC if user did not specify)") colorArgb: Int,
    ): Map<String, Any?> {
        val newId = addGroupUseCase(name, colorArgb).getOrElse { err ->
            val message = when (err) {
                is GroupValidationError.Empty -> "Group name cannot be empty."
                is GroupValidationError.TooLong -> "Group name exceeds 20 characters."
                is GroupValidationError.DuplicateName ->
                    "A group named '$name' already exists. Use getCollectionGroups to find its id."
                else -> "Failed to create group: ${err.message ?: "unknown error"}"
            }
            throw IllegalArgumentException(message)
        }
        return mapOf(
            "id" to newId,
            "name" to name.trim(),
            "colorArgb" to colorArgb,
        )
    }
}
