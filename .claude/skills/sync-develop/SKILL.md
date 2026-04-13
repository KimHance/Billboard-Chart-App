---
name: sync-develop
description: Use this skill after a release PR has been squash-merged into main, to reset the develop branch to match main HEAD. Handles fetch, reset, force push, and cleanup of the old release branch. Invoke with "싱크해줘", "develop 최신화", "sync develop", or similar.
---

# Sync Develop Skill

After a release PR is squash-merged into main, develop still has the old pre-squash commits. This skill resets develop to match main HEAD so the next development cycle starts clean.

## Preconditions

```bash
git fetch origin main
git branch --show-current
```

- Must be on `develop` branch. If not, `git checkout develop` first.
- Abort if there are uncommitted changes: "커밋 안 된 변경이 있어요. stash 하거나 커밋한 후 다시 시도해주세요."

## Step 1 — Verify the merge happened

```bash
git log origin/main --oneline -5
```

Show the user the latest main commits. Ask for confirmation:

```
origin/main 최신 상태:
  abc1234 Release v1.1.0 — ... (#N)
  def5678 ...

이 상태로 develop 을 리셋할까요? (Y/n)
```

**GATE** — Do not proceed without explicit confirmation. This is a destructive operation.

## Step 2 — Reset develop to main HEAD

```bash
git reset --hard origin/main
```

## Step 3 — Force push

```bash
git push --force-with-lease origin develop
```

`--force-with-lease` ensures that if someone else (unlikely in solo project) pushed to develop in the meantime, the push is rejected rather than silently overwriting.

## Step 4 — Clean up release branch

Find and delete the merged release branch:

```bash
git branch -D release/v*
git push origin --delete release/vX.Y.Z
```

If no local release branch exists, skip silently. If remote deletion fails (already deleted by PR auto-delete), skip silently.

## Step 5 — Confirm

```bash
git log --oneline -3
```

Show the user:

```
✅ develop 이 main 과 동기화됐어요.

현재 develop HEAD:
  abc1234 Release v1.1.0 — ... (#N)

release/vX.Y.Z 브랜치 삭제됨.
다음 작업을 시작하세요!
```

## Rules

- **Always ask for confirmation before reset** — this is irreversible
- **Never run on main** — this skill is for develop only
- **Use `--force-with-lease`** not `--force`
- **If develop has commits not yet in any release** — warn the user: "develop 에 릴리즈에 안 들어간 커밋 N 개가 있어요. 리셋하면 사라져요. 계속할까요?"
