# Card Collection + Groups — Design Spec

**Date:** 2026-05-16
**Status:** Approved (pending implementation plan)
**Scope:** Add group-based classification to the existing card collection feature, replace the orbit-layout collection screen with a now-playing-player + horizontal mini-rail + right sidebar layout based on the provided design handoff.

---

## 1. Goals and Constraints

### Functional
- Each collected card belongs to exactly one group. Re-collecting an already-collected track moves it to the newly selected group (no duplication).
- Card stats (`lastWeek`, `peakPosition`, `weeksOnChart`) are frozen at collection time — already true today, preserved.
- Card front never displays current rank or chart name.
- The `Default` group cannot be deleted or renamed.
- Group names are unique (case-insensitive, trimmed).
- Removing a non-empty group cascades deletion of all cards in it (after a confirm step in UI).

### Non-functional / hard rules
- Follow the existing Slack Circuit pattern for every screen (Screen / State / Event / Presenter / Ui).
- Use `BillboardTheme.colorScheme.*` and `BillboardTheme.typography.*` — no `MaterialTheme` direct access.
- Use `BillboardColor` primitives for theme-independent brand colors (group preset colors live here).
- All new Composables get a `@ThemePreviews` preview.
- All clickable non-`Button` elements get `semantics { role = Role.Button; contentDescription = ... }`.
- New code in Korean comments, English docs.

### Decisions locked in during brainstorming
- `MAX_SLOTS = 9` constant is **removed** — collection is unlimited.
- The existing `OrbitLayout` collection UI is **deleted** and replaced.
- On first launch, only the **Default** group is seeded automatically. Other presets (Workout/Chill/Throwback) become palette suggestions in `NewGroupForm`, not auto-seeded groups.
- Room database version bump (1 → 2) with `fallbackToDestructiveMigration(dropAllTables = true)`. Existing collection data is wiped intentionally.
- The "now-playing deck" in the redesigned collection screen reuses `:core:player` `YoutubePlayer` + `PlayerControllerButtons`, identical to the Home pattern. Changing the active mini-rail card re-loads the video URL via `PlayerState.loadVideo(videoId, 0f)`.
- When the currently selected group has zero cards, render a single-line text placeholder instead of any animated `LazyRow` / pulsing slots.
- "+ NEW GROUP" inside the Home `CollectOverlay` dropdown opens an inline `NewGroupForm` over the overlay (does not navigate away).

---

## 2. Architecture & Module Layout

No new modules. All changes land in existing modules per `.claude/rules/01-architecture.md`.

### New files

