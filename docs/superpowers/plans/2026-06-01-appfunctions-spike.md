# AppFunctions Spike Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose one `@AppFunction` (`getCurrentHot100TopSong`) from the Billboard app so the Gemini app on a Galaxy S25 can invoke it (including from a cold-started process) and receive the live Hot 100 #1 song from our existing Retrofit pipeline.

**Architecture:** Add `androidx.appfunctions` (alpha09) wiring to `:app` only. `BillboardApplication` implements `AppFunctionConfiguration.Provider` and surfaces a `BillboardFunctions` class that injects the existing `GetBillboardHot100UseCase`. KSP code-gen handles manifest/schema registration. No new modules.

**Tech Stack:** Kotlin, Hilt 2.58, KSP 2.3.4, `androidx.appfunctions:*:1.0.0-alpha09`, existing `:core:domain` UseCase.

**Spec:** `docs/superpowers/specs/2026-06-01-appfunctions-spike-design.md`

**Branch:** create `feature/appfunctions-spike` off `develop` before Task 1.

---

## File Map

| File | Action | Purpose |
|---|---|---|
| `gradle/libs.versions.toml` | Modify | Add `appfunctions` version + 3 library aliases, bump `minSdk` 32→36 |
| `app/build.gradle.kts` | Modify | Add 3 dependencies, add KSP arg block |
| `app/src/main/java/com/hancekim/billboard/appfunctions/BillboardFunctions.kt` | Create | `@AppFunction` class with one suspend fun |
| `app/src/test/java/com/hancekim/billboard/appfunctions/BillboardFunctionsTest.kt` | Create | One JVM unit test verifying rank-1 mapping |
| `app/src/main/java/com/hancekim/billboard/BillboardApplication.kt` | Modify | Implement `AppFunctionConfiguration.Provider` |

No changes to `:core:domain`, `:core:data`, `:core:data-impl`, `:core:data-source`, any `:feature:*` module, or any convention plugin.

---

## Task 0: Create branch and confirm clean tree

**Files:** none

- [ ] **Step 1: Verify on develop and clean**

```bash
git branch --show-current
git status --porcelain
```

Expected: branch = `develop`, no output from `git status`.

- [ ] **Step 2: Create spike branch**

```bash
git checkout -b feature/appfunctions-spike
```

Expected: `Switched to a new branch 'feature/appfunctions-spike'`

---

## Task 1: Version catalog — add AppFunctions libraries and bump minSdk

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Bump minSdk**

In `gradle/libs.versions.toml`, change line 8:

```toml
minSdk = "36"
```

(was `"32"`)

- [ ] **Step 2: Add appfunctions version**

Insert after the `androidxRoom3 = "3.0.0-alpha04"` line (line 25):

```toml
androidxAppFunctions = "1.0.0-alpha09"
```

- [ ] **Step 3: Add three library entries**

Insert after the `androidx-room3-gradle-plugin = ...` line (around line 148), as a new block:

```toml
androidx-appfunctions = { group = "androidx.appfunctions", name = "appfunctions", version.ref = "androidxAppFunctions" }
androidx-appfunctions-service = { group = "androidx.appfunctions", name = "appfunctions-service", version.ref = "androidxAppFunctions" }
androidx-appfunctions-compiler = { group = "androidx.appfunctions", name = "appfunctions-compiler", version.ref = "androidxAppFunctions" }
```

- [ ] **Step 4: Verify gradle sync via build**

```bash
./gradlew help -q
```

Expected: completes without error (catalog is syntactically valid).

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): add androidx.appfunctions 1.0.0-alpha09 + bump minSdk to 36 for spike"
```

---

## Task 2: Wire `:app` dependencies and KSP arg

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add KSP plugin invocation and arg block**

In `app/build.gradle.kts`, insert a `ksp { ... }` block at the top level (between `android { ... }` and `dependencies { ... }`, around line 35):

```kotlin
ksp {
    // appfunctions-compiler 가 여러 모듈에 분산되어 있어도 :app 에서 한 번 모아 schema 생성.
    arg("appfunctions:aggregateAppFunctions", "true")
}
```

Note: KSP plugin is already applied transitively via `billboard.android.hilt` convention. If gradle sync errors on missing KSP, add `alias(libs.plugins.ksp)` to the `plugins { }` block.

- [ ] **Step 2: Add three dependencies**

In the `dependencies { }` block (after `implementation(libs.androidx.core.splash)` at line 43), insert:

```kotlin
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    ksp(libs.androidx.appfunctions.compiler)
```

- [ ] **Step 3: Verify gradle sync**

```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath -q 2>&1 | grep -i "appfunctions"
```

Expected: three `androidx.appfunctions:*:1.0.0-alpha09` lines.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts
git commit -m "build(app): add androidx.appfunctions deps + aggregateAppFunctions KSP arg"
```

---

## Task 3: BillboardFunctions class with one @AppFunction (TDD)

**Files:**
- Create: `app/src/main/java/com/hancekim/billboard/appfunctions/BillboardFunctions.kt`
- Create: `app/src/test/java/com/hancekim/billboard/appfunctions/BillboardFunctionsTest.kt`

