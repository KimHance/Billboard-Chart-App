# Card Collection + Groups Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Spec:** `docs/superpowers/specs/2026-05-16-card-collection-groups-design.md`

**Goal:** Add group classification to card collection, replace the orbit collection screen with a YouTube-player + mini-rail + right sidebar layout, integrate everything through Home long-press flow.

**Architecture:** New `groups` table joined to existing `collected_cards` via `groupId` FK (CASCADE). Groups exposed through their own `GroupRepository` + use cases. Collection screen reuses `:core:player` `YoutubePlayer`; mini-rail card taps re-load video URL. Default group is seeded on first launch and is protected from deletion at use-case + DAO levels.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Slack Circuit, Hilt, Room (room3), Coroutines + Flow, kotlinx-collections-immutable, JUnit4 + MockK + Turbine + Compose UI Test.

**Commit style:** match repo history — `feat(...)`, `refactor(...)`, `test(...)`, `chore(...)`. All commits include `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>`.

---

## Phase A — Data Layer

### Task 1: Add `Group` model + `GroupRepository` interface

**Files:**
- Create: `core/data/src/main/java/com/hancekim/billboard/core/data/model/Group.kt`
- Create: `core/data/src/main/java/com/hancekim/billboard/core/data/repository/GroupRepository.kt`

- [x] **Step 1: Create `Group` model**

```kotlin
// core/data/src/main/java/com/hancekim/billboard/core/data/model/Group.kt
package com.hancekim.billboard.core.data.model

data class Group(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long,
) {
    companion object {
        // Default 그룹 고정 id — 삭제 금지 대상
        const val DEFAULT_ID: Long = 1L
    }
}
```

- [x] **Step 2: Create `GroupRepository` interface**

```kotlin
// core/data/src/main/java/com/hancekim/billboard/core/data/repository/GroupRepository.kt
package com.hancekim.billboard.core.data.repository

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getGroupsFlow(): Flow<List<Group>>
    suspend fun getById(id: Long): Group?
    suspend fun existsByName(name: String): Boolean
    suspend fun add(name: String, colorArgb: Int): Long
    suspend fun remove(id: Long)
}
```

- [x] **Step 3: Verify compilation**

Run: `./gradlew :core:data:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [x] **Step 4: Commit**

```bash
git add core/data/src/main/java/com/hancekim/billboard/core/data/model/Group.kt \
        core/data/src/main/java/com/hancekim/billboard/core/data/repository/GroupRepository.kt
git commit -m "$(cat <<'EOF'
feat(data): add Group model and repository interface

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Extend `CollectedCard` with `groupId` and drop `MAX_SLOTS`

**Files:**
- Modify: `core/data/src/main/java/com/hancekim/billboard/core/data/model/CollectedCard.kt`

- [ ] **Step 1: Replace model**

```kotlin
package com.hancekim.billboard.core.data.model

import com.hancekim.billboard.core.data.model.Group

data class CollectedCard(
    val key: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val collectedAt: Long,
    val lastWeek: Int,
    val peakPosition: Int,
    val weeksOnChart: Int,
    val groupId: Long = Group.DEFAULT_ID,
)
```

Default value keeps existing call sites compiling until they are updated explicitly. MAX_SLOTS constant is removed.

- [ ] **Step 2: Find and fix every reference to `MAX_SLOTS`**

Run: `grep -rn "MAX_SLOTS" --include="*.kt"`
For each hit (expected in `:core:domain`, `:feature:collection`, tests), delete the reference (slot-full guards become no-ops). Compilation will guide the rest.

- [ ] **Step 3: Verify compilation across data + domain + feature**

Run: `./gradlew :core:data:compileDebugKotlin :core:domain:compileKotlin :core:data-impl:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git commit -am "$(cat <<'EOF'
refactor(data): add groupId to CollectedCard and remove MAX_SLOTS

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Group + CollectedCard Room entities, DAOs, database v2

**Files:**
- Create: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/GroupEntity.kt`
- Create: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/GroupDao.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/CollectedCardEntity.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/CollectionDao.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/CollectionDatabase.kt`

- [ ] **Step 1: Create `GroupEntity`**

```kotlin
package com.hancekim.billboard.core.datasource.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index(value = ["nameNormalized"], unique = true)],
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val nameNormalized: String,
    val colorArgb: Int,
    val createdAt: Long,
)
```

- [ ] **Step 2: Create `GroupDao`**

```kotlin
package com.hancekim.billboard.core.datasource.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY id ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GroupEntity?

    @Insert
    suspend fun insert(entity: GroupEntity): Long

    // Default(=1) 안전망 — UseCase 가드 + DAO 가드 이중 방어
    @Query("DELETE FROM groups WHERE id = :id AND id != 1")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM groups WHERE nameNormalized = :normalized)")
    suspend fun existsByName(normalized: String): Boolean
}
```

- [ ] **Step 3: Update `CollectedCardEntity` with `groupId` + FK CASCADE**

```kotlin
package com.hancekim.billboard.core.datasource.db

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
    val groupId: Long,
)
```

- [ ] **Step 4: Update `CollectionDao` — add `observeByGroup`, replace `insert` with `upsert`, add `deleteByGroup`**

Replace the existing DAO with:
```kotlin
package com.hancekim.billboard.core.datasource.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collected_cards ORDER BY collectedAt DESC")
    fun observeAll(): Flow<List<CollectedCardEntity>>

    @Query("SELECT * FROM collected_cards WHERE groupId = :groupId ORDER BY collectedAt DESC")
    fun observeByGroup(groupId: Long): Flow<List<CollectedCardEntity>>

    // 같은 key 가 들어오면 REPLACE — 한 곡이 다른 그룹으로 이동하는 의미
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CollectedCardEntity)

    @Query("DELETE FROM collected_cards WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM collected_cards WHERE groupId = :groupId")
    suspend fun deleteByGroup(groupId: Long)

    @Query("DELETE FROM collected_cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM collected_cards WHERE `key` = :key LIMIT 1")
    fun observeByKey(key: String): Flow<CollectedCardEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM collected_cards WHERE `key` = :key)")
    suspend fun exists(key: String): Boolean

    @Query("SELECT COUNT(*) FROM collected_cards")
    suspend fun count(): Int
}
```

- [ ] **Step 5: Bump database to v2**

```kotlin
package com.hancekim.billboard.core.datasource.db

import androidx.room3.Database
import androidx.room3.RoomDatabase

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

- [ ] **Step 6: Compile**

Run: `./gradlew :core:data-source:compileProdDebugKotlin`
Expected: BUILD SUCCESSFUL (existing entity-mapper code in `CollectionDataSourceImpl` will need a `groupId` field — fixed in Task 4).

- [ ] **Step 7: Commit**

```bash
git add core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/db/
git commit -m "$(cat <<'EOF'
feat(data-source): add Group entity/DAO and bump DB to v2

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 4: `GroupDataSource` (prod Room + demo in-memory) + `CollectionDataSourceImpl` `groupId` plumbing + Default seed

**Files:**
- Create: `core/data-source/src/main/java/com/hancekim/billboard/core/datasource/GroupDataSource.kt`
- Create: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/GroupDataSourceImpl.kt`
- Create: `core/data-source/src/demo/java/com/hancekim/billboard/core/datasource/GroupDataSourceImpl.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/CollectionDataSourceImpl.kt`
- Modify: `core/data-source/src/demo/java/com/hancekim/billboard/core/datasource/CollectionDataSourceImpl.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/di/DatabaseModule.kt`
- Modify: `core/data-source/src/prod/java/com/hancekim/billboard/core/datasource/di/DataSourceModule.kt`
- Modify: `core/data-source/src/demo/java/com/hancekim/billboard/core/datasource/di/DataSourceModule.kt`

- [ ] **Step 1: Add shared `GroupDataSource` interface**

