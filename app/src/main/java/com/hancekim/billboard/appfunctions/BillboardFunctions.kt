package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.AddToCollectionUseCase
import com.hancekim.billboard.core.domain.GetBillboard200UseCase
import com.hancekim.billboard.core.domain.GetBillboardArtist100UseCase
import com.hancekim.billboard.core.domain.GetBillboardGlobal200UseCase
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.GroupValidationError
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.hancekim.billboard.core.domain.model.CollectedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Billboard 차트 [AppFunction] 모음.
 *
 * 외부 에이전트(Gemini 등)가 콜드부팅으로 우리 앱을 깨워 호출할 때의 진입점.
 * Hilt 가 4 개 차트 UseCase + 컬렉션 UseCase 들을 주입한다.
 *
 * Schema 분리 근거:
 *  - Song 차트 3 종(Hot 100 / Billboard 200 / Global 200)은 (title, artist, rank) 동일 schema 라
 *    단일 [getSongChartByRank] 로 통합하고 chartType 으로 분기.
 *  - Artist 100 은 아티스트 단위라 곡/앨범 schema 가 의미가 없음 — 별도 [getArtist100ByRank] 로 분리.
 *  - 컬렉션은 사용자 라이브러리 — 차트 조회와 별도의 5 개 함수로 노출.
 */
