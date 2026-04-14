---
name: sync-develop
description: Use this skill after a release PR has been squash-merged into main. Resets develop to main HEAD, auto-creates and pushes the version tag (triggering release.yml for APK build), and cleans up the release branch. Invoke with "싱크해줘", "develop 최신화", "sync develop", or similar.
---

# Sync Develop Skill

After a release PR is squash-merged into main, this skill handles three things in one shot:
1. Reset develop to main HEAD
2. Create + push the version tag (triggers APK build via release.yml)
3. Clean up the release branch

## Preconditions

```bash
git fetch origin main --tags
git branch --show-current
```

- Must be on `develop` (or `main`). If on another branch, `git checkout develop` first.
- Abort if uncommitted changes exist.

## Step 1 — Verify the merge

```bash
git log origin/main --oneline -5
```

Show the user the latest main commits and ask for confirmation.

**GATE** — Do not proceed without explicit confirmation.

## Step 2 — Auto-tag from version catalog

Read the version from `gradle/libs.versions.toml`:

```bash
VERSION=$(grep 'appVersionName' gradle/libs.versions.toml | sed 's/.*= "//;s/"//')
TAG="v${VERSION}"
```

Check if the tag already exists:

```bash
git tag --list "$TAG"
```

- If tag already exists → skip tagging, notify user: "v{VERSION} 태그 이미 존재 — 스킵"
- If tag does not exist → create and push:

```bash
git checkout main
git pull origin main
git tag "$TAG"
git push origin "$TAG"
```

This push triggers `release.yml` → APK build → GitHub Release.

Notify user: "v{VERSION} 태그 푸시 완료 — release.yml 이 APK 빌드 시작합니다."

## Step 3 — Reset develop to main HEAD

```bash
git checkout develop
git reset --hard origin/main
git push --force-with-lease origin develop
```

## Step 4 — Clean up release branch

```bash
git branch -D release/v*
git push origin --delete release/v<version>
```

Skip silently if branch doesn't exist (already deleted by PR auto-delete).

## Step 5 — Confirm

```bash
git log --oneline -3
```

```
✅ 동기화 완료

  🏷️ v<version> 태그 푸시됨 → APK 빌드 중
  🔄 develop == main (리셋됨)
  🗑️ release/v<version> 삭제됨

다음 작업을 시작하세요!
```

## Rules

- Always confirm before reset (destructive)
- Never run reset on main
- Use `--force-with-lease` not `--force`
- Tag from main branch, not develop
- Skip tagging if tag already exists
- If develop has unreleased commits, warn before reset