```kotlin
package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupDataSource {
    fun observeAll(): Flow<List<Group>>
    suspend fun getById(id: Long): Group?
    suspend fun existsByName(normalized: String): Boolean
    suspend fun insert(name: String, colorArgb: Int): Long
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 2: Add prod `GroupDataSourceImpl` (Room)**

```kotlin
package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.datasource.db.GroupDao
import com.hancekim.billboard.core.datasource.db.GroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupDataSourceImpl @Inject constructor(
    private val dao: GroupDao,
) : GroupDataSource {

    override fun observeAll(): Flow<List<Group>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Group? =
        dao.getById(id)?.toDomain()

    override suspend fun existsByName(normalized: String): Boolean =
        dao.existsByName(normalized)

    override suspend fun insert(name: String, colorArgb: Int): Long {
        val trimmed = name.trim()
        val entity = GroupEntity(
            name = trimmed,
            nameNormalized = trimmed.lowercase(),
            colorArgb = colorArgb,
            createdAt = System.currentTimeMillis(),
        )
        return dao.insert(entity)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    private fun GroupEntity.toDomain() = Group(id, name, colorArgb, createdAt)
}
```

- [ ] **Step 3: Add demo `GroupDataSourceImpl` (in-memory, Default seeded)**

```kotlin
package com.hancekim.billboard.core.datasource

import com.hancekim.billboard.core.data.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupDataSourceImpl @Inject constructor() : GroupDataSource {

    private val nextId = java.util.concurrent.atomic.AtomicLong(2L) // 1 is Default
    private val state = MutableStateFlow(
        listOf(
            Group(
                id = Group.DEFAULT_ID,
                name = "Default",
                colorArgb = 0xFF00FF85.toInt(),
                createdAt = System.currentTimeMillis(),
            ),
        ),
    )

    override fun observeAll(): Flow<List<Group>> = state

    override suspend fun getById(id: Long): Group? = state.value.find { it.id == id }

    override suspend fun existsByName(normalized: String): Boolean =
        state.value.any { it.name.trim().lowercase() == normalized }

    override suspend fun insert(name: String, colorArgb: Int): Long {
        val id = nextId.getAndIncrement()
        state.value = state.value + Group(id, name.trim(), colorArgb, System.currentTimeMillis())
        return id
    }

    override suspend fun deleteById(id: Long) {
        if (id == Group.DEFAULT_ID) return
        state.value = state.value.filterNot { it.id == id }
    }
}
```

- [ ] **Step 4: Update prod `CollectionDataSourceImpl` to map `groupId`**

(In the prod impl, anywhere `CollectedCardEntity` is constructed or mapped to/from `CollectedCard`, add `groupId`. Use `upsert` instead of `insert`.)

```kotlin
// Replace insert mapping with upsert + groupId
override suspend fun insert(card: CollectedCard) {
    dao.upsert(card.toEntity())
}

private fun CollectedCard.toEntity() = CollectedCardEntity(
    key = key, title = title, artist = artist, albumArtUrl = albumArtUrl,
    collectedAt = collectedAt, lastWeek = lastWeek,
    peakPosition = peakPosition, weeksOnChart = weeksOnChart,
    groupId = groupId,
)

private fun CollectedCardEntity.toDomain() = CollectedCard(
    key = key, title = title, artist = artist, albumArtUrl = albumArtUrl,
    collectedAt = collectedAt, lastWeek = lastWeek,
    peakPosition = peakPosition, weeksOnChart = weeksOnChart,
    groupId = groupId,
)
```

- [ ] **Step 5: Update demo `CollectionDataSourceImpl` semantics — REPLACE on same key**

```kotlin
override suspend fun insert(card: CollectedCard) {
    cards.value = cards.value.filterNot { it.key == card.key } + card
}
```

- [ ] **Step 6: Update prod `DatabaseModule` with destructive migration + Default seed callback**

```kotlin
package com.hancekim.billboard.core.datasource.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.datasource.db.CollectionDao
import com.hancekim.billboard.core.datasource.db.CollectionDatabase
import com.hancekim.billboard.core.datasource.db.GroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DEFAULT_GROUP_COLOR_ARGB: Int = 0xFF00FF85.toInt()

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCollectionDatabase(
        @ApplicationContext context: Context,
    ): CollectionDatabase =
        Room.databaseBuilder<CollectionDatabase>(
            context = context,
            name = "billboard_collection.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(connection: SQLiteConnection) {
                    val now = System.currentTimeMillis()
                    connection.execSQL(
                        "INSERT INTO groups (id, name, nameNormalized, colorArgb, createdAt) " +
                            "VALUES (${Group.DEFAULT_ID}, 'Default', 'default', $DEFAULT_GROUP_COLOR_ARGB, $now)"
                    )
                }
            })
            .build()

    @Provides
    fun provideCollectionDao(db: CollectionDatabase): CollectionDao = db.collectionDao()

    @Provides
    fun provideGroupDao(db: CollectionDatabase): GroupDao = db.groupDao()
}
```

If the Room version in use doesn't expose `SQLiteConnection`, fall back to `SupportSQLiteDatabase` (`override fun onCreate(db: SupportSQLiteDatabase)`) — pick whichever the existing room3 dependency supports (check the imports already present).

- [ ] **Step 7: Bind `GroupDataSource` in both flavor `DataSourceModule.kt`**

prod:
```kotlin
@Binds @Singleton
abstract fun bindGroupDataSource(impl: GroupDataSourceImpl): GroupDataSource
```
demo: same `@Binds`.

- [ ] **Step 8: Compile both flavors**

Run: `./gradlew :core:data-source:compileProdDebugKotlin :core:data-source:compileDemoDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add core/data-source/
git commit -m "$(cat <<'EOF'
feat(data-source): add GroupDataSource, wire Default seed and destructive migration

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: `GroupRepositoryImpl` + Hilt binding + `CollectionRepositoryImpl` cleanup

**Files:**
- Create: `core/data-impl/src/main/java/com/hancekim/billboard/core/dataimpl/repository/GroupRepositoryImpl.kt`
- Modify: `core/data-impl/src/main/java/com/hancekim/billboard/core/dataimpl/di/RepositoryModule.kt`
- Modify: `core/data-impl/src/main/java/com/hancekim/billboard/core/dataimpl/repository/CollectionRepositoryImpl.kt`

- [ ] **Step 1: Implement `GroupRepositoryImpl`**

```kotlin
package com.hancekim.billboard.core.dataimpl.repository

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import com.hancekim.billboard.core.datasource.GroupDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val dataSource: GroupDataSource,
) : GroupRepository {

    override fun getGroupsFlow(): Flow<List<Group>> = dataSource.observeAll()

    override suspend fun getById(id: Long): Group? = dataSource.getById(id)

    override suspend fun existsByName(name: String): Boolean =
        dataSource.existsByName(name.trim().lowercase())

    override suspend fun add(name: String, colorArgb: Int): Long =
        dataSource.insert(name, colorArgb)

    override suspend fun remove(id: Long) = dataSource.deleteById(id)
}
```

- [ ] **Step 2: Add `@Binds` in `RepositoryModule`**

```kotlin
@Binds
@Singleton
abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository
```

- [ ] **Step 3: Compile**

Run: `./gradlew :core:data-impl:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add core/data-impl/
git commit -m "$(cat <<'EOF'
feat(data-impl): bind GroupRepository

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: Domain — `GroupValidationError`, `AddGroupUseCase`, `RemoveGroupUseCase`, `GetGroupsFlowUseCase` (TDD)

**Files:**
- Create: `core/domain/src/main/java/com/hancekim/billboard/core/domain/GroupValidationError.kt`
- Create: `core/domain/src/main/java/com/hancekim/billboard/core/domain/AddGroupUseCase.kt`
- Create: `core/domain/src/main/java/com/hancekim/billboard/core/domain/RemoveGroupUseCase.kt`
- Create: `core/domain/src/main/java/com/hancekim/billboard/core/domain/GetGroupsFlowUseCase.kt`
- Test: `core/domain/src/test/java/com/hancekim/billboard/core/domain/AddGroupUseCaseTest.kt`
- Test: `core/domain/src/test/java/com/hancekim/billboard/core/domain/RemoveGroupUseCaseTest.kt`

- [ ] **Step 1: Write `AddGroupUseCaseTest` (failing)**

```kotlin
package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.GroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddGroupUseCaseTest {

    private val repo: GroupRepository = mockk(relaxed = true)
    private val useCase = AddGroupUseCase(repo)

    @Test
    fun `빈 이름이면 Empty 실패`() = runTest {
        val result = useCase("   ", 0xFFFFFFFF.toInt())
        assertTrue(result.exceptionOrNull() is GroupValidationError.Empty)
    }

    @Test
    fun `21자 이상이면 TooLong 실패`() = runTest {
        val result = useCase("A".repeat(21), 0)
        assertTrue(result.exceptionOrNull() is GroupValidationError.TooLong)
    }

    @Test
    fun `normalize 기준 중복이면 DuplicateName 실패`() = runTest {
        coEvery { repo.existsByName("workout") } returns true
        val result = useCase("  Workout ", 0)
        assertTrue(result.exceptionOrNull() is GroupValidationError.DuplicateName)
    }

    @Test
    fun `정상 입력이면 새 id 반환하고 trim 된 이름으로 저장`() = runTest {
        coEvery { repo.existsByName(any()) } returns false
        coEvery { repo.add("Workout", 123) } returns 42L
        val result = useCase("  Workout ", 123)
        assertEquals(42L, result.getOrNull())
        coVerify { repo.add("Workout", 123) }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:domain:test --tests "*.AddGroupUseCaseTest"`
Expected: FAIL — `AddGroupUseCase` and `GroupValidationError` do not exist.

- [ ] **Step 3: Implement `GroupValidationError`**

```kotlin
package com.hancekim.billboard.core.domain

sealed class GroupValidationError(message: String) : Throwable(message) {
    data object Empty : GroupValidationError("이름을 입력하세요") {
        private fun readResolve(): Any = Empty
    }
    data object TooLong : GroupValidationError("20자 이하로 입력하세요") {
        private fun readResolve(): Any = TooLong
    }
    data object DuplicateName : GroupValidationError("이미 같은 이름의 그룹이 있어요") {
        private fun readResolve(): Any = DuplicateName
    }
}
```

- [ ] **Step 4: Implement `AddGroupUseCase`**

```kotlin
package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.repository.GroupRepository
import javax.inject.Inject

class AddGroupUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    suspend operator fun invoke(name: String, colorArgb: Int): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(GroupValidationError.Empty)
        if (trimmed.length > 20) return Result.failure(GroupValidationError.TooLong)
        if (repo.existsByName(trimmed)) return Result.failure(GroupValidationError.DuplicateName)
        return runCatching { repo.add(trimmed, colorArgb) }
            .recoverCatching { e ->
                if (e is android.database.sqlite.SQLiteConstraintException) {
                    throw GroupValidationError.DuplicateName
                } else throw e
            }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:domain:test --tests "*.AddGroupUseCaseTest"`
Expected: PASS.

- [ ] **Step 6: Write `RemoveGroupUseCaseTest`**

```kotlin
package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class RemoveGroupUseCaseTest {

    private val repo: GroupRepository = mockk(relaxed = true)
    private val useCase = RemoveGroupUseCase(repo)

    @Test
    fun `Default 그룹 삭제 시 IllegalArgumentException`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(Group.DEFAULT_ID) }
        }
    }

    @Test
    fun `일반 그룹 id 면 repo remove 호출`() = runTest {
        useCase(42L)
        coVerify { repo.remove(42L) }
    }
}
```

- [ ] **Step 7: Implement `RemoveGroupUseCase` and `GetGroupsFlowUseCase`**

```kotlin
// RemoveGroupUseCase.kt
package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    suspend operator fun invoke(id: Long) {
        require(id != Group.DEFAULT_ID) { "Default group cannot be deleted" }
        repo.remove(id)
    }
}
```

```kotlin
// GetGroupsFlowUseCase.kt
package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupsFlowUseCase @Inject constructor(
    private val repo: GroupRepository,
) {
    operator fun invoke(): Flow<List<Group>> = repo.getGroupsFlow()
}
```

- [ ] **Step 8: Run all domain tests**

Run: `./gradlew :core:domain:test`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add core/domain/
git commit -m "$(cat <<'EOF'
feat(domain): add Group use cases with validation and Default guard

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 7: `:core:data-test` — fixtures and fakes for groups + updated card fixture

**Files:**
- Create: `core/data-test/src/main/java/com/hancekim/billboard/core/datatest/fixture/FakeGroup.kt`
- Create: `core/data-test/src/main/java/com/hancekim/billboard/core/datatest/repository/FakeGroupRepository.kt`
- Modify: `core/data-test/src/main/java/com/hancekim/billboard/core/datatest/fixture/FakeCollectedCard.kt`
- Modify: `core/data-test/src/main/java/com/hancekim/billboard/core/datatest/repository/FakeCollectionRepository.kt`

- [ ] **Step 1: Add `fakeGroup`**

```kotlin
package com.hancekim.billboard.core.datatest.fixture