class BillboardFunctions @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
    private val getBillboard200UseCase: GetBillboard200UseCase,
    private val getBillboardGlobal200UseCase: GetBillboardGlobal200UseCase,
    private val getBillboardArtist100UseCase: GetBillboardArtist100UseCase,
    private val getCollectionFlowUseCase: GetCollectionFlowUseCase,
    private val getGroupsFlowUseCase: GetGroupsFlowUseCase,
    private val addToCollectionUseCase: AddToCollectionUseCase,
    private val removeFromCollectionUseCase: RemoveFromCollectionUseCase,
    private val addGroupUseCase: AddGroupUseCase,
) {

    /** A song or album entry on a Billboard song chart at a specific rank. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class ChartSong(
        /** Song or album title. */
        val title: String,
        /** Artist credits. */
        val artist: String,
        /** Chart rank position. */
        val rank: Int,
        /** Source chart identifier: "hot100", "billboard200", or "global200". */
        val chartType: String,
    )

    /** An artist entry on the Billboard Artist 100 chart at a specific rank. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class ChartArtist(
        /** Artist name. */
        val name: String,
        /** Chart rank position (1 to 100). */
        val rank: Int,
    )

    /** A song collected by the user into a personal group. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CollectedSong(
        /** Unique key derived from title + artist. Use this when removing from collection. */
        val key: String,
        /** Song title. */
        val title: String,
        /** Artist credits. */
        val artist: String,
        /** Identifier of the group this song belongs to. */
        val groupId: Long,
        /** Epoch millis when the song was added to the collection. */
        val collectedAt: Long,
    )

    /** A collection group the user has created. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CollectionGroup(
        /** Unique group identifier. Pass this to addSongToCollection. */
        val id: Long,
        /** Human-readable group name. Up to 20 characters. */
        val name: String,
        /** ARGB color used to render the group. */
        val colorArgb: Int,
    )

    /** Result of adding a song to a collection group. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CollectionAddResult(
        /** The collection key for the added song. */
        val key: String,
        /** Song title. */
        val title: String,
        /** Artist credits. */
        val artist: String,
        /** Source chart identifier. */
        val chartType: String,
        /** Original chart rank at the time of adding. */
        val rank: Int,
        /** Group id the song was added to. */
        val groupId: Long,
        /** Whether the underlying write succeeded. */
        val success: Boolean,
    )

    /**
     * Look up a single entry on a Billboard song chart by its chart position.
     *
     * Supports three song-oriented charts that share the same schema:
     *  - "hot100": Billboard Hot 100 singles chart (valid rank 1..100)
     *  - "billboard200": Billboard 200 albums chart (valid rank 1..200)
     *  - "global200": Billboard Global 200 singles chart (valid rank 1..200)
     *
     * For Artist 100 use [getArtist100ByRank] instead — Artist 100 ranks artists, not songs.
     *
     * @param context The execution context.
     * @param chartType Chart identifier. Must be one of "hot100", "billboard200", "global200".
     *                  Case-insensitive.
     * @param rank Chart rank to look up. 1..100 for "hot100"; 1..200 for "billboard200" and "global200".
     * @return A [ChartSong] populated with the title, artist credits, rank, and normalized chartType.
     * @throws AppFunctionInvalidArgumentException If [chartType] is not recognized or [rank] is out of range.
     *                                             Suggest the caller pick one of the documented chartType
     *                                             values and provide a rank within the allowed range.
     * @throws AppFunctionElementNotFoundException If the chart has no entry at [rank]. Usually transient —
     *                                             retry after the next chart refresh.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getSongChartByRank(
        context: AppFunctionContext,
        chartType: String,
        rank: Int,
    ): ChartSong = withContext(Dispatchers.IO) {
        val normalized = chartType.lowercase()
        val maxRank = when (normalized) {
            "hot100" -> 100
            "billboard200", "global200" -> 200
            else -> throw AppFunctionInvalidArgumentException(
                "Unknown chartType '$chartType'. Use 'hot100', 'billboard200', or 'global200'."
            )
        }
        if (rank !in 1..maxRank) {
            throw AppFunctionInvalidArgumentException(
                "rank must be in 1..$maxRank for $normalized, got $rank"
            )
        }

        val overview = when (normalized) {
            "hot100" -> getBillboardHot100UseCase()
            "billboard200" -> getBillboard200UseCase()
            "global200" -> getBillboardGlobal200UseCase()
            else -> error("unreachable") // 위에서 이미 검증됨.
        }
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw AppFunctionElementNotFoundException("$normalized has no rank-$rank entry")
        ChartSong(
            title = entry.title,
            artist = entry.artist,
            rank = rank,
            chartType = normalized,
        )
    }

    /**
     * Look up a single entry on the Billboard Artist 100 chart by chart position.
     *
     * Artist 100 ranks artists themselves (not songs or albums), so this function returns
     * a [ChartArtist] with just the artist `name` — there is no separate title field.
     *
     * @param context The execution context.
     * @param rank Chart rank to look up. Must be between 1 and 100 inclusive.
     * @return A [ChartArtist] populated with the artist name and rank.
     * @throws AppFunctionInvalidArgumentException If [rank] is out of the 1..100 range.
     *                                             Suggest the caller correct the rank value.
     * @throws AppFunctionElementNotFoundException If the chart has no entry at [rank]. Usually transient —
     *                                             retry after the next chart refresh.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getArtist100ByRank(context: AppFunctionContext, rank: Int): ChartArtist =
        withContext(Dispatchers.IO) {
            if (rank !in 1..100) {
                throw AppFunctionInvalidArgumentException(
                    "rank must be in 1..100, got $rank"
                )
            }
            val overview = getBillboardArtist100UseCase()
            val entry = overview.chartList.firstOrNull { it.rank == rank }
                ?: throw AppFunctionElementNotFoundException("Artist 100 has no rank-$rank entry")
            // 도메인 매퍼 에 따라 artist 또는 title 한쪽에 아티스트 이름이 들어옴 — 비어있지 않은 쪽 선택.
            val name = entry.artist.ifBlank { entry.title }
            ChartArtist(name = name, rank = rank)
        }

    /**
     * Return the user's full song collection across all groups.
     *
     * Reads the first emission of the collection Flow — represents the current persisted state.
     * Empty list when the user has not collected anything yet.
     *
     * @param context The execution context.
     * @return List of [CollectedSong] in the order the underlying store returns them. May be empty.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCollection(context: AppFunctionContext): List<CollectedSong> =
        withContext(Dispatchers.IO) {
            val cards = getCollectionFlowUseCase().first()
            cards.map { card ->
                CollectedSong(
                    key = card.key,
                    title = card.title,
                    artist = card.artist,
                    groupId = card.groupId,
                    collectedAt = card.collectedAt,
                )
            }
        }

    /**
     * Return all collection groups the user has created (including the default group).
     *
     * Use this before calling [addSongToCollection] to discover valid `groupId` values, or
     * to surface group choices to the user.
     *
     * @param context The execution context.
     * @return List of [CollectionGroup]. Always contains at least the default group.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCollectionGroups(context: AppFunctionContext): List<CollectionGroup> =
        withContext(Dispatchers.IO) {
            val groups = getGroupsFlowUseCase().first()
            groups.map { group ->
                CollectionGroup(
                    id = group.id,
                    name = group.name,
                    colorArgb = group.colorArgb,
                )
            }
        }

    /**
     * Add the song currently at [rank] on the given Billboard song chart into a user collection group.
     *
     * Resolves the (title, artist) pair from the live chart, derives a stable collection key
     * via `CollectedCard.createKey(title, artist)`, then persists the card into [groupId].
     *
     * Only song charts are supported. Artist 100 entries are rejected because Artist 100
     * ranks artists, not songs, and has no track-level identity to collect.
     *
     * @param context The execution context.
     * @param chartType Chart identifier. Must be one of "hot100", "billboard200", "global200".
     *                  Case-insensitive.
     * @param rank Chart rank to look up. 1..100 for "hot100"; 1..200 for "billboard200" and "global200".
     * @param groupId Target group id. Must reference an existing group — fetch it via [getCollectionGroups]
     *                first, or create a new one via [createCollectionGroup].
     * @return [CollectionAddResult] echoing the persisted song and whether the write succeeded.
     * @throws AppFunctionInvalidArgumentException If [chartType] is unsupported (including "artist100"),
     *                                             [rank] is out of range, or [groupId] does not exist.
     *                                             Suggest the caller call [getCollectionGroups] first or
     *                                             create a new group via [createCollectionGroup].
     * @throws AppFunctionElementNotFoundException If the chart has no entry at [rank]. Usually transient —
     *                                             retry after the next chart refresh.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun addSongToCollection(
        context: AppFunctionContext,
        chartType: String,
        rank: Int,
        groupId: Long,
    ): CollectionAddResult = withContext(Dispatchers.IO) {
        val normalized = chartType.lowercase()
        // Artist 100 은 곡 단위가 아니므로 컬렉션 대상에서 제외.
        if (normalized == "artist100") {
            throw AppFunctionInvalidArgumentException(
                "Artist 100 entries cannot be added; only song charts are supported."
            )
        }
        val maxRank = when (normalized) {
            "hot100" -> 100
            "billboard200", "global200" -> 200
            else -> throw AppFunctionInvalidArgumentException(
                "Unknown chartType '$chartType'. Use 'hot100', 'billboard200', or 'global200'."
            )
        }
        if (rank !in 1..maxRank) {
            throw AppFunctionInvalidArgumentException(
                "rank must be in 1..$maxRank for $normalized, got $rank"
            )
        }

        val overview = when (normalized) {
            "hot100" -> getBillboardHot100UseCase()
            "billboard200" -> getBillboard200UseCase()
            "global200" -> getBillboardGlobal200UseCase()
            else -> error("unreachable")
        }
        val entry = overview.chartList.firstOrNull { it.rank == rank }
            ?: throw AppFunctionElementNotFoundException("$normalized has no rank-$rank entry")

        // groupId 사전 검증 — 존재하지 않는 그룹은 사용자가 의도한 그룹이 아닐 가능성이 높음.
        val groups = getGroupsFlowUseCase().first()
        if (groups.none { it.id == groupId }) {
            throw AppFunctionInvalidArgumentException(
                "Group id $groupId not found. Call getCollectionGroups first or createCollectionGroup to make a new one."
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
            groupId = groupId,
        )
        val success = addToCollectionUseCase(card)
        CollectionAddResult(
            key = key,
            title = entry.title,
            artist = entry.artist,
            chartType = normalized,
            rank = rank,
            groupId = groupId,
            success = success,
        )
    }

    /**
     * Remove a single collected song from the user's collection by its stable key.
     *
     * Destructive operation — caller should confirm with the user before invoking.
     * The [key] is the same value returned by [getCollection] or [addSongToCollection].
     *
     * @param context The execution context.
     * @param key Collection key in the form `title::artist`. Must be non-empty.
     * @return `true` if the remove call completed. Removing a non-existent key is a no-op and
     *         still returns `true`.
     * @throws AppFunctionInvalidArgumentException If [key] is blank. Suggest the caller fetch
     *                                             the actual key via [getCollection] first.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun removeSongFromCollection(
        context: AppFunctionContext,
        key: String,
    ): Boolean = withContext(Dispatchers.IO) {
        if (key.isBlank()) {
            throw AppFunctionInvalidArgumentException(
                "key must not be blank. Call getCollection to discover valid keys."
            )
        }
        removeFromCollectionUseCase(key)
        true
    }

    /**
     * Create a new collection group with the given name and color.
     *
     * Name is trimmed before validation. Duplicate detection is case-insensitive on the
     * trimmed name. On success, returns a [CollectionGroup] echoing the persisted record so
     * the caller can immediately pass `id` to [addSongToCollection].
     *
     * @param context The execution context.
     * @param name Group name. Must be non-empty after trimming and at most 20 characters.
     * @param colorArgb ARGB color used to render the group in the UI.
     * @return [CollectionGroup] containing the new group's id, trimmed name, and colorArgb.
     * @throws AppFunctionInvalidArgumentException If the name is empty, exceeds 20 characters,
     *                                             or duplicates an existing group. Recovery:
     *                                             pick a different name, or use [getCollectionGroups]
     *                                             to find the existing group's id.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createCollectionGroup(
        context: AppFunctionContext,
        name: String,
        colorArgb: Int,
    ): CollectionGroup = withContext(Dispatchers.IO) {
        val newId = addGroupUseCase(name, colorArgb).getOrElse { err ->
            when (err) {
                is GroupValidationError.Empty -> throw AppFunctionInvalidArgumentException(
                    "Group name cannot be empty."
                )
                is GroupValidationError.TooLong -> throw AppFunctionInvalidArgumentException(
                    "Group name exceeds 20 characters."
                )
                is GroupValidationError.DuplicateName -> throw AppFunctionInvalidArgumentException(
                    "A group named '$name' already exists. Use getCollectionGroups to find its id."
                )
                else -> throw AppFunctionInvalidArgumentException(
                    "Failed to create group: ${err.message ?: "unknown error"}"
                )
            }
        }
        CollectionGroup(
            id = newId,
            name = name.trim(),
            colorArgb = colorArgb,
        )
    }
}
