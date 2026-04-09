---
name: create-release-pr
description: Use this skill when the user says "릴리즈 PR 만들어", "릴리즈 하자", "create release PR", or similar. Full release PR pipeline — analyzes current branch vs latest released tag, proposes a semver bump (major/minor/patch), waits for user confirmation, renames/checks out to `release/vX.Y.Z`, bumps `gradle/libs.versions.toml`, commits, pushes, and creates the PR with a templated body and `release` label so `claude-review.yml` runs automatically.
---

# Create Release PR Skill

This skill owns the **entire release PR pipeline** from "I'm done working" to "PR is up for review". It is split into numbered phases with **explicit user-confirmation gates** — never auto-commit or auto-push without approval.

## Inputs

- Current working branch with changes (not `main`)
- Clean working tree preferred (uncommitted changes will be flagged in Phase 0)

## Phase 0 — Preconditions

```bash
git rev-parse --is-inside-work-tree
git branch --show-current
git status --porcelain
git fetch origin main --tags
```

Abort with a clear Korean message if:
- Current branch is `main` → "main 에서 바로 릴리즈는 안 돼요. 작업 브랜치로 먼저 체크아웃해주세요."
- Working tree is dirty → ask the user: "커밋 안 된 변경이 있어요. (1) stash (2) 커밋 (3) 중단 — 어떻게 할까요?"
- `origin/main` fetch fails → report the error, abort

## Phase 1 — Analyze diff against the latest released version

Find the latest release tag:

```bash
git tag --list 'v*' --sort=-version:refname | head -1
```

If no tag exists → treat `origin/main` as the baseline and note "최초 릴리즈" in the analysis.

Inspect the diff between the latest tag (or `origin/main`) and the current HEAD:

```bash
BASE=<latest tag or origin/main>
git log $BASE..HEAD --pretty=format:"- %s"
git diff $BASE...HEAD --stat
git diff $BASE...HEAD --name-only
```

Summarize for the user:
- 커밋 몇 개
- 변경 파일 몇 개, +/- 라인 수
- 영향 받은 모듈 (`:app`, `:feature:*`, `:core:*`)
- 각 커밋 제목 리스트

## Phase 2 — Propose semver bump

Read the current version:

```bash
grep -E "appVersionCode|appVersionName" gradle/libs.versions.toml
```

Based on the diff analysis, **propose** (not enforce) a semver bump:

| Type | When | Billboard 예시 |
|---|---|---|
| **major** (X.0.0) | Breaking public API, 데이터 마이그레이션, minSdk 상향, 대규모 리팩토링 | minSdk 32→34, Room 스키마 변경 |
| **minor** (1.X.0) | 새 기능, 새 화면, 새 UseCase/Repository, 새 DTO 필드 | Setting 화면, 새 차트 타입 |
| **patch** (1.0.X) | 버그픽스, 문서, 빌드 튜닝, 작은 리팩토링 | R8 룰 수정, 아이콘 변경 |

Output for the user (Korean):

```
📊 변경 요약
  - 커밋 N 개 / M 파일 / +A -D 라인
  - 모듈: ...

현재 버전
  appVersionName: X.Y.Z
  appVersionCode: N

🎯 제안: <major|minor|patch> → <new version>
   근거: <구체적인 이유 — 어떤 커밋 / 어떤 파일이 이 선택의 근거인지>

이대로 진행할까요? 다른 버전을 원하시면 말씀해주세요.
```

**GATE 1** — Do not proceed without explicit user confirmation. Accept a different semver choice if the user overrides.

## Phase 3 — Branch handling

After the user confirms the version, determine how to handle the branch:

```bash
CURRENT=$(git branch --show-current)
TARGET="release/v<new_version>"
```

Three cases:

1. **Current branch name already matches `release/v<new_version>`** → nothing to do
2. **Current branch name is different but not yet on remote** → rename locally: `git branch -m "$TARGET"`
3. **Current branch is already pushed to remote under a different name** → ask the user: "기존 원격 브랜치를 그대로 둘까요, 아니면 `$TARGET` 로 새로 push 할까요?"

**GATE 2** — Confirm branch handling with the user before making any change.

## Phase 4 — Bump version in libs.versions.toml

Edit `gradle/libs.versions.toml`:
- `appVersionName` → the confirmed semver (e.g., `"1.0.1"`)
- `appVersionCode` → previous value + 1 (always monotonic, regardless of semver type)

Use the Edit tool, only those two lines. Never touch anything else in the file.

Verify the edit:

```bash
grep -E "appVersionCode|appVersionName" gradle/libs.versions.toml
```

Show the before/after diff to the user. **GATE 3** — confirm the edit looks right before committing.

## Phase 5 — Commit

```bash
git add gradle/libs.versions.toml
git commit -m "release v<new_version>"
```

Show the resulting `git log -1` to the user.

## Phase 6 — Push