import com.hancekim.billboard.core.data.model.Group

fun fakeGroup(
    id: Long = Group.DEFAULT_ID,
    name: String = "Default",
    colorArgb: Int = 0xFF00FF85.toInt(),
    createdAt: Long = 0L,
): Group = Group(id, name, colorArgb, createdAt)
```

- [ ] **Step 2: Add `groupId` default to `fakeCollectedCard`**

In `FakeCollectedCard.kt`, add `groupId: Long = Group.DEFAULT_ID` to the factory signature and pass it through.

- [ ] **Step 3: Add `FakeGroupRepository`**

```kotlin
package com.hancekim.billboard.core.datatest.repository

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong

class FakeGroupRepository(
    initial: List<Group> = listOf(
        Group(Group.DEFAULT_ID, "Default", 0xFF00FF85.toInt(), 0L)
    ),
) : GroupRepository {
    private val nextId = AtomicLong(initial.maxOf { it.id } + 1L)
    private val state = MutableStateFlow(initial)

    override fun getGroupsFlow(): Flow<List<Group>> = state
    override suspend fun getById(id: Long): Group? = state.value.find { it.id == id }
    override suspend fun existsByName(name: String): Boolean {
        val normalized = name.trim().lowercase()
        return state.value.any { it.name.trim().lowercase() == normalized }
    }
    override suspend fun add(name: String, colorArgb: Int): Long {
        val id = nextId.getAndIncrement()
        state.value = state.value + Group(id, name.trim(), colorArgb, 0L)
        return id
    }
    override suspend fun remove(id: Long) {
        if (id == Group.DEFAULT_ID) return
        state.value = state.value.filterNot { it.id == id }
    }
}
```

- [ ] **Step 4: Update `FakeCollectionRepository`**

Change `add` to REPLACE semantics (filter then append) and ensure `groupId` is preserved.
Add a cascade hook used by tests: when a `FakeGroupRepository.remove(id)` is called, also call `FakeCollectionRepository.removeByGroup(id)` from the test setup (don't try to chain them inside the fake itself — keep fakes orthogonal).

```kotlin
suspend fun removeByGroup(groupId: Long) {
    cards.value = cards.value.filterNot { it.groupId == groupId }
}
```

- [ ] **Step 5: Compile**

Run: `./gradlew :core:data-test:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add core/data-test/
git commit -m "$(cat <<'EOF'
test(data-test): add Group fixtures, FakeGroupRepository, REPLACE collection fake

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Phase B — Design-System (shared UI)

### Task 8: Preset brand color primitives

**Files:**
- Modify: `core/design-foundation/src/main/java/com/hancekim/billboard/core/designfoundation/color/BillboardColor.kt`

- [ ] **Step 1: Append the four preset swatches**

```kotlin
val HoloGreen = Color(0xFF00FF85)
val HoloAmber = Color(0xFFFFB400)
val HoloBlue = Color(0xFF5B8DEF)
val HoloMagenta = Color(0xFFE879F9)
```

(If `BillboardColor` is structured as an `object`, put them inside it; if as a file-level set of vals, match the existing style.)

- [ ] **Step 2: Compile**

Run: `./gradlew :core:design-foundation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add core/design-foundation/
git commit -m "$(cat <<'EOF'
feat(design-foundation): add Holo preset color primitives

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 9: `GroupDot` composable

**Files:**
- Create: `core/design-system/src/main/java/com/hancekim/billboard/core/designsystem/componenet/group/GroupDot.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun GroupDot(
    colorArgb: Int,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    val color = Color(colorArgb)
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.45f), Color.Transparent),
                radius = this.size.minDimension,
            ),
        )
        drawCircle(color = color, radius = this.size.minDimension * 0.42f)
    }
}

@ThemePreviews
@Composable
private fun GroupDotPreview() {
    BillboardTheme {
        GroupDot(colorArgb = BillboardColor.HoloAmber.toArgb())
    }
}
```

(`Color.toArgb()` import: `androidx.compose.ui.graphics.toArgb`.)

- [ ] **Step 2: Compile**

Run: `./gradlew :core:design-system:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add core/design-system/
git commit -m "$(cat <<'EOF'
feat(design-system): add GroupDot composable

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 10: `GroupChip` composable

**Files:**
- Create: `core/design-system/src/main/java/com/hancekim/billboard/core/designsystem/componenet/group/GroupChip.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.preview.ThemePreviews
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun GroupChip(
    group: Group,
    modifier: Modifier = Modifier,
) {
    val color = Color(group.colorArgb)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GroupDot(colorArgb = group.colorArgb, size = 8.dp)
        Text(text = group.name.uppercase(), color = color, style = BillboardTheme.typography.labelSm())
    }
}

@ThemePreviews
@Composable
private fun GroupChipPreview() {
    BillboardTheme {
        GroupChip(Group(1, "Workout", BillboardColor.HoloAmber.toArgb(), 0L))
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :core:design-system:compileDebugKotlin
git add core/design-system/
git commit -m "feat(design-system): add GroupChip composable

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 11: `GroupDropdown` composable

**Files:**
- Create: `core/design-system/src/main/java/com/hancekim/billboard/core/designsystem/componenet/group/GroupDropdown.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.core.designsystem.componenet.group

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun GroupDropdown(
    groups: ImmutableList<Group>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val selected = groups.firstOrNull { it.id == selectedId } ?: groups.first()
    val rotation by animateFloatAsState(if (open) 180f else 0f, label = "dropdown-chevron")

    Row(
        modifier = modifier.noRippleClickable { open = !open },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GroupChip(group = selected)
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = if (open) "그룹 선택 닫기" else "그룹 선택 열기",
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { rotationZ = rotation },
            tint = BillboardTheme.colorScheme.textPrimary,
        )
    }

    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        groups.forEach { group ->
            DropdownMenuItem(
                text = { Text(group.name, style = BillboardTheme.typography.bodyMd()) },
                leadingIcon = { GroupDot(colorArgb = group.colorArgb) },
                onClick = {
                    open = false
                    onSelect(group.id)
                },
            )
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("+ NEW GROUP", style = BillboardTheme.typography.labelSm()) },
            onClick = {
                open = false
                onCreateNew()
            },
        )
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :core:design-system:compileDebugKotlin
git add core/design-system/
git commit -m "feat(design-system): add GroupDropdown composable

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 12: `BillboardHeader` collection badge slot

**Files:**
- Modify: `core/design-system/src/main/java/com/hancekim/billboard/core/designsystem/componenet/header/BillboardHeader.kt`

- [ ] **Step 1: Add optional parameters**