```
:core:data
  model/Group.kt                            -- data class + DEFAULT_ID = 1L
  repository/GroupRepository.kt             -- interface
  model/CollectedCard.kt                    -- ADD groupId: Long, REMOVE MAX_SLOTS
  repository/CollectionRepository.kt        -- (signature unchanged; semantics: upsert REPLACE on same key)

:core:domain
  model/Group.kt                            -- domain mirror
  GetGroupsFlowUseCase.kt
  AddGroupUseCase.kt                        -- validates name + uniqueness, returns Result<Long>
  RemoveGroupUseCase.kt                     -- guards Group.DEFAULT_ID
  GroupValidationError.kt                   -- sealed Throwable
  mapper/GroupMapper.kt
  model/CollectedCard.kt                    -- ADD groupId

:core:data-impl
  repository/GroupRepositoryImpl.kt
  di/RepositoryModule.kt                    -- ADD @Binds for GroupRepository
  repository/CollectionRepositoryImpl.kt    -- pass-through groupId

:core:data-source
  GroupDataSource.kt                        -- interface (shared)
  prod/db/GroupEntity.kt
  prod/db/GroupDao.kt
  prod/db/CollectedCardEntity.kt            -- ADD groupId column + FK(RESTRICT) + Index
  prod/db/CollectionDatabase.kt             -- version = 2, entities += GroupEntity
  prod/db/GroupDataSourceImpl.kt
  prod/di/DatabaseModule.kt                 -- fallbackToDestructiveMigration + onCreate Default seed
  prod/di/DataSourceModule.kt               -- @Binds GroupDataSource → impl
  demo/GroupDataSourceImpl.kt               -- in-memory MutableStateFlow + Default seed
  demo/di/DataSourceModule.kt               -- @Binds

:core:design-foundation
  color/BillboardColor.kt                   -- ADD HoloGreen / HoloAmber / HoloBlue / HoloMagenta primitives

:core:design-system/componenet/group/
  GroupDot.kt                               -- 12.dp dot with outer glow (reusable, size override)
  GroupChip.kt                              -- pill chip, group color tinted
  GroupDropdown.kt                          -- closed = chip + chevron; open = DropdownMenu

:core:design-system/componenet/header/
  BillboardHeader.kt                        -- ADD optional collectionCount + onCollectionClick slot

:feature:home
  CollectOverlay.kt                         -- rewrite signature; group-aware 3-state button; inline NewGroupForm
  HomeState.kt                              -- ADD groups, selectedGroupIdInOverlay, collectedGroupColorByKey, collectionCount, overlayState, newGroupForm
  HomePresenter.kt                          -- ADD GetGroupsFlowUseCase, AddGroupUseCase; combine flows; commit dispatcher
  HomeUi.kt                                 -- wire BillboardHeader badge; pass colors to RankingRow; new overlay params
  RankingList/RankingRow                    -- ADD collectedGroupColor: Color?, combinedClickable(onLongClick)

:feature:collection
  CollectionState.kt                        -- rewrite; cardsInCurrentGroup, groups, currentGroupId, nowPlayingKey, playerState, sidebar, newGroupForm, pendingDeleteGroupId
  CollectionPresenter.kt                    -- inject group + player use cases; combined flows; eventSink dispatcher
  CollectionUi.kt                           -- new layout: header + NowPlayingPlayer + MiniRail | GroupSidebar overlay
  CardDetailUi.kt                           -- ADD GroupChip(card.groupId) row
  component/NowPlayingPlayer.kt
  component/MiniRail.kt
  component/EmptyGroupPlaceholder.kt        -- single static text composable (NO pulsing slots)
  component/GroupSidebar.kt
  component/NewGroupForm.kt
  build.gradle.kts                          -- ADD implementation(projects.core.player)

:core:data-test
  fixture/FakeGroup.kt
  fixture/FakeCollectedCard.kt              -- ADD groupId = Group.DEFAULT_ID default
  repository/FakeGroupRepository.kt
  repository/FakeCollectionRepository.kt    -- upsert REPLACE semantics, groupId preserved

:core:circuit
  Screens.kt                                -- NO CHANGE (Collection and CardDetail already exist)
```

### Deleted files
```
:feature:collection/component/OrbitLayout.kt
:feature:collection/component/EmptySlot.kt
```

### Module graph
No new module-to-module edges except `:feature:collection → :core:player` (already legal per architecture rules — player is consumed by `:feature:home` already). No reverse dependencies introduced.

---

## 3. Data Model

### Domain
```kotlin
data class Group(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long,
) {
    companion object { const val DEFAULT_ID = 1L }
}

data class CollectedCard(
    val key: String,            // "title::artist" (existing)
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
    val groupId: Long,          // NEW
)
// MAX_SLOTS constant removed.
```

### Entity / DB

```kotlin
@Entity(
    tableName = "groups",
    indices = [Index(value = ["nameNormalized"], unique = true)],
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,           // display
    val nameNormalized: String, // trim().lowercase() — uniqueness key
    val colorArgb: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "collected_cards",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("groupId")],
)
data class CollectedCardEntity(
    @PrimaryKey val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
    val groupId: Long,          // NEW
)

@Database(
    entities = [CollectedCardEntity::class, GroupEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun groupDao(): GroupDao
}
```

### DAO additions
```kotlin
// GroupDao
@Query("SELECT * FROM groups ORDER BY id ASC")
fun observeAll(): Flow<List<GroupEntity>>

@Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
suspend fun getById(id: Long): GroupEntity?

@Insert
suspend fun insert(entity: GroupEntity): Long

@Query("DELETE FROM groups WHERE id = :id AND id != 1")
suspend fun deleteById(id: Long): Int

@Query("SELECT EXISTS(SELECT 1 FROM groups WHERE nameNormalized = :normalized)")
suspend fun existsByName(normalized: String): Boolean

// CollectionDao additions / changes
@Query("SELECT * FROM collected_cards WHERE groupId = :groupId ORDER BY collectedAt DESC")
fun observeByGroup(groupId: Long): Flow<List<CollectedCardEntity>>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsert(entity: CollectedCardEntity)
// Old `insert(IGNORE)` is removed; REPLACE provides "one song = one group" semantics naturally.

@Query("DELETE FROM collected_cards WHERE groupId = :groupId")
suspend fun deleteByGroup(groupId: Long)
```