- [ ] **Step 1: Add test dependencies to `:app` if missing**

Check `app/build.gradle.kts` for `testImplementation` lines. If MockK / coroutines-test are not present, append to `dependencies { }`:

```kotlin
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
```

(Skip this step if any of the three are already present.)

- [ ] **Step 2: Write the failing test**

Create `app/src/test/java/com/hancekim/billboard/appfunctions/BillboardFunctionsTest.kt`:

```kotlin
package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import com.hancekim.billboard.core.domain.model.Chart
import com.hancekim.billboard.core.domain.model.ChartOverview
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class BillboardFunctionsTest {

    private val useCase: GetBillboardHot100UseCase = mockk()
    private val context: AppFunctionContext = mockk(relaxed = true)
    private val sut = BillboardFunctions(useCase)

    @Test
    fun `getCurrentHot100TopSong - rank 1 entry 가 TopSong 으로 매핑된다`() = runTest {
        coEvery { useCase() } returns ChartOverview(
            chartList = listOf(
                Chart(rank = 2, title = "Second", artist = "B"),
                Chart(rank = 1, title = "Espresso", artist = "Sabrina Carpenter"),
                Chart(rank = 3, title = "Third", artist = "C"),
            ),
        )

        val result = sut.getCurrentHot100TopSong(context)

        assertEquals("Espresso", result.title)
        assertEquals("Sabrina Carpenter", result.artist)
        assertEquals(1, result.rank)
    }

    @Test
    fun `getCurrentHot100TopSong - rank 1 이 없으면 AppFunctionElementNotFoundException`() = runTest {
        coEvery { useCase() } returns ChartOverview(
            chartList = listOf(Chart(rank = 2, title = "Only Second", artist = "X")),
        )

        assertFailsWith<AppFunctionElementNotFoundException> {
            sut.getCurrentHot100TopSong(context)
        }
    }
}
```

- [ ] **Step 3: Run test to verify it fails (compile error — class missing)**

```bash
./gradlew :app:testProdDebugUnitTest --tests "com.hancekim.billboard.appfunctions.BillboardFunctionsTest" 2>&1 | tail -30
```

Expected: compile error like `Unresolved reference: BillboardFunctions`.

- [ ] **Step 4: Create `BillboardFunctions`**

Create `app/src/main/java/com/hancekim/billboard/appfunctions/BillboardFunctions.kt`:

```kotlin
package com.hancekim.billboard.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.hancekim.billboard.core.domain.GetBillboardHot100UseCase
import javax.inject.Inject

// Gemini 앱이 콜드부팅으로 우리 앱을 깨워 호출할 때 진입점.
// Hilt 가 GetBillboardHot100UseCase 를 주입 → Retrofit 으로 실시간 차트 가져옴.
class BillboardFunctions @Inject constructor(
    private val getBillboardHot100UseCase: GetBillboardHot100UseCase,
) {

    /** 현재 빌보드 Hot 100 1위 곡. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class TopSong(
        /** Song title. */
        val title: String,
        /** Artist name. */
        val artist: String,
        /** Chart rank (always 1 for this function). */
        val rank: Int,
    )

    /**
     * Returns the current #1 song on Billboard Hot 100.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCurrentHot100TopSong(context: AppFunctionContext): TopSong {
        val overview = getBillboardHot100UseCase()
        val top = overview.chartList.firstOrNull { it.rank == 1 }
            ?: throw AppFunctionElementNotFoundException("Hot 100 has no rank-1 entry")
        return TopSong(title = top.title, artist = top.artist, rank = 1)
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
./gradlew :app:testProdDebugUnitTest --tests "com.hancekim.billboard.appfunctions.BillboardFunctionsTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/hancekim/billboard/appfunctions/BillboardFunctions.kt \
        app/src/test/java/com/hancekim/billboard/appfunctions/BillboardFunctionsTest.kt \
        app/build.gradle.kts
git commit -m "feat(app): add BillboardFunctions.getCurrentHot100TopSong @AppFunction"
```

---

## Task 4: BillboardApplication → AppFunctionConfiguration.Provider

**Files:**
- Modify: `app/src/main/java/com/hancekim/billboard/BillboardApplication.kt`

- [ ] **Step 1: Add interface + injection + override**

Replace the file contents with:

```kotlin
package com.hancekim.billboard

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.appfunctions.service.AppFunctionConfiguration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.hancekim.billboard.appfunctions.BillboardFunctions
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BillboardApplication :
    Application(),
    SingletonImageLoader.Factory,
    AppFunctionConfiguration.Provider {

    @Inject
    lateinit var imageLoader: ImageLoader

    // 콜드부팅 시 Hilt 가 BillboardFunctions 를 주입 → AppFunctions 시스템에 노출.
    @Inject
    lateinit var billboardFunctions: BillboardFunctions

    val isDebuggable: Boolean
        get() {
            return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

    override fun onCreate() {
        super.onCreate()
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(BillboardFunctions::class.java) { billboardFunctions }
            .build()
}
```