Locate the existing `BillboardHeader` function. Add two parameters with default `null`:
```kotlin
collectionCount: Int? = null,
onCollectionClick: (() -> Unit)? = null,
```

Within the header's trailing row (the area where the existing trailing icon lives), prepend (when `collectionCount != null && onCollectionClick != null`) a clickable icon + badge:

```kotlin
if (collectionCount != null && onCollectionClick != null) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .noRippleClickable(onClick = onCollectionClick)
            .semantics { role = Role.Button; contentDescription = "컬렉션 열기" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BillboardIcons.Collection,
            contentDescription = null,
            tint = BillboardTheme.colorScheme.textPrimary,
        )
        if (collectionCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .background(BillboardTheme.colorScheme.holoGlow, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = collectionCount.toString(),
                    style = BillboardTheme.typography.labelSm(),
                    color = BillboardTheme.colorScheme.textOnAccent,
                )
            }
        }
    }
}
```

If `holoGlow` / `textOnAccent` semantic tokens don't exist in `BillboardColorScheme`, add them first (light + dark variants both) in `:core:design-foundation/color/BillboardColorScheme.kt` per `07-design-system.md` — badge bg `BillboardColor.HoloGreen`, badge fg `Color.Black`. Do NOT use raw hex literals in design-system code.

- [ ] **Step 2: Compile**

Run: `./gradlew :core:design-system:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add core/design-system/
git commit -m "$(cat <<'EOF'
feat(design-system): BillboardHeader optional collection badge slot

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Phase C — Home integration

### Task 13: `RankingList` long-press + collected group dot

**Files:**
- Modify: `feature/home/src/main/java/com/hancekim/billboard/.../RankingList.kt` (locate the row composable used in `HomeUi`)
- Modify: `feature/home/src/main/java/com/hancekim/billboard/home/HomeUi.kt` call site

- [ ] **Step 1: Add `collectedGroupColor: Color?` parameter and group-dot overlay**

Find the row composable. Add parameter:
```kotlin
collectedGroupColor: Color? = null,
```
Inside the row's thumbnail `Box`:
```kotlin
collectedGroupColor?.let {
    GroupDot(
        colorArgb = it.toArgb(),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 3.dp, y = (-3).dp),
    )
}
```

- [ ] **Step 2: Replace `clickable` with `combinedClickable(onClick, onLongClick)`**

```kotlin
modifier = Modifier
    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
```
Add `onLongClick: () -> Unit` parameter, propagate from `HomeUi`.

- [ ] **Step 3: Compile**

Run: `./gradlew :feature:home:compileProdDebugKotlin`
Expected: BUILD SUCCESSFUL (HomeUi call sites updated next task).

- [ ] **Step 4: Commit**

```bash
git add feature/home/
git commit -m "$(cat <<'EOF'
feat(home): RankingRow long-press handler and collected group dot

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 14: `HomeState` extensions + `OverlayCollectState`

**Files:**
- Modify: `feature/home/src/main/java/com/hancekim/billboard/home/HomeState.kt`
- Create: `feature/home/src/main/java/com/hancekim/billboard/home/component/OverlayCollectState.kt`

- [ ] **Step 1: Create `OverlayCollectState`**

```kotlin
package com.hancekim.billboard.home.component

sealed interface OverlayCollectState {
    data object Uncollected : OverlayCollectState
    data class Collected(val groupId: Long) : OverlayCollectState
}
```

- [ ] **Step 2: Extend `HomeState`**

Append the new fields to the `HomeState` data class (keep `eventSink` last):

```kotlin
val groups: ImmutableMap<Long, Group>,
val selectedGroupIdInOverlay: Long,
val collectedGroupColorByKey: ImmutableMap<String, Int>,
val collectionCount: Int,
val overlayState: OverlayCollectState,
val newGroupFormInOverlay: NewGroupFormState?,
```

Add `NewGroupFormState` as a shared `@Immutable data class` (move under `:feature:home/component` or a shared spot accessible to both presenters; for now duplicate the small data class in each feature to avoid module thrash):

```kotlin
@Immutable
data class NewGroupFormState(
    val name: String,
    val hex: String,
    val isDuplicate: Boolean,
    val isHexValid: Boolean,
)
```

- [ ] **Step 3: Extend `HomeEvent`**

```kotlin
data class OnLongPressItem(val item: Chart) : HomeEvent
data class OnSelectGroupInOverlay(val id: Long) : HomeEvent
data object OnCreateNewGroupClickInOverlay : HomeEvent
data class OnNewGroupNameChangeInOverlay(val name: String) : HomeEvent
data class OnNewGroupHexChangeInOverlay(val hex: String) : HomeEvent
data object OnSubmitNewGroupInOverlay : HomeEvent
data object OnCancelNewGroupInOverlay : HomeEvent
data object OnCommitOverlay : HomeEvent
data object OnCollectionHeaderClick : HomeEvent
```

- [ ] **Step 4: Compile (presenter will be red — fix in next task)**

Run: `./gradlew :feature:home:compileProdDebugKotlin`
Expected: errors will reference `HomePresenter` not yet handling new fields. Proceed.

- [ ] **Step 5: Commit (skip if build fully red; squash into Task 15)**

If the module still compiles, commit:
```bash
git add feature/home/
git commit -m "$(cat <<'EOF'
feat(home): state + event additions for group-aware overlay

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```
Otherwise carry these changes forward into Task 15's commit.

---

### Task 15: `HomePresenter` rewrite — combine flows, overlay dispatcher

**Files:**
- Modify: `feature/home/src/main/java/com/hancekim/billboard/home/HomePresenter.kt`

- [ ] **Step 1: Inject `GetGroupsFlowUseCase` and `AddGroupUseCase`**

Add to constructor:
```kotlin
private val getGroupsFlow: GetGroupsFlowUseCase,
private val addGroupUseCase: AddGroupUseCase,
```

- [ ] **Step 2: Collect groups + combine with collection**

Inside `present()`:
```kotlin
val groups by produceRetainedState<ImmutableList<Group>>(persistentListOf()) {
    getGroupsFlow().collect { value = it.toImmutableList() }
}
val groupsMap = remember(groups) { groups.associateBy { it.id }.toImmutableMap() }
val collection by produceRetainedState<ImmutableList<CollectedCard>>(persistentListOf()) {
    getCollectionFlow().collect { value = it.toImmutableList() }
}
val collectedGroupColorByKey = remember(collection, groupsMap) {
    collection.associate { card ->
        card.key to (groupsMap[card.groupId]?.colorArgb ?: 0)
    }.toImmutableMap()
}
val collectionCount = collection.size

var selectedGroupIdInOverlay by rememberRetained { mutableLongStateOf(Group.DEFAULT_ID) }
var newGroupFormInOverlay by rememberRetained { mutableStateOf<NewGroupFormState?>(null) }
val overlayState: OverlayCollectState = remember(collection, currentOverlayChart) {
    val chart = currentOverlayChart ?: return@remember OverlayCollectState.Uncollected
    val key = "${chart.title}::${chart.artist}"
    collection.firstOrNull { it.key == key }
        ?.let { OverlayCollectState.Collected(it.groupId) }
        ?: OverlayCollectState.Uncollected
}
```