### Migration
- `Room.databaseBuilder(...).fallbackToDestructiveMigration(dropAllTables = true).addCallback(...)`.
- `RoomDatabase.Callback.onCreate(db)` inserts the Default group with `id = 1, name = "Default", colorArgb = BillboardColor.HoloGreen.toArgb(), createdAt = now()`. Because autoGenerate seed starts at the first INSERT, the first inserted row is id = 1.
- Demo flavor `GroupDataSourceImpl` mirrors the same Default seed in its initial `MutableStateFlow` value.

### Color serialization
- Compose `Color` ↔ `Int` (`toArgb()` / `Color(int)`).
- Preset palette in `BillboardColor`: `HoloGreen` (#00FF85), `HoloAmber` (#FFB400), `HoloBlue` (#5B8DEF), `HoloMagenta` (#E879F9). These are theme-independent brand swatches per `07-design-system.md`.

---

## 4. Domain Layer

```kotlin
class AddGroupUseCase @Inject constructor(private val repo: GroupRepository) {
    suspend operator fun invoke(name: String, colorArgb: Int): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(GroupValidationError.Empty)
        if (trimmed.length > 20) return Result.failure(GroupValidationError.TooLong)
        if (repo.existsByName(trimmed)) return Result.failure(GroupValidationError.DuplicateName)
        return runCatching { repo.add(trimmed, colorArgb) }
            .recoverCatching { e ->
                if (e is SQLiteConstraintException) throw GroupValidationError.DuplicateName
                else throw e
            }
    }
}

class RemoveGroupUseCase @Inject constructor(private val repo: GroupRepository) {
    suspend operator fun invoke(id: Long) {
        require(id != Group.DEFAULT_ID) { "Default group cannot be deleted" }
        repo.remove(id)   // Room CASCADE drops collected_cards rows with this groupId
    }
}

sealed class GroupValidationError(msg: String) : Throwable(msg) {
    data object Empty : GroupValidationError("이름을 입력하세요")
    data object TooLong : GroupValidationError("20자 이하로 입력하세요")
    data object DuplicateName : GroupValidationError("이미 같은 이름의 그룹이 있어요")
}

class GetGroupsFlowUseCase @Inject constructor(private val repo: GroupRepository) {
    operator fun invoke(): Flow<List<Group>> = repo.getGroupsFlow()
}
```

`AddToCollectionUseCase` keeps its current shape but `CollectedCard.groupId` carries the destination group. `RemoveAllFromCollectionUseCase` is unchanged (clears all cards across all groups).

---

## 5. UI

### 5.1 Shared (`:core:design-system/componenet/group/`)
- **GroupDot** — `colorArgb: Int`, `size: Dp = 12.dp`. Draws an inner solid circle + outer glow via `drawBehind`. Reused by RankingRow corner indicator and Sidebar row indicator.
- **GroupChip** — pill, background `Color(colorArgb).copy(alpha = 0.18f)`, foreground `Color(colorArgb)`, 1.dp border same color. Typography `BillboardTheme.typography.labelSm()`.
- **GroupDropdown** — closed renders `GroupChip` + 12.dp chevron icon (rotates 0° → 180° via `animateFloatAsState`, read inside `graphicsLayer { rotationZ = providerValue() }` to stay on draw phase). Open uses Material3 `DropdownMenu` skinned with `BillboardTheme` tokens; rows render `GroupDot + name`; terminal divider + `"+ NEW GROUP"` row that triggers `onCreateNew()`.

### 5.2 `BillboardHeader` change
Add two optional parameters:
```kotlin
fun BillboardHeader(
    title: String,
    isLogoVisible: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    onLeadingIconClick: (() -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    collectionCount: Int? = null,
    onCollectionClick: (() -> Unit)? = null,
)
```
- When `collectionCount != null`, render `IcoCollection` left of `trailingIcon` with a 16.dp pill badge showing the count when `count > 0`. Badge color: `BillboardTheme.colorScheme.holoGlow` (or whichever accent token already exists for badges; add semantic token if missing).
- Default `null` keeps all existing callers untouched.

### 5.3 `RankingRow` change (`:feature:home`)
- Add `collectedGroupColor: Color? = null`.
- Switch the row's `Modifier.clickable` to `Modifier.combinedClickable(onClick = ..., onLongClick = ...)`. Compose's default long-press threshold is ~500 ms, matching the handoff.
- When `collectedGroupColor != null`, overlay `GroupDot(colorArgb = it.toArgb())` on top-end of the thumbnail with `offset(x = 3.dp, y = (-3).dp)`.

### 5.4 `CollectOverlay` rewrite (`:feature:home/component`)
New signature:
```kotlin
fun CollectOverlay(
    visible: Boolean,
    chart: Chart?,
    overlayState: OverlayCollectState,            // Uncollected | Collected(currentGroupId)
    groups: ImmutableList<Group>,
    selectedGroupId: Long,
    newGroupForm: NewGroupFormState?,             // null = dropdown mode; non-null = inline form
    modifier: Modifier = Modifier,
    onSelectGroup: (Long) -> Unit,
    onCreateNewGroupClick: () -> Unit,
    onNewGroupNameChange: (String) -> Unit,
    onNewGroupHexChange: (String) -> Unit,
    onSubmitNewGroup: () -> Unit,
    onCancelNewGroup: () -> Unit,
    onCommit: () -> Unit,                         // add / move / remove — presenter decides
    onDismiss: () -> Unit,
)
```
- `isCollectionFull` parameter removed (unlimited).
- 360.dp radial glow behind the 240.dp HoloCard uses `Color(selectedGroupId-resolved)` gradient. Color transitions animate via `animateColorAsState`.
- Action button label/style derived from `overlayState` + `selectedGroupId`:
  - `Uncollected` → `"ADD TO {GROUP}"`, background `Color(groupColor)`.
  - `Collected(curr)` where `curr == selectedGroupId` → `"REMOVE FROM COLLECTION"`, transparent bg + white 1.dp border.
  - `Collected(curr)` where `curr != selectedGroupId` → `"MOVE TO {GROUP}"`, background `Color(groupColor)`.
- Sparkle burst is gated to commit success only (12 particles, 600 ms, 120.dp radius via existing `SparkleEffect`).

### 5.5 `CollectionUi.kt` rewrite (`:feature:collection`)

```
Scaffold(
    topBar = BillboardHeader(
        title = "COLLECTION",
        subtitle = "${allCards.size} TOTAL · ${cardsInCurrentGroup.size} IN ${currentGroup.name}"  (subtitle slot if available; else as second-line composable inside header content),
        leadingIcon = ArrowBack, onLeadingIconClick = -> OnBackClick,
    ),
) {
  Box(fillMaxSize, radial bg) {
    Column {
      if (cardsInCurrentGroup.isEmpty()) {
        NowPlayingPlayer(videoId = null)      // dashed "NOTHING PLAYING"
        Spacer(weight 1f)
        EmptyGroupPlaceholder()
      } else {
        NowPlayingPlayer(playerState, modifier.fillMaxWidth().aspectRatio(16/9f))
        Column {
          GroupChip(currentCard.groupId resolved)
          Text(currentCard.title, titleLg)
          Text(currentCard.artist, bodyMd)
        }
        Spacer(weight 1f)
        MiniRail(
          cards = cardsInCurrentGroup,
          activeKey = nowPlayingKey,
          onSelect = { OnSelectCard(it) },
        )
      }
    }
    if (nowPlayingKey != null) {
      InspectPin(modifier.align(TopEnd).padding(top = 72.dp, end = 16.dp), onClick = OnInspectClick)
    }
    GroupSidebar(
      modifier = Modifier.align(CenterEnd),
      sidebar = state.sidebar,
      groups = state.groups,
      currentGroupId = state.currentGroupId,
      pendingDeleteGroupId = state.pendingDeleteGroupId,
      newGroupForm = state.newGroupForm,
      onToggle = { OnSidebarToggle(it) },
      onSelectGroup = { OnSelectGroup(it) },
      onRequestDelete = { OnRequestDeleteGroup(it) },
      onConfirmDelete = { OnConfirmDeleteGroup },
      onCancelDelete = { OnCancelDeleteGroup },
      onNewGroupClick = { OnNewGroupClick },
      onCancelNewGroup = { OnCancelNewGroup },
      onNewGroupNameChange = { OnNewGroupNameChange(it) },
      onNewGroupHexChange = { OnNewGroupHexChange(it) },
      onSubmitNewGroup = { OnSubmitNewGroup },
    )
  }
}
```

- **`NowPlayingPlayer`** — wraps `YoutubePlayer` (`:core:player`) + `PlayerControllerButtons`. When `videoId == null` (empty group), renders a dashed `Box` with text "NOTHING PLAYING" (typography `labelMd()`, color `textSecondary`).
- **`MiniRail`** — `LazyRow` of 100.dp cards (album art with gradient + icon fallback). Active card: full saturation + `BillboardColor.HoloBlue` 1.dp border + outer glow; inactive: `Modifier.graphicsLayer { alpha = 0.5f }`.
- **`EmptyGroupPlaceholder`** — static `Column(centered)` with two lines: "이 그룹에 카드가 없어요" (bodyMd, textPrimary) and "홈에서 곡을 길게 눌러 추가하세요" (labelSm, textSecondary). No animations, no pulsing slots.
- **`GroupSidebar`** — `AnimatedVisibility` with `slideInHorizontally / slideOutHorizontally` and `tween(280, easing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f))`.
  - Closed peek (14.dp): vertical color bar (3.dp wide, 16.dp vertical margin) + rotated group name text. Click toggles open.
  - Open (220.dp): dark gradient bg, left 1.dp border, "GROUPS" header, group rows (`GroupDot + name + (count)` + trash icon for non-Default), "+ NEW GROUP" button at bottom, inline `NewGroupForm` when active.
  - Trash icon for non-Default: zero-card → instant delete (`OnRequestDeleteGroup` → presenter sees count = 0 → fires delete immediately). Non-zero card → inline red confirm box (`pendingDeleteGroupId == id`) with cancel / delete buttons.
- **`NewGroupForm`** — name input (max 20, helper text + red error when duplicate or empty), hex input (`#RRGGBB` regex), 4 preset color swatches (tappable to fill hex), `Cancel` + `ADD`. `ADD` disabled when `!isHexValid || isDuplicate || name.trim().isEmpty()`.
- **`InspectPin`** — small 36.dp clickable pin (`semantics` with `contentDescription = "Inspect card detail"`).
- **`CardDetailUi.kt`** — minimal change: add a single row showing `GroupChip(card.groupId resolved)` above existing title.

---

## 6. Presenter State Flow

### 6.1 `CollectionState`
```kotlin
@Immutable
data class CollectionState(
    val groups: ImmutableList<Group>,
    val currentGroupId: Long,
    val cardsInCurrentGroup: ImmutableList<CollectedCard>,
    val nowPlayingKey: String?,
    val playerState: PlayerState,
    val sidebar: SidebarState,
    val newGroupForm: NewGroupFormState?,
    val pendingDeleteGroupId: Long?,
    val eventSink: (CollectionEvent) -> Unit,
) : CircuitUiState

@Immutable
data class SidebarState(val isOpen: Boolean, val countsByGroupId: ImmutableMap<Long, Int>)

@Immutable
data class NewGroupFormState(
    val name: String,
    val hex: String,
    val isDuplicate: Boolean,
    val isHexValid: Boolean,
)

sealed interface CollectionEvent : CircuitUiEvent {
    data object OnBackClick : CollectionEvent
    data class OnSelectCard(val key: String) : CollectionEvent
    data object OnInspectClick : CollectionEvent
    data class OnSidebarToggle(val open: Boolean) : CollectionEvent
    data class OnSelectGroup(val id: Long) : CollectionEvent
    data class OnRequestDeleteGroup(val id: Long) : CollectionEvent
    data object OnConfirmDeleteGroup : CollectionEvent
    data object OnCancelDeleteGroup : CollectionEvent
    data object OnNewGroupClick : CollectionEvent
    data object OnCancelNewGroup : CollectionEvent
    data class OnNewGroupNameChange(val name: String) : CollectionEvent
    data class OnNewGroupHexChange(val hex: String) : CollectionEvent
    data object OnSubmitNewGroup : CollectionEvent
}
```

### 6.2 `CollectionPresenter` (key logic)
- Inject: `GetGroupsFlowUseCase`, `GetCollectionFlowUseCase`, `AddGroupUseCase`, `RemoveGroupUseCase`, `AddToCollectionUseCase` (for cascade-on-delete is not needed — repo handles), `ResolveYoutubeVideoIdUseCase` (whatever Home uses today), `@Assisted Navigator`.
- `groups`, `allCards` collected via `produceRetainedState`.
- `cardsInCurrentGroup = remember(allCards, currentGroupId) { allCards.filter { it.groupId == currentGroupId }.toImmutableList() }`.
- `countsByGroupId = remember(allCards) { allCards.groupingBy { it.groupId }.eachCount().toImmutableMap() }`.
- `playerState = rememberRetained { PlayerState() }` (Home pattern).
- `LaunchedEffect(currentGroupId, cardsInCurrentGroup.firstOrNull()?.key)` — if `nowPlayingKey` not in current group, reset to first card or null.
- `LaunchedEffect(nowPlayingKey)` — resolve videoId for that card, then `playerState.loadVideo(videoId, 0f)`. Wrap in `runCatching` + `Timber.e` per `05-error-handling.md`.
- `OnRequestDeleteGroup(id)`: if `countsByGroupId[id] ?: 0 == 0` → call `removeGroup(id)` immediately; else `pendingDeleteGroupId = id`.
- `OnConfirmDeleteGroup`: call `removeGroup(pendingDeleteGroupId)`. On success, if `currentGroupId == deletedId` → reset to `Group.DEFAULT_ID`. Clear `pendingDeleteGroupId`.
- `OnSubmitNewGroup`: parse hex, call `addGroup(name, color)`. On success, `currentGroupId = newId`, close form, close sidebar.
- `OnSelectGroup(id)`: `currentGroupId = id; sidebarOpen = false`.

### 6.3 `HomeState` additions
- `groups: ImmutableMap<Long, Group>` — id → Group for overlay rendering.
- `selectedGroupIdInOverlay: Long` — seeded when overlay opens: collected → its group; else last selected group; else `DEFAULT_ID`.
- `collectedGroupColorByKey: ImmutableMap<String, Int>` — RankingRow corner-dot color lookup.
- `collectionCount: Int` — header badge.
- `overlayState: OverlayCollectState` — `Uncollected | Collected(groupId)`.
- `newGroupFormInOverlay: NewGroupFormState?` — same shape as collection's NewGroupForm.

### 6.4 `HomePresenter` additions
- Inject `GetGroupsFlowUseCase`, `AddGroupUseCase`.
- Combine `getCollectionFlow()` + `getGroupsFlow()` to derive `collectedGroupColorByKey` (`cards.associate { it.key to groups.first { g -> g.id == it.groupId }.colorArgb }.toImmutableMap()`) and `collectionCount`.
- New events:
  - `OnLongPressRow(item)` → opens overlay; sets `selectedGroupIdInOverlay` per the "collected → its group; else last; else Default" rule.
  - `OnSelectGroupInOverlay(id)` → updates `selectedGroupIdInOverlay`.
  - `OnCreateNewGroupClickInOverlay` / `OnNewGroupNameChangeInOverlay` / `OnNewGroupHexChangeInOverlay` / `OnSubmitNewGroupInOverlay` / `OnCancelNewGroupInOverlay` — mirror Collection's form events.
  - `OnCommitOverlay` → dispatcher:
    - `overlayState == Uncollected` → `addToCollection(card.copy(groupId = selectedGroupIdInOverlay))`.
    - `overlayState == Collected(curr) && curr == selectedGroupIdInOverlay` → `removeFromCollection(key)`.
    - `overlayState == Collected(curr) && curr != selectedGroupIdInOverlay` → `addToCollection(card.copy(groupId = selectedGroupIdInOverlay))` — REPLACE semantics in DAO move the card.
  - `OnCollectionHeaderClick` → `navigator.goTo(BillboardScreen.Collection)`.

### 6.5 Form name de-duplication
Both `CollectionPresenter` and `HomePresenter` compute `isDuplicate` in their respective `OnNewGroupNameChange` handlers as:
```kotlin
groups.any { it.name.trim().lowercase() == event.name.trim().lowercase() }
```
This matches `GroupEntity.nameNormalized` exactly. Race-on-submit fallback: `AddGroupUseCase` converts `SQLiteConstraintException` to `GroupValidationError.DuplicateName`, presenter logs via Timber.

---

## 7. Test Strategy

### 7.1 Unit (`:core:domain/src/test`, JUnit4 + MockK)
- `AddGroupUseCaseTest` — empty / too long / duplicate / success / SQLiteConstraintException → DuplicateName.
- `RemoveGroupUseCaseTest` — Default id rejects with `IllegalArgumentException`; non-Default delegates.
- `AddToCollectionUseCaseTest` — drop old `MAX_SLOTS` cases; verify `groupId` is preserved end-to-end (mock repo `add(card)` and verify captured card).

### 7.2 Fakes (`:core:data-test`)
- `FakeGroupRepository` — single `MutableStateFlow<List<Group>>`, Default seeded; `existsByName` mirrors normalize rule; `add` returns synthetic monotonically increasing id.
- `FakeCollectionRepository` — `upsert` REPLACE semantics, `groupId` preserved, `deleteByGroup` for cascade.
- `fakeCollectedCard(...)` adds `groupId: Long = Group.DEFAULT_ID` default param.
- `fakeGroup(id, name, color)` new fixture.

### 7.3 Instrumented (`:feature:collection/src/androidTest`)
- `CollectionPresenterTest` covers:
  - Initial `currentGroupId == DEFAULT_ID`, sidebar closed.
  - `OnSelectGroup` updates id and closes sidebar.
  - `OnSelectCard` updates `nowPlayingKey`.
  - `OnInspectClick` navigates to `CardDetail(nowPlayingKey)`.
  - Empty current group → `cardsInCurrentGroup.isEmpty()` and `nowPlayingKey == null`.
  - `OnRequestDeleteGroup` zero-count → immediate delete.
  - `OnRequestDeleteGroup` non-zero → `pendingDeleteGroupId` set; `OnConfirmDeleteGroup` cascades; `currentGroupId` resets to Default if it was the deleted one.
  - `OnSubmitNewGroup` success → `currentGroupId = newId`, form/sidebar closed.
  - `OnNewGroupNameChange` to duplicate → `isDuplicate = true`.
  - `OnRequestDeleteGroup(DEFAULT_ID)` → silently logged, no state change.
- `CollectionUiTest`:
  - Empty group renders `EmptyGroupPlaceholder` text (not LazyRow).
  - Mini-rail card tap dispatches `OnSelectCard`.
  - Sidebar peek tap toggles to open (testTag-driven).
  - NewGroupForm with duplicate name disables `ADD`.
  - Trash → confirm box → confirm removes the group row.

### 7.4 Instrumented (`:feature:home/src/androidTest`)
- `HomePresenterTest` additions: long-press on collected row seeds overlay's `selectedGroupIdInOverlay` to that card's group; selecting a different group + commit fires `addToCollection` (REPLACE move); combined flows expose `collectionCount` + `collectedGroupColorByKey`.
- `HomeUiTest` additions: header badge count rendered; RankingRow corner GroupDot appears for collected rows.

### 7.5 Out of scope (intentional)
- YouTube playback realism, sparkle particle pixels, 360.dp glow blur regression — manual verification only.
- Macrobenchmark trace additions — handled by existing `generate-baseline-profile` skill.

---

## 8. Supplemental Definitions

### `OverlayCollectState`
```kotlin
sealed interface OverlayCollectState {
    data object Uncollected : OverlayCollectState
    data class Collected(val groupId: Long) : OverlayCollectState
}
```
Lives in `:feature:home` next to `CollectOverlay.kt`.

### Hex parsing
`NewGroupForm` accepts strings matching `Regex("^#[0-9A-Fa-f]{6}$")`. Parsing: `Color(android.graphics.Color.parseColor(hex)).toArgb()`. Preset swatch taps simply write the swatch's `#RRGGBB` string into the hex field.

### `BillboardHeader` subtitle on Collection screen
`BillboardHeader` does NOT gain a subtitle slot. The subtitle line ("`{N} TOTAL · {M} IN {GROUP}`") is rendered as a separate `Text` composable placed inside `CollectionUi`'s body, immediately under the topBar, styled as `BillboardTheme.typography.labelSm()` in the current group color. This keeps header changes scoped to just the collection-badge slot.

## 9. Open Items
None — all decisions captured above.

## 10. Out of Scope
- Importing / exporting groups.
- Reordering groups.
- Renaming the Default group (forbidden by design).
- Per-group play queue (mini-rail order is collectedAt DESC, fixed).
- Sharing cards.
