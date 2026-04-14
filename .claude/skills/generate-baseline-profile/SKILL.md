---
name: generate-baseline-profile
description: Use this skill to generate baseline profiles locally and produce an analysis report. Runs Gradle Managed Device to collect profiles, copies to src/main, and writes docs/baseline-profile-report.md with rule counts, top classes, and delta vs previous. Invoke with "베이스라인 프로파일 생성", "프로파일 돌려줘", "generate baseline profile".
---

# Generate Baseline Profile Skill

Generates baseline profiles using Gradle Managed Device and produces an analysis report.

## Step 1 — Preconditions

```bash
git branch --show-current
git status --porcelain
```

- Warn if dirty tree (profile generation takes time, uncommitted changes might conflict)
- Any branch is OK (not restricted to develop)

## Step 2 — Generate baseline profile

```bash
./gradlew :app:generateDemoBaselineProfile \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect \
  --no-configuration-cache
```

Notes:
- `swiftshader_indirect` for headless/CI environments without GPU
- `--no-configuration-cache` because managed device tasks don't support it
- This automatically runs `copyBaselineProfileToMain` (finalized task in app/build.gradle.kts)
- Takes 10-25 minutes depending on machine

Show progress:
```
⏳ 베이스라인 프로파일 생성 중... (10~25분 소요)
   Device: Pixel 6 API 32 (Gradle Managed Device)
   Flavor: demo
```

## Step 3 — Verify output

```bash
ls -la app/src/main/generated/baselineProfiles/
wc -l app/src/main/generated/baselineProfiles/*.txt
```

Abort if no profile files found.

## Step 4 — Generate analysis report

Read the profile file and generate `docs/baseline-profile-report.md`:

```bash
PROFILE_DIR="app/src/main/generated/baselineProfiles"
PROFILE_FILE=$(ls $PROFILE_DIR/*.txt | head -1)
```

Report structure:

```markdown
# Baseline Profile Report

**Generated**: <date/time>
**Branch**: <current branch>
**Device**: Pixel 6 API 32 (Gradle Managed Device)
**Flavor**: demo

## Summary

| Metric | Value |
|---|---|
| Total rules | <count> |
| Startup (HSP) rules | <count of lines containing HSP> |
| Interaction rules | <total - startup> |
| File size | <size in KB> |

## Top 15 Classes

| Class | Rules |
|---|---|
| <class name> | <count> |
| ... | ... |

## Method Distribution

| Type | Count |
|---|---|
| Hot (H) | <count> |
| Startup (S) | <count> |
| Post-startup (P) | <count> |

## Delta vs Previous

If a previous report exists at `docs/baseline-profile-report.md`, compare:

| Metric | Previous | Current | Change |
|---|---|---|---|
| Total rules | <old> | <new> | +/-N (+/-%) |

If no previous report, note "첫 프로파일 생성 — 비교 대상 없음".

## Notes

<Any observations about the profile — new classes appearing, significant changes>
```

Write this to `docs/baseline-profile-report.md` using the Write tool.

## Step 5 — Show results

```
✅ 베이스라인 프로파일 생성 완료

📊 요약
  Total: N rules (HSP: M, Interaction: K)
  Top class: HomePresenter (45 rules)
  
📄 리포트: docs/baseline-profile-report.md

커밋할까요?
```

If user confirms, commit both profile files and report:

```bash
git add app/src/main/generated/baselineProfiles/ docs/baseline-profile-report.md
git commit -m "baseline profile 갱신 + 분석 리포트"
```

## Rules

- Never skip the report generation — always produce docs/baseline-profile-report.md
- Always show delta vs previous if previous report exists
- Do not modify any source code — only profile files and the report
- If generation fails, show the error and suggest checking emulator/SDK setup