(Where `currentOverlayChart` is the existing state that drives the overlay's `chart` param. If the existing presenter stores it differently, adapt — variable name is illustrative.)

- [ ] **Step 3: Wire new events**

```kotlin
is HomeEvent.OnLongPressItem -> {
    currentOverlayChart = event.item
    selectedGroupIdInOverlay = when (val s = overlayState) {
        is OverlayCollectState.Collected -> s.groupId
        OverlayCollectState.Uncollected -> selectedGroupIdInOverlay
    }
}
is HomeEvent.OnSelectGroupInOverlay -> { selectedGroupIdInOverlay = event.id }
HomeEvent.OnCreateNewGroupClickInOverlay -> {
    newGroupFormInOverlay = NewGroupFormState("", "", false, false)
}
HomeEvent.OnCancelNewGroupInOverlay -> { newGroupFormInOverlay = null }
is HomeEvent.OnNewGroupNameChangeInOverlay -> {
    val name = event.name
    val normalized = name.trim().lowercase()
    newGroupFormInOverlay = newGroupFormInOverlay?.copy(
        name = name,
        isDuplicate = groups.any { it.name.trim().lowercase() == normalized },
    )
}
is HomeEvent.OnNewGroupHexChangeInOverlay -> {
    newGroupFormInOverlay = newGroupFormInOverlay?.copy(
        hex = event.hex,
        isHexValid = HEX_REGEX.matches(event.hex),
    )
}
HomeEvent.OnSubmitNewGroupInOverlay -> {
    val form = newGroupFormInOverlay ?: return@launch
    val color = android.graphics.Color.parseColor(form.hex)
    scope.launch {
        addGroupUseCase(form.name, color)
            .onSuccess { newId ->
                selectedGroupIdInOverlay = newId
                newGroupFormInOverlay = null
            }
            .onFailure { Timber.e(it, "add group in overlay failed") }
    }
}
HomeEvent.OnCommitOverlay -> {
    val chart = currentOverlayChart ?: return
    val card = chart.toCollectedCard(groupId = selectedGroupIdInOverlay) // existing extension/mapper, ensure groupId carried
    when (val s = overlayState) {
        OverlayCollectState.Uncollected -> scope.launch {
            runCatching { addToCollectionUseCase(card) }
                .onFailure { Timber.e(it, "addToCollection failed") }
        }
        is OverlayCollectState.Collected -> {
            if (s.groupId == selectedGroupIdInOverlay) {
                scope.launch {
                    runCatching { removeFromCollectionUseCase(card.key) }
                        .onFailure { Timber.e(it, "removeFromCollection failed") }
                }
            } else {
                scope.launch {
                    runCatching { addToCollectionUseCase(card) } // REPLACE = move group
                        .onFailure { Timber.e(it, "moveGroup failed") }
                }
            }
        }
    }
    currentOverlayChart = null
}
HomeEvent.OnCollectionHeaderClick -> navigator.goTo(BillboardScreen.Collection)
```

Add a top-of-file constant: `private val HEX_REGEX = Regex("^#[0-9A-Fa-f]{6}$")`.

- [ ] **Step 4: Compile**

Run: `./gradlew :feature:home:compileProdDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add feature/home/
git commit -m "$(cat <<'EOF'
feat(home): HomePresenter group-aware overlay dispatcher

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 16: `CollectOverlay` rewrite — 3-state action button, group glow, inline form

**Files:**
- Modify: `feature/home/src/main/java/com/hancekim/billboard/home/component/CollectOverlay.kt`

- [ ] **Step 1: Replace function signature**

```kotlin
@Composable
fun CollectOverlay(
    visible: Boolean,
    chart: Chart?,
    overlayState: OverlayCollectState,
    groups: ImmutableList<Group>,
    selectedGroupId: Long,
    newGroupForm: NewGroupFormState?,
    modifier: Modifier = Modifier,
    onSelectGroup: (Long) -> Unit,
    onCreateNewGroupClick: () -> Unit,
    onNewGroupNameChange: (String) -> Unit,
    onNewGroupHexChange: (String) -> Unit,
    onSubmitNewGroup: () -> Unit,
    onCancelNewGroup: () -> Unit,
    onCommit: () -> Unit,
    onDismiss: () -> Unit,
)
```

- [ ] **Step 2: Replace 360.dp radial glow color source**

Drive the glow brush colors from `selectedGroupColor = groups.firstOrNull { it.id == selectedGroupId }?.colorArgb?.let(::Color) ?: Color.White`. Animate with `animateColorAsState`. Read inside `drawBehind` / `graphicsLayer` lambdas to keep on draw phase per `03-compose-state.md`.

- [ ] **Step 3: Replace dropdown + action button block**

```kotlin
if (newGroupForm != null) {
    NewGroupForm(
        form = newGroupForm,
        onNameChange = onNewGroupNameChange,
        onHexChange = onNewGroupHexChange,
        onSubmit = onSubmitNewGroup,
        onCancel = onCancelNewGroup,
    )
} else {
    GroupDropdown(
        groups = groups,
        selectedId = selectedGroupId,
        onSelect = onSelectGroup,
        onCreateNew = onCreateNewGroupClick,
    )
    Spacer(Modifier.height(16.dp))
    val selected = groups.firstOrNull { it.id == selectedGroupId } ?: return
    val (label, bgColor, borderColor) = when (overlayState) {
        OverlayCollectState.Uncollected ->
            Triple("ADD TO ${selected.name.uppercase()}", Color(selected.colorArgb), Color.Transparent)
        is OverlayCollectState.Collected ->
            if (overlayState.groupId == selectedGroupId)
                Triple("REMOVE FROM COLLECTION", Color.Transparent, Color.White)
            else
                Triple("MOVE TO ${selected.name.uppercase()}", Color(selected.colorArgb), Color.Transparent)
    }
    OverlayActionButton(label = label, bg = bgColor, border = borderColor, onClick = onCommit)
}
```

`OverlayActionButton` is a small local composable (Button with given bg/border/text color).

`NewGroupForm` will be created in Task 23. For now, since this is a foreward reference inside Home, declare a temporary `expect`-style stub: instead, **block Task 16 commit** until Task 23 is done, OR copy the `NewGroupForm` composable into `:feature:home/component/NewGroupForm.kt` first. **Recommended:** create `:feature:home/component/NewGroupForm.kt` now as a sibling to CollectOverlay (single-file copy is acceptable — feature modules don't share components per `07-design-system.md`).

- [ ] **Step 4: Add `:feature:home/component/NewGroupForm.kt`**

```kotlin
package com.hancekim.billboard.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun NewGroupForm(
    form: NewGroupFormState,
    onNameChange: (String) -> Unit,
    onHexChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(12.dp)) {
        BasicTextField(value = form.name, onValueChange = onNameChange,
            textStyle = BillboardTheme.typography.bodyMd().copy(color = BillboardTheme.colorScheme.textPrimary),
            modifier = Modifier.background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(6.dp)).padding(8.dp))
        if (form.isDuplicate) {
            Text("이미 같은 이름의 그룹이 있어요", color = BillboardTheme.colorScheme.error,
                style = BillboardTheme.typography.labelSm())
        }
        BasicTextField(value = form.hex, onValueChange = onHexChange,
            textStyle = BillboardTheme.typography.bodyMd().copy(color = BillboardTheme.colorScheme.textPrimary),
            modifier = Modifier.background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(6.dp)).padding(8.dp))
        Row {
            if (form.isHexValid) {
                androidx.compose.foundation.layout.Box(
                    Modifier.size(20.dp).background(Color(android.graphics.Color.parseColor(form.hex)), RoundedCornerShape(50)),
                )
            }
        }
        Row {
            TextButton(onClick = onCancel) { Text("CANCEL") }
            TextButton(
                onClick = onSubmit,
                enabled = !form.isDuplicate && form.isHexValid && form.name.trim().isNotEmpty(),
            ) { Text("ADD") }
        }
    }
}
```

- [ ] **Step 5: Compile**

Run: `./gradlew :feature:home:compileProdDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add feature/home/
git commit -m "$(cat <<'EOF'
feat(home): CollectOverlay group-aware buttons + inline NewGroupForm

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 17: `HomeUi` wiring — header badge, RankingRow params, overlay params

**Files:**
- Modify: `feature/home/src/main/java/com/hancekim/billboard/home/HomeUi.kt`

- [ ] **Step 1: Pass new params to `BillboardHeader`**

```kotlin
BillboardHeader(
    ...,
    collectionCount = state.collectionCount,
    onCollectionClick = { state.eventSink(HomeEvent.OnCollectionHeaderClick) },
)
```

- [ ] **Step 2: Pass per-row props to RankingRow callsite**

```kotlin
RankingRow(
    ...,
    collectedGroupColor = state.collectedGroupColorByKey[item.key]?.let(::Color),
    onLongClick = { state.eventSink(HomeEvent.OnLongPressItem(item)) },
)
```

- [ ] **Step 3: Pass new params to `CollectOverlay`**

```kotlin
CollectOverlay(
    visible = state.overlayChart != null,
    chart = state.overlayChart,
    overlayState = state.overlayState,
    groups = state.groups.values.toImmutableList(),
    selectedGroupId = state.selectedGroupIdInOverlay,
    newGroupForm = state.newGroupFormInOverlay,
    onSelectGroup = { state.eventSink(HomeEvent.OnSelectGroupInOverlay(it)) },
    onCreateNewGroupClick = { state.eventSink(HomeEvent.OnCreateNewGroupClickInOverlay) },
    onNewGroupNameChange = { state.eventSink(HomeEvent.OnNewGroupNameChangeInOverlay(it)) },
    onNewGroupHexChange = { state.eventSink(HomeEvent.OnNewGroupHexChangeInOverlay(it)) },
    onSubmitNewGroup = { state.eventSink(HomeEvent.OnSubmitNewGroupInOverlay) },
    onCancelNewGroup = { state.eventSink(HomeEvent.OnCancelNewGroupInOverlay) },
    onCommit = { state.eventSink(HomeEvent.OnCommitOverlay) },
    onDismiss = { state.eventSink(HomeEvent.OnDismissOverlay) }, // existing
)
```

- [ ] **Step 4: Build prod debug variant**

Run: `./gradlew :feature:home:assembleProdDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add feature/home/
git commit -m "$(cat <<'EOF'
feat(home): wire HomeUi header badge, RankingRow long-press, overlay groups

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Phase D — Collection screen

### Task 18: Delete `OrbitLayout` + `EmptySlot`

**Files:**
- Delete: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/OrbitLayout.kt`
- Delete: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/EmptySlot.kt`

- [ ] **Step 1: Delete + remove `OrbitLayout` usage from `CollectionUi.kt`**

```bash
git rm feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/OrbitLayout.kt
git rm feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/EmptySlot.kt
```
Edit `CollectionUi.kt` and delete the `OrbitLayout(...)` call (will be re-built in Task 24). Module will break compilation temporarily — fixed by end of Phase D.

- [ ] **Step 2: Commit (intentionally breaking — squash with later task if not preferred)**

```bash
git commit -m "$(cat <<'EOF'
refactor(collection): remove OrbitLayout/EmptySlot in favor of new layout

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 19: Add `:core:player` dependency to `:feature:collection`