- [ ] **Step 2: Build app**

```bash
./gradlew :app:assembleProdDebug 2>&1 | tail -40
```

Expected: `BUILD SUCCESSFUL`. If KSP emits a schema-generation error, copy the error and stop — the alpha09 API may have shifted; consult `https://developer.android.com/ai/appfunctions/add-appfunctions`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hancekim/billboard/BillboardApplication.kt
git commit -m "feat(app): wire BillboardApplication as AppFunctionConfiguration.Provider"
```

---

## Task 5: ADB verification on Galaxy S25

**Files:** none (runtime verification)

- [ ] **Step 1: Install on S25**

```bash
./gradlew :app:installProdDebug
```

Expected: `Installed on 1 device.` If install fails with `INSTALL_FAILED_OLDER_SDK`, the S25 firmware is below Android 16 — abort and reconsider minSdk floor.

- [ ] **Step 2: List app functions exposed by our package**

```bash
adb shell cmd app_function list-app-functions | grep --after-context 10 com.hancekim.billboard
```

Expected: at least one block referencing `BillboardFunctions` and `getCurrentHot100TopSong` with `enabled=true`.

If empty: the schema was not indexed. Re-run `./gradlew :app:installProdDebug` and wait ~30s for AppSearch indexing, then retry. Still empty → check `adb logcat | grep -iE "appfunction|appsearch"` for indexing failures.

- [ ] **Step 3: Document the verification result inline**

Append the actual ADB output (the function block) to `docs/superpowers/specs/2026-06-01-appfunctions-spike-design.md` under a new "## Verification Results" section. This makes the spike's outcome reviewable from the spec.

- [ ] **Step 4: Commit verification evidence**

```bash
git add docs/superpowers/specs/2026-06-01-appfunctions-spike-design.md
git commit -m "docs(spike): record adb list-app-functions verification output"
```

---

## Task 6: End-to-end Gemini app test (manual, user-driven)

**Files:** none

- [ ] **Step 1: With the Billboard app force-stopped, open the Gemini app on the S25**

```
adb shell am force-stop com.hancekim.billboard
```

Then on the S25 device: launch the Gemini app.

- [ ] **Step 2: Ask the canonical query**

In Gemini app, type or speak:

> "What's the current #1 song on the Billboard Hot 100?"

- [ ] **Step 3: Observe behavior — three possible outcomes**

| Outcome | Meaning | Next action |
|---|---|---|
| Gemini answers with live chart's title + artist | ✅ Full spike success | Append outcome to spec's Verification Results, then merge to develop or hold for design review |
| Gemini answers from its own training data (stale title) | ⚠️ Function exposed but not discovered | Try variations: "Use the Billboard app to find the current Hot 100 #1", or invoke via Samsung Bixby; record what works |
| Gemini answers "I can't help with that" / errors | ❌ Discovery or invocation failure | Capture `adb logcat -d -b all \| grep -iE "appfunction\|billboard\|gemini"` immediately after attempt; append findings to spec |

- [ ] **Step 4: Verify cold-boot path explicitly**

```bash
adb shell am force-stop com.hancekim.billboard
adb logcat -c
```

Repeat Step 2 query, then:

```bash
adb logcat -d | grep -iE "billboard|appfunction" | head -20
```

Expected (success): logs show `BillboardApplication` being constructed during the Gemini-initiated invocation — confirming cold-boot DI works.

- [ ] **Step 5: Append final findings to spec, commit, and push**

Append a "## Final Outcome" block to the spec with the three outcomes above checked off (✅/⚠️/❌), the cold-boot log evidence, and any follow-up needed.

```bash
git add docs/superpowers/specs/2026-06-01-appfunctions-spike-design.md
git commit -m "docs(spike): record Gemini app end-to-end test outcome"
git push -u origin feature/appfunctions-spike
```

---

## Self-Review Notes

**Spec coverage check:**
- Spec §"Goals" — covered by Tasks 1-5 (one `@AppFunction`, cold-boot DI, discovery, invocation verification).
- Spec §"Changes" 4 items — Task 1 (catalog), Task 2 (build), Task 3 (BillboardFunctions), Task 4 (Application). ✅
- Spec §"Verification Steps" 5 items — Tasks 5 (ADB) + 6 (Gemini E2E with cold-boot). ✅
- Spec §"Out of Scope" items — none referenced in any task. ✅

**Type consistency:**
- `BillboardFunctions` constructor param `getBillboardHot100UseCase: GetBillboardHot100UseCase` matches `:core:domain` definition (verified by reading the source).
- `TopSong(title, artist, rank)` referenced consistently across Task 3 test + impl, no rename.
- `AppFunctionConfiguration.Provider` interface name verified against alpha09 docs.

**Placeholder scan:** no TBD / "add appropriate error handling" / "similar to Task N" patterns found.

**Open risk to flag at execution time:** Step 4 of Task 6 may show no logs if Gemini app never actually invokes our function. That is a discovery problem (Google allowlist / Play Console enrollment / category), not a code problem — the spec already lists it as the primary unknown.
