---
name: create-release-pr
description: Use this skill when the user says "릴리즈 PR 만들어", "릴리즈 하자", "create release PR", or similar. Must be on develop branch. Creates a release/vX.Y.Z branch from develop, bumps version, pushes, and opens a PR to main with release label. After merge, use sync-develop skill to reset develop.
---

# Create Release PR Skill

Full release PR pipeline for solo development on the `develop` branch. Creates a separate `release/vX.Y.Z` branch so develop stays free for next work while the PR is under review.

## Phase 0 — Preconditions

```bash
git branch --show-current
git status --porcelain
git fetch origin main --tags
```

- **Must be on `develop`**. If on `main` → "main 에서는 안 돼요. `git checkout develop` 먼저." If on another branch → "develop 에서만 릴리즈 가능해요."
- Dirty tree → ask: "(1) stash (2) 커밋 (3) 중단"
- Fetch fail → abort

## Phase 1 — Diff analysis

```bash
BASE=$(git tag --list 'v*' --sort=-version:refname | head -1)
# no tag → BASE=origin/main

git log ${BASE}..HEAD --pretty=format:"- %s"
git diff ${BASE}...HEAD --stat
```

Summarize: commit count, files, +/- lines, affected modules.

## Phase 2 — Semver proposal

```bash
grep -E "appVersionCode|appVersionName" gradle/libs.versions.toml
```

| Type | When |
|---|---|
| major | breaking change, minSdk bump, data migration |
| minor | new feature, new screen, new UseCase |
| patch | bugfix, build tweak, refactor |

**GATE 1** — show proposal with reasoning, wait for confirmation.

## Phase 3 — Create release branch from develop

```bash
git checkout -b release/v<version>
```

This creates a new branch from current develop HEAD. **develop is untouched** — user can switch back and keep working while PR is under review.

**GATE 2** — confirm branch creation.

## Phase 4 — Bump version

Edit `gradle/libs.versions.toml`:
- `appVersionName` → new version
- `appVersionCode` → previous + 1

**GATE 3** — show before/after, confirm.

## Phase 5 — Commit + Push

```bash
git add gradle/libs.versions.toml
git commit -m "release v<version>"
git push -u origin release/v<version>
```

**GATE 4** — confirm push result.

## Phase 6 — Create PR

Use MCP `mcp__github__create_pull_request`:
- base: `main`
- head: `release/v<version>`
- title: `Release v<version> — <한국어 요약>`

Then add label via `mcp__github__update_issue`:
- labels: `["release"]`

PR body template (Korean, keep it scannable):

```markdown
## 📝 작업 상세 설명
<2-4 short paragraphs>

## 🔀 변경 흐름
<mermaid — Pattern A/B/C/D>

## 📂 변경 파일
| 파일 | +/- |
|---|---|

## 📦 버전
- appVersionName: old → **new**
- appVersionCode: old → **new**

## ✅ 체크리스트
- [x] assembleProdRelease 빌드 성공
- [ ] 수동 테스트
- [ ] Breaking change 확인

---
🤖 `claude-review.yml` 로 자동 리뷰됩니다.
```

Mermaid rules:
- Pattern A: Feature layer (UI→Presenter→UseCase→Repo)
- Pattern B: Module dependency graph
- Pattern C: Compose only (State→Ui→Component)
- Pattern D: Gradle only → skip diagram
- `:::changed` + `classDef changed fill:#fef3c7,stroke:#f59e0b,stroke-width:2px`
- Max 10 nodes

## Phase 7 — Post-creation notice

Tell the user:
1. PR URL
2. `claude-review.yml` 자동 실행 예정
3. `develop` 으로 돌아가서 다음 작업 가능: `git checkout develop`
4. 머지 후 → `sync-develop` 스킬로 develop 최신화
5. 그 다음 태그 푸시 → `release.yml` 로 APK 빌드

```
✅ PR 생성 완료: <URL>

📌 다음 단계:
  git checkout develop   ← 다음 작업 시작 가능
  (리뷰 끝나고 머지 후)
  "싱크해줘"             ← develop 최신화
  git tag v<version> && git push origin v<version>  ← APK 빌드
```

## Rules

- **develop 에서만** 실행 가능
- **release branch 는 develop 에서 분기** (리네임 X, 새 브랜치 O)
- develop 은 건드리지 않음 — 리뷰 중 다음 작업 가능
- User confirmation at Gates 1-4
- Never force-push
- Always attach `release` label (triggers review workflow)
- If PR already exists for target branch → abort