**Files:**
- Modify: `feature/collection/build.gradle.kts`

- [ ] **Step 1: Add dependency**

Inside `dependencies { ... }`:
```kotlin
implementation(projects.core.player)
```

- [ ] **Step 2: Sync + compile**

Run: `./gradlew :feature:collection:dependencies --configuration prodDebugRuntimeClasspath | grep player`
Expected: `:core:player` listed.

- [ ] **Step 3: Commit**

```bash
git commit -am "$(cat <<'EOF'
chore(collection): depend on :core:player

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 20: `NowPlayingPlayer` component

**Files:**
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/NowPlayingPlayer.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.player.PlayerControllerButtons
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.core.player.YoutubePlayer

@Composable
fun NowPlayingPlayer(
    playerState: PlayerState?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center,
    ) {
        if (playerState != null) {
            YoutubePlayer(state = playerState)
            PlayerControllerButtons(
                state = playerState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("NOTHING PLAYING", style = BillboardTheme.typography.labelMd(),
                    color = BillboardTheme.colorScheme.textSecondary)
            }
        }
    }
}
```

(Match the actual `YoutubePlayer` / `PlayerControllerButtons` signatures from `:core:player` — `core/player/src/main/java/.../YoutubePlayer.kt` uses `state: PlayerState`; verify and adapt if different.)

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :feature:collection:compileProdDebugKotlin
git add feature/collection/
git commit -m "feat(collection): NowPlayingPlayer wrapping YoutubePlayer

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 21: `MiniRail` component

