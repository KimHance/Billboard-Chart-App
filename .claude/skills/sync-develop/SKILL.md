---
name: sync-develop
description: Use this skill after a release PR has been squash-merged into main. Resets develop to main HEAD and cleans up the release branch. Tag push and APK build are handled automatically by `auto-release.yml` on PR merge. Invoke with "싱크해줘", "develop 최신화", "sync develop", or similar.
---

# Sync Develop Skill

After a release PR is squash-merged into main, this skill handles two things:
1. Reset develop to main HEAD
2. Clean up the release branch

> **Tag push and APK release are no longer this skill's responsibility.**
> `auto-release.yml` automatically tags and builds the APK on PR merge —
> see `.github/workflows/auto-release.yml`.

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

## Step 2 — Reset develop to main HEAD

```bash
git checkout develop
git reset --hard origin/main
git push --force-with-lease origin develop
```

## Step 3 — Clean up release branch

```bash
git branch -D release/v*
git push origin --delete release/v<version>
```

Skip silently if branch doesn't exist (already deleted by PR auto-delete).

## Step 4 — Confirm

```bash
git log --oneline -3
```

Read `gradle/libs.versions.toml` to surface the version for the user:

```bash
VERSION=$(grep 'appVersionName' gradle/libs.versions.toml | sed 's/.*= "//;s/"//')
```

```
✅ 동기화 완료

  🔄 develop == main (리셋됨)
  🗑️ release/v<VERSION> 삭제됨

  ℹ️ v<VERSION> 태그 + APK 빌드는 auto-release.yml 이 처리 중입니다.
     진행 상태: gh run list --workflow=auto-release.yml --limit 1
```

## Rules

- Always confirm before reset (destructive)
- Never run reset on main
- Use `--force-with-lease` not `--force`
- If develop has unreleased commits, warn before reset
- Do **not** push tags from this skill — `auto-release.yml` owns that responsibility