```bash
git push -u origin "$TARGET"
```

If the branch already has a different name on remote, handle per the decision in Phase 3.

**GATE 4** — Confirm push result with the user (success/failure).

## Phase 7 — Analyze changes for the PR body

Collect data needed for the PR body:

```bash
# commit list
git log $BASE..HEAD --pretty=format:"- %s"

# changed files with stats
git diff $BASE...HEAD --numstat

# module labels
git diff $BASE...HEAD --name-only | \
  sed -E 's|^(app\|build-logic\|feature/[^/]+\|core/[^/]+).*|\1|' | \
  sort -u
```

Group changed files by module for the body table.

## Phase 8 — Generate the PR title

Format: `Release v<new_version> — <one-line summary>`

Rules:
- 요약은 **한국어**
- 60자 이내
- 주요 의도를 한 줄로 (모든 변경을 나열하지 말 것)
- 뒤에 마침표 없음

예시:
- `Release v1.2.0 — 차트 필터 기능 추가`
- `Release v1.1.3 — prodRelease R8 크래시 수정`

## Phase 9 — Generate the PR body (Korean)

Use this exact template. Replace all `<...>` placeholders.

```markdown
## 📝 작업 상세 설명

<두세 문단으로 이번 릴리즈의 주요 변경 사항 설명.
 무엇을 했고, 왜 했는지, 사용자/개발자 관점의 영향.
 커밋 메시지와 diff 를 종합해 자연어로 풀어서 작성.
 단순 나열이 아니라 문맥과 의도를 담을 것.>

## 🔀 변경 흐름

<Mermaid flowchart — Phase 10 규칙 참조>

## 📂 변경 파일

| 모듈 | 파일 | +/- |
|---|---|---|
| :feature:home | HomePresenter.kt | +45/-12 |
| :core:domain | GetBillboardChartUseCase.kt | +28/-0 |
| ... | ... | ... |

**합계**: <N> 파일, +<additions> / -<deletions>

## 📦 버전

- `appVersionName`: <old> → **<new>**
- `appVersionCode`: <old> → **<new>**

## ✅ 체크리스트

- [x] `./gradlew :app:assembleProdRelease` 빌드 성공
- [ ] 수동 테스트 완료
- [ ] Baseline profile 갱신 여부 확인
- [ ] CLAUDE.md 업데이트 필요 여부 확인
- [ ] breaking change 여부 확인

## 🔍 리뷰 포인트

<리뷰어가 특별히 봐야 할 부분.
 예: R8 룰 변경, 새 의존성 추가, API 변경, 성능 임팩트.
 해당 사항 없으면 "특별히 주의 깊게 볼 항목 없음".>

## 🔗 관련

- <관련 이슈, 이전 PR, 참고 링크 — 없으면 "없음">

---

🤖 이 PR 은 `claude-review.yml` 워크플로우로 자동 리뷰될 예정이에요.
```

## Phase 10 — Mermaid rules

Pick one pattern based on what the PR touches:

- **Pattern A** — Feature 레이어 변경 (UI → Presenter → UseCase → Repository → DataSource)
- **Pattern B** — 모듈 의존 그래프 변경
- **Pattern C** — Compose-only (State → Ui → Component → Event → Presenter)
- **Pattern D** — Build/Gradle only → 다이어그램 생략, "빌드 설정만 변경 — 흐름도 생략" 텍스트로 대체

Highlight changed nodes with `:::changed` and append:
```
classDef changed fill:#fef3c7,stroke:#f59e0b,stroke-width:2px
```

10 노드 이내로 유지. 여러 패턴이 섞이면 A 기준으로 확장.

## Phase 11 — Create the PR

```bash
gh pr create \
  --base main \
  --head "$TARGET" \
  --title "<title from Phase 8>" \
  --body "$(cat <<'EOF'
<body from Phase 9>
EOF
)" \
  --label release
```

`--label release` is **mandatory** — it triggers `claude-review.yml`.

After creation, capture the PR URL from `gh pr create` output.

## Phase 12 — Post-creation notice (Korean)

Tell the user:
1. PR URL
2. `claude-review.yml` 이 자동으로 실행될 예정
3. 리뷰 완료 후 머지하면 → 태그 푸시 시 `release.yml` 이 APK 빌드 시작
4. 필요하면 베이스라인 프로파일을 추가로 갱신하라고 안내 (다음 세션의 baseline-profile.yml 이 아직 없으면)

## Rules

- **User confirmation is required at Gates 1–4.** Do not collapse them
- **Never force-push** or modify branches the user did not approve
- **Never skip the toml bump** — version and code must both advance
- **Always attach `release` label** in Phase 11 — it is load-bearing
- **Title and body both in Korean**
- **If a PR already exists** for the target branch, abort before Phase 11
- **Body structure must stay consistent with `pr-review-summary`** so PR description and auto-review comment look visually aligned