**Files:**
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/MiniRail.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.designfoundation.color.BillboardColor
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MiniRail(
    cards: ImmutableList<CollectedCard>,
    activeKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(cards, key = { it.key }) { card ->
            val isActive = card.key == activeKey
            AsyncImage(
                model = card.albumArtUrl,
                contentDescription = "${card.title} by ${card.artist}",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { alpha = if (isActive) 1f else 0.5f }
                    .background(BillboardTheme.colorScheme.bgCard, RoundedCornerShape(8.dp))
                    .border(
                        width = if (isActive) 1.dp else 0.dp,
                        color = if (isActive) BillboardColor.HoloBlue else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .noRippleClickable { onSelect(card.key) },
            )
        }
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :feature:collection:compileProdDebugKotlin
git add feature/collection/
git commit -m "feat(collection): MiniRail horizontal card list

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 22: `EmptyGroupPlaceholder` component

**Files:**
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/EmptyGroupPlaceholder.kt`

- [ ] **Step 1: Implement**

```kotlin
package com.hancekim.billboard.feature.collection.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.designsystem.BillboardTheme

@Composable
fun EmptyGroupPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("이 그룹에 카드가 없어요",
            style = BillboardTheme.typography.bodyMd(),
            color = BillboardTheme.colorScheme.textPrimary)
        Text("홈에서 곡을 길게 눌러 추가하세요",
            style = BillboardTheme.typography.labelSm(),
            color = BillboardTheme.colorScheme.textSecondary)
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :feature:collection:compileProdDebugKotlin
git add feature/collection/
git commit -m "feat(collection): EmptyGroupPlaceholder text-only state

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 23: Collection-side `NewGroupForm` + `NewGroupFormState`

**Files:**
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/NewGroupForm.kt` (copy of Home's, single file to keep boundary clean)
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/NewGroupFormState.kt`

- [ ] **Step 1: Mirror `NewGroupFormState`**

```kotlin
package com.hancekim.billboard.feature.collection.component

import androidx.compose.runtime.Immutable

@Immutable
data class NewGroupFormState(
    val name: String,
    val hex: String,
    val isDuplicate: Boolean,
    val isHexValid: Boolean,
)
```

- [ ] **Step 2: Reuse the `NewGroupForm` composable from Task 16**

Copy it verbatim into `:feature:collection/component/NewGroupForm.kt` — adjust the package and imports.

- [ ] **Step 3: Compile + commit**

```bash
./gradlew :feature:collection:compileProdDebugKotlin
git add feature/collection/
git commit -m "feat(collection): NewGroupForm + state for sidebar

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 24: `GroupSidebar` component

**Files:**
- Create: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/component/GroupSidebar.kt`

- [ ] **Step 1: Implement (closed peek + open panel + delete confirm)**

```kotlin
package com.hancekim.billboard.feature.collection.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.designfoundation.modifier.noRippleClickable
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.group.GroupDot
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

private val SidebarEasing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

@Composable
fun GroupSidebar(
    isOpen: Boolean,
    groups: ImmutableList<Group>,
    currentGroupId: Long,
    countsByGroupId: ImmutableMap<Long, Int>,
    pendingDeleteGroupId: Long?,
    newGroupForm: NewGroupFormState?,
    onToggle: (Boolean) -> Unit,
    onSelectGroup: (Long) -> Unit,
    onRequestDelete: (Long) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onNewGroupClick: () -> Unit,
    onCancelNewGroup: () -> Unit,
    onNewGroupNameChange: (String) -> Unit,
    onNewGroupHexChange: (String) -> Unit,
    onSubmitNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) {
        // Closed peek: 14dp vertical color bar with rotated group label
        val current = groups.firstOrNull { it.id == currentGroupId } ?: return
        Box(
            modifier = modifier
                .width(14.dp)
                .fillMaxHeight()
                .noRippleClickable { onToggle(true) },
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp)
                    .background(Color(current.colorArgb), RoundedCornerShape(2.dp)),
            )
        }
        return
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(animationSpec = tween(280, easing = SidebarEasing)) { it },
        exit = slideOutHorizontally(animationSpec = tween(280, easing = SidebarEasing)) { it },
    ) {
        Column(
            modifier = modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(BillboardTheme.colorScheme.bgCard)
                .border(1.dp, Color.White.copy(alpha = 0.08f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("GROUPS", style = BillboardTheme.typography.labelSm())
            groups.forEach { g ->
                Row(
                    modifier = Modifier
                        .background(
                            if (g.id == currentGroupId) Color(g.colorArgb).copy(alpha = 0.12f)
                            else Color.Transparent,
                            RoundedCornerShape(6.dp),
                        )
                        .noRippleClickable { onSelectGroup(g.id) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GroupDot(colorArgb = g.colorArgb)
                    Text("${g.name}  (${countsByGroupId[g.id] ?: 0})",
                        style = BillboardTheme.typography.bodyMd(),
                        modifier = Modifier.weight(1f))
                    if (g.id != Group.DEFAULT_ID) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "${g.name} 삭제",
                            modifier = Modifier
                                .size(20.dp)
                                .noRippleClickable { onRequestDelete(g.id) },
                            tint = BillboardTheme.colorScheme.error,
                        )
                    }
                }
                if (pendingDeleteGroupId == g.id) {
                    Column(
                        modifier = Modifier
                            .background(BillboardTheme.colorScheme.error.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                    ) {
                        Text("${countsByGroupId[g.id] ?: 0}개 카드가 함께 삭제됩니다",
                            style = BillboardTheme.typography.labelSm(),
                            color = BillboardTheme.colorScheme.error)
                        Row {
                            TextButton(onClick = onCancelDelete) { Text("CANCEL") }
                            TextButton(onClick = onConfirmDelete) {
                                Text("DELETE", color = BillboardTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (newGroupForm != null) {
                NewGroupForm(
                    form = newGroupForm,
                    onNameChange = onNewGroupNameChange,
                    onHexChange = onNewGroupHexChange,
                    onSubmit = onSubmitNewGroup,
                    onCancel = onCancelNewGroup,
                )
            } else {
                TextButton(onClick = onNewGroupClick) { Text("+ NEW GROUP") }
            }
        }
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
./gradlew :feature:collection:compileProdDebugKotlin
git add feature/collection/
git commit -m "feat(collection): GroupSidebar with delete confirm + new group form

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

### Task 25: `CollectionState` + `CollectionEvent` rewrite

**Files:**
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CollectionState.kt`

- [ ] **Step 1: Replace state + events**

```kotlin
package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Immutable
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.feature.collection.component.NewGroupFormState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class CollectionState(
    val groups: ImmutableList<Group>,
    val currentGroupId: Long,
    val cardsInCurrentGroup: ImmutableList<CollectedCard>,
    val countsByGroupId: ImmutableMap<Long, Int>,
    val nowPlayingKey: String?,
    val playerState: PlayerState?,
    val sidebarOpen: Boolean,
    val newGroupForm: NewGroupFormState?,
    val pendingDeleteGroupId: Long?,
    val eventSink: (CollectionEvent) -> Unit,
) : CircuitUiState

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

- [ ] **Step 2: Commit (will not yet compile until presenter updates — chain into next task)**

Skip commit here; bundle with Task 26.

---

### Task 26: `CollectionPresenter` rewrite (TDD)

**Files:**
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CollectionPresenter.kt`
- Modify: `feature/collection/src/androidTest/java/com/hancekim/billboard/feature/collection/CollectionPresenterTest.kt`

- [ ] **Step 1: Write failing test cases**

Replace the existing test file:
```kotlin
package com.hancekim.billboard.feature.collection

import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.datatest.fixture.fakeCollectedCard
import com.hancekim.billboard.core.datatest.fixture.fakeGroup
import com.hancekim.billboard.core.datatest.repository.FakeCollectionRepository
import com.hancekim.billboard.core.datatest.repository.FakeGroupRepository
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.RemoveGroupUseCase
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionPresenterTest {

    private fun buildPresenter(
        groupRepo: FakeGroupRepository = FakeGroupRepository(),
        collectionRepo: FakeCollectionRepository = FakeCollectionRepository(),
    ): CollectionPresenter = CollectionPresenter(
        navigator = FakeNavigator(initialScreen = com.hancekim.billboard.core.circuit.BillboardScreen.Collection),
        getGroupsFlow = GetGroupsFlowUseCase(groupRepo),
        getCollectionFlow = GetCollectionFlowUseCase(collectionRepo),
        addGroupUseCase = AddGroupUseCase(groupRepo),
        removeGroupUseCase = RemoveGroupUseCase(groupRepo),
        // resolveYoutubeVideoId stub — pass real Home use case or fake
    )

    @Test fun `초기 currentGroupId 는 DEFAULT_ID`() = runTest {
        buildPresenter().test {
            assertEquals(Group.DEFAULT_ID, awaitItem().currentGroupId)
        }
    }

    @Test fun `OnSelectGroup 으로 currentGroupId 변경되고 sidebar 닫힘`() = runTest {
        val groupRepo = FakeGroupRepository().apply { add("Workout", 0xFFFFB400.toInt()) }
        buildPresenter(groupRepo = groupRepo).test {
            val initial = awaitItem()
            initial.eventSink(CollectionEvent.OnSidebarToggle(true))
            awaitItem()
            initial.eventSink(CollectionEvent.OnSelectGroup(2L))
            val after = awaitItem()
            assertEquals(2L, after.currentGroupId)
            assertEquals(false, after.sidebarOpen)
        }
    }

    @Test fun `빈 그룹이면 cards 빈 리스트, nowPlayingKey null`() = runTest {
        buildPresenter().test {
            val s = awaitItem()
            assertTrue(s.cardsInCurrentGroup.isEmpty())
            assertNull(s.nowPlayingKey)
        }
    }

    @Test fun `OnRequestDeleteGroup 카드 0개면 즉시 삭제`() = runTest {
        val groupRepo = FakeGroupRepository().apply { add("Workout", 0) }
        buildPresenter(groupRepo = groupRepo).test {
            val s = awaitItem()
            s.eventSink(CollectionEvent.OnRequestDeleteGroup(2L))
            val after = awaitItem()
            assertTrue(after.groups.none { it.id == 2L })
        }
    }

    @Test fun `Default 삭제 시도는 무시`() = runTest {
        buildPresenter().test {
            val s = awaitItem()
            s.eventSink(CollectionEvent.OnRequestDeleteGroup(Group.DEFAULT_ID))
            expectNoEvents() // Timber.e 로만 처리, state 변화 없음
        }
    }
}
```

(Adapt constructor injection to whatever matches `:core:domain` use case constructors exactly.)

- [ ] **Step 2: Run tests — they fail (constructor / signature mismatch)**

Run: `./gradlew :feature:collection:connectedDemoDebugAndroidTest`
Expected: COMPILE FAIL or test failure due to old presenter signature.

- [ ] **Step 3: Rewrite `CollectionPresenter`**

```kotlin
package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.data.model.Group
import com.hancekim.billboard.core.domain.AddGroupUseCase
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.GetGroupsFlowUseCase
import com.hancekim.billboard.core.domain.RemoveGroupUseCase
import com.hancekim.billboard.core.player.PlayerState
import com.hancekim.billboard.feature.collection.component.NewGroupFormState
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

private val HEX_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

class CollectionPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getGroupsFlow: GetGroupsFlowUseCase,
    private val getCollectionFlow: GetCollectionFlowUseCase,
    private val addGroupUseCase: AddGroupUseCase,
    private val removeGroupUseCase: RemoveGroupUseCase,
    // resolveYoutubeVideoIdUseCase 의 실제 명칭으로 교체
) : Presenter<CollectionState> {

    @androidx.compose.runtime.Composable
    override fun present(): CollectionState {
        val scope = rememberRetained<CoroutineScope> { kotlinx.coroutines.MainScope() }
        val groups by produceRetainedState(persistentListOf<Group>()) {
            getGroupsFlow().collect { value = it.toImmutableList() }
        }
        val allCards by produceRetainedState(persistentListOf<CollectedCard>()) {
            getCollectionFlow().collect { value = it.toImmutableList() }
        }
        var currentGroupId by rememberRetained { mutableLongStateOf(Group.DEFAULT_ID) }
        val cardsInCurrentGroup = remember(allCards, currentGroupId) {
            allCards.filter { it.groupId == currentGroupId }.toImmutableList()
        }
        val countsByGroupId = remember(allCards) {
            allCards.groupingBy { it.groupId }.eachCount().toImmutableMap()
        }
        var nowPlayingKey by rememberRetained { mutableStateOf<String?>(null) }
        var sidebarOpen by rememberRetained { mutableStateOf(false) }
        var newGroupForm by rememberRetained { mutableStateOf<NewGroupFormState?>(null) }
        var pendingDeleteGroupId by rememberRetained { mutableStateOf<Long?>(null) }

        LaunchedEffect(currentGroupId, cardsInCurrentGroup.firstOrNull()?.key) {
            if (nowPlayingKey == null || cardsInCurrentGroup.none { it.key == nowPlayingKey }) {
                nowPlayingKey = cardsInCurrentGroup.firstOrNull()?.key
            }
        }

        // playerState 는 컨텍스트가 필요하므로 LocalContext 로 가져옴 (Home 패턴 그대로)
        val context = androidx.compose.ui.platform.LocalContext.current
        val playerState = rememberRetained { PlayerState(context) }

        LaunchedEffect(nowPlayingKey) {
            val key = nowPlayingKey ?: return@LaunchedEffect
            val card = allCards.firstOrNull { it.key == key } ?: return@LaunchedEffect
            runCatching {
                // resolveYoutubeVideoIdUseCase(card.title, card.artist) — 실제 구현으로 교체
                ""
            }
                .onSuccess { id -> if (id.isNotEmpty()) playerState.loadVideo(id, 0f) }
                .onFailure { Timber.e(it, "videoId resolve failed key=$key") }
        }

        return CollectionState(
            groups = groups,
            currentGroupId = currentGroupId,
            cardsInCurrentGroup = cardsInCurrentGroup,
            countsByGroupId = countsByGroupId,
            nowPlayingKey = nowPlayingKey,
            playerState = playerState,
            sidebarOpen = sidebarOpen,
            newGroupForm = newGroupForm,
            pendingDeleteGroupId = pendingDeleteGroupId,
            eventSink = { event ->
                when (event) {
                    CollectionEvent.OnBackClick -> navigator.pop()
                    is CollectionEvent.OnSelectCard -> { nowPlayingKey = event.key }
                    CollectionEvent.OnInspectClick ->
                        nowPlayingKey?.let { navigator.goTo(BillboardScreen.CardDetail(it)) }
                    is CollectionEvent.OnSidebarToggle -> sidebarOpen = event.open
                    is CollectionEvent.OnSelectGroup -> {
                        currentGroupId = event.id
                        sidebarOpen = false
                    }
                    is CollectionEvent.OnRequestDeleteGroup -> {
                        if (event.id == Group.DEFAULT_ID) {
                            Timber.e("attempted to delete Default group")
                        } else if ((countsByGroupId[event.id] ?: 0) == 0) {
                            scope.launch {
                                runCatching { removeGroupUseCase(event.id) }
                                    .onFailure { Timber.e(it, "remove group failed") }
                            }
                        } else {
                            pendingDeleteGroupId = event.id
                        }
                    }
                    CollectionEvent.OnConfirmDeleteGroup -> {
                        val id = pendingDeleteGroupId ?: return@CollectionState
                        scope.launch {
                            runCatching { removeGroupUseCase(id) }
                                .onSuccess {
                                    if (currentGroupId == id) currentGroupId = Group.DEFAULT_ID
                                    pendingDeleteGroupId = null
                                }
                                .onFailure { Timber.e(it, "confirm delete failed") }
                        }
                    }
                    CollectionEvent.OnCancelDeleteGroup -> pendingDeleteGroupId = null
                    CollectionEvent.OnNewGroupClick ->
                        newGroupForm = NewGroupFormState("", "", false, false)
                    CollectionEvent.OnCancelNewGroup -> newGroupForm = null
                    is CollectionEvent.OnNewGroupNameChange -> {
                        val normalized = event.name.trim().lowercase()
                        newGroupForm = newGroupForm?.copy(
                            name = event.name,
                            isDuplicate = groups.any { it.name.trim().lowercase() == normalized },
                        )
                    }
                    is CollectionEvent.OnNewGroupHexChange ->
                        newGroupForm = newGroupForm?.copy(
                            hex = event.hex,
                            isHexValid = HEX_REGEX.matches(event.hex),
                        )
                    CollectionEvent.OnSubmitNewGroup -> {
                        val form = newGroupForm ?: return@CollectionState
                        val color = android.graphics.Color.parseColor(form.hex)
                        scope.launch {
                            addGroupUseCase(form.name, color)
                                .onSuccess { newId ->
                                    currentGroupId = newId
                                    newGroupForm = null
                                    sidebarOpen = false
                                }
                                .onFailure { Timber.e(it, "addGroup failed") }
                        }
                    }
                }
            },
        )
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
    fun interface Factory {
        fun create(navigator: Navigator): CollectionPresenter
    }
}
```

(`return@CollectionState` is illustrative — actual non-local return inside lambda uses `return@eventSink` or guards via `?.let { … }`. Use whichever compiles with the target Kotlin version.)

- [ ] **Step 4: Re-run tests**

Run: `./gradlew :feature:collection:connectedDemoDebugAndroidTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add feature/collection/
git commit -m "$(cat <<'EOF'
feat(collection): rewrite Presenter + State for group-aware UI

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 27: `CollectionUi` rewrite — wire all components

**Files:**
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CollectionUi.kt`

- [ ] **Step 1: Replace body**

```kotlin
@CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
@Composable
fun CollectionUi(state: CollectionState, modifier: Modifier = Modifier) {
    val colorScheme = BillboardTheme.colorScheme
    BackHandler { state.eventSink(CollectionEvent.OnBackClick) }

    Scaffold(
        modifier = modifier,
        containerColor = colorScheme.bgApp,
        topBar = {
            BillboardHeader(
                title = "COLLECTION",
                isLogoVisible = false,
                leadingIcon = BillboardIcons.ArrowBack,
                trailingIcon = null,
                onLeadingIconClick = { state.eventSink(CollectionEvent.OnBackClick) },
            )
        },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.fillMaxSize()) {
                val currentGroup = state.groups.firstOrNull { it.id == state.currentGroupId }
                Text(
                    text = "${state.cardsInCurrentGroup.size} IN ${currentGroup?.name?.uppercase() ?: "—"}",
                    style = BillboardTheme.typography.labelSm(),
                    color = currentGroup?.colorArgb?.let { Color(it) } ?: colorScheme.textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (state.cardsInCurrentGroup.isEmpty()) {
                    NowPlayingPlayer(playerState = null)
                    Spacer(Modifier.weight(1f))
                    EmptyGroupPlaceholder()
                } else {
                    val currentCard = state.cardsInCurrentGroup.firstOrNull { it.key == state.nowPlayingKey }
                        ?: state.cardsInCurrentGroup.first()
                    NowPlayingPlayer(playerState = state.playerState)
                    Spacer(Modifier.height(8.dp))
                    currentGroup?.let { GroupChip(it, Modifier.padding(horizontal = 16.dp)) }
                    Text(currentCard.title, style = BillboardTheme.typography.titleLg(),
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Text(currentCard.artist, style = BillboardTheme.typography.bodyMd(),
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.weight(1f))
                    Text("${state.cardsInCurrentGroup.size} CARDS",
                        style = BillboardTheme.typography.labelSm(),
                        modifier = Modifier.padding(horizontal = 16.dp))
                    MiniRail(
                        cards = state.cardsInCurrentGroup,
                        activeKey = state.nowPlayingKey,
                        onSelect = { state.eventSink(CollectionEvent.OnSelectCard(it)) },
                    )
                }
            }
            if (state.nowPlayingKey != null) {
                Text("INSPECT", style = BillboardTheme.typography.labelSm(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 72.dp, end = 16.dp)
                        .noRippleClickable { state.eventSink(CollectionEvent.OnInspectClick) }
                        .semantics { role = Role.Button; contentDescription = "카드 상세 보기" })
            }
            GroupSidebar(
                modifier = Modifier.align(Alignment.CenterEnd),
                isOpen = state.sidebarOpen,
                groups = state.groups,
                currentGroupId = state.currentGroupId,
                countsByGroupId = state.countsByGroupId,
                pendingDeleteGroupId = state.pendingDeleteGroupId,
                newGroupForm = state.newGroupForm,
                onToggle = { state.eventSink(CollectionEvent.OnSidebarToggle(it)) },
                onSelectGroup = { state.eventSink(CollectionEvent.OnSelectGroup(it)) },
                onRequestDelete = { state.eventSink(CollectionEvent.OnRequestDeleteGroup(it)) },
                onConfirmDelete = { state.eventSink(CollectionEvent.OnConfirmDeleteGroup) },
                onCancelDelete = { state.eventSink(CollectionEvent.OnCancelDeleteGroup) },
                onNewGroupClick = { state.eventSink(CollectionEvent.OnNewGroupClick) },
                onCancelNewGroup = { state.eventSink(CollectionEvent.OnCancelNewGroup) },
                onNewGroupNameChange = { state.eventSink(CollectionEvent.OnNewGroupNameChange(it)) },
                onNewGroupHexChange = { state.eventSink(CollectionEvent.OnNewGroupHexChange(it)) },
                onSubmitNewGroup = { state.eventSink(CollectionEvent.OnSubmitNewGroup) },
            )
        }
    }
}
```

- [ ] **Step 2: Compile assembleDemoDebug + assembleProdDebug**

Run: `./gradlew :feature:collection:assembleDemoDebug :feature:collection:assembleProdDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add feature/collection/
git commit -m "$(cat <<'EOF'
feat(collection): wire CollectionUi to NowPlayingPlayer + MiniRail + Sidebar

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 28: `CardDetailUi` group chip row

**Files:**
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CardDetailUi.kt`
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CardDetailState.kt`
- Modify: `feature/collection/src/main/java/com/hancekim/billboard/feature/collection/CardDetailPresenter.kt`

- [ ] **Step 1: Add `group: Group?` to `CardDetailState`**

Append `val group: Group?` (above `eventSink`).

- [ ] **Step 2: Resolve in `CardDetailPresenter`**

Combine `GetCollectedCardFlowUseCase` with `GetGroupsFlowUseCase` (inject) to resolve `card.groupId` → `Group`.

- [ ] **Step 3: Render `GroupChip(group)` above title in `CardDetailUi`**

Add `state.group?.let { GroupChip(it) }` immediately above the existing title `Text`.

- [ ] **Step 4: Test + commit**

```bash
./gradlew :feature:collection:connectedDemoDebugAndroidTest
git add feature/collection/
git commit -m "feat(collection): show GroupChip in CardDetail

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Phase E — Final integration

### Task 29: Full app build + lint + Baseline Profile manifest

**Files:** none (verification only).

- [ ] **Step 1: Assemble both flavors**

Run: `./gradlew assembleProdDebug assembleDemoDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run unit tests across modules**

Run: `./gradlew :core:domain:test :core:data-impl:test`
Expected: PASS.

- [ ] **Step 3: Run instrumented tests on demo flavor**

Run: `./gradlew :feature:home:connectedDemoDebugAndroidTest :feature:collection:connectedDemoDebugAndroidTest`
Expected: PASS.

- [ ] **Step 4: Lint**

Run: `./gradlew lintDemoDebug`
Expected: 0 errors.

- [ ] **Step 5: Smoke test on emulator manually**

Install demo build, verify:
- App boots with Default group seeded.
- Long-press a Hot 100 row → CollectOverlay opens with Default selected.
- Add to Default → mini-dot appears on row + collection badge increments.
- Open Collection → YouTube player loads first card, mini-rail visible.
- Sidebar peek → open → "+ NEW GROUP" → form → ADD → new group selected, sidebar closed, empty placeholder visible.
- Long-press the same row again → "MOVE TO {new group}" → confirm → row dot color changes, card appears in new group.
- Delete new group with cards → confirm → cards gone, currentGroup reset to Default.

- [ ] **Step 6: Commit any final tweaks**

```bash
git status
# fix anything trivial that turned up
git commit -am "$(cat <<'EOF'
chore: post-integration polish

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Coverage Map (Self-Review)

| Spec Section | Implementing Task(s) |
|---|---|
| §3 Data Model | T1, T2, T3 |
| §3 Migration / Default seed | T4 |
| §4 Domain UseCases + ValidationError | T6 |
| §4 Fakes | T7 |
| §5.1 Shared UI (Dot/Chip/Dropdown) | T8, T9, T10, T11 |
| §5.2 BillboardHeader badge | T12 |
| §5.3 RankingRow | T13 |
| §5.4 CollectOverlay rewrite | T16 |
| §5.5 NowPlayingPlayer | T20 |
| §5.5 MiniRail | T21 |
| §5.5 EmptyGroupPlaceholder | T22 |
| §5.5 NewGroupForm | T16 (home) + T23 (collection) |
| §5.5 GroupSidebar | T24 |
| §5.5 CardDetail GroupChip | T28 |
| §6.1 CollectionState | T25 |
| §6.2 CollectionPresenter | T26 |
| §6.3 HomeState | T14 |
| §6.4 HomePresenter | T15, T17 |
| §6.5 Name de-duplication | T6, T15, T26 |
| §7 Tests | T6, T7, T26, T28, T29 |
| §8 OverlayCollectState | T14 |
| §8 Hex parsing | T16, T26 |
| §8 Subtitle on Collection | T27 |
