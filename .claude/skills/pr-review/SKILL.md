---
name: pr-review
description: Use this skill to run the full PR review pipeline for the Billboard project. Identifies changed files, dispatches the relevant specialized reviewer sub-agents in parallel (billboard-reviewer, compose-reviewer, module-boundary-checker), collects their findings, and hands off to the pr-review-summary skill to post the final review comment. Invoked from `.github/workflows/claude-review.yml` and also runnable locally.
---

# PR Review Orchestration

This skill is the **entry point** for the Billboard PR review pipeline. It owns three responsibilities: (1) identify what changed, (2) dispatch the right reviewers in parallel, (3) hand off to `pr-review-summary` for posting.

## Inputs

- `PR_NUMBER` — either from `${{ github.event.pull_request.number }}` in CI, or passed explicitly in local runs
- Working directory = the Billboard repository root with the PR branch checked out

## Step 0 — Preconditions

```bash
git rev-parse --is-inside-work-tree
git rev-parse HEAD
git fetch origin main
```

- Abort with a clear message if not inside a git repo
- Abort if `origin/main` cannot be fetched

## Step 1 — Identify changed files

```bash
git diff origin/main...HEAD --name-only
git diff origin/main...HEAD --stat
```

Save the file list for classification in Step 2.

## Step 2 — Classify changes into reviewer scopes

Walk the changed file list and build three bucket sets. A single file may land in multiple buckets.

**Bucket `billboard-reviewer`** — any of:
- `*.kt` under `app/src/`, `feature/*/src/`, `core/*/src/`, `build-logic/convention/src/` (**excluding** pure Composable files that only contain `@Composable` functions without any architectural change)
- `core/data/**/*.kt`, `core/data-impl/**/*.kt`, `core/data-source/**/*.kt`, `core/domain/**/*.kt`
- `app/proguard-rules.pro`
- Any file containing `@Inject`, `@AssistedInject`, `@CircuitInject`, `@Module`, `@Binds`, `@Provides`, `UseCase`, `Repository`, `DataSource`, `Presenter`, `@HiltAndroidApp`
- Test files under `src/test/` or `src/androidTest/`

**Bucket `compose-reviewer`** — any of:
- Files that declare `@Composable` functions
- Files defining `*State` classes implementing `CircuitUiState`
- Files defining `*Event` sealed interfaces implementing `CircuitUiEvent`
- Files under `core/design-system/src/`, `core/design-foundation/src/`
- Files under `feature/*/src/main/java/**/component/`

**Bucket `module-boundary-checker`** — any of:
- `**/build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `build-logic/convention/src/main/**`

Overlap across buckets is expected and OK. Each reviewer carries the **same** rule set (`.claude/rules/01~07.md`) — buckets only decide where each reviewer **starts**. Findings in another reviewer's primary scope (discovered while traversing) are tagged `[cross-cutting]` by the reviewer and dedup'd in Step 4.

If **no bucket** matches any file, exit early: post a single summary comment via `pr-review-summary` saying "리뷰 대상 파일 없음 — 스킵".

## Step 3 — Dispatch reviewers in parallel

**Always dispatch all three reviewers** as long as at least one bucket has a matching file. A reviewer whose primary-scope bucket is empty will respond with a one-line "스킵" message — that's expected and cheap. The "always dispatch" policy ensures no rule violation is missed even if our bucket classification missed a file.

Use the `Task` tool, one call per reviewer, **in the same assistant turn** so they run in parallel.

For each spawned agent, the prompt should include:
- The list of files in that bucket (relative paths)
- The base ref (`origin/main`) so they can run their own diff if needed
- A reminder to return findings as a structured list with: `severity`, `confidence`, `file`, `line`, `what`, `why`, `suggestion`
- An instruction to respond in Korean for the user-facing fields, English for field keys

Example Task prompt skeleton (adapt per bucket):

```
You are being dispatched by pr-review to review the following files in PR #${PR_NUMBER}:

<file list>

STEP 0 (mandatory, before anything else):
Use the Read tool to load all 7 rule files into your context:
  - .claude/rules/01-architecture.md
  - .claude/rules/02-circuit.md
  - .claude/rules/03-compose-state.md
  - .claude/rules/04-di-hilt.md
  - .claude/rules/05-error-handling.md
  - .claude/rules/06-testing.md
  - .claude/rules/07-design-system.md
Do not skip this step. The rules are the source of truth for HOW to judge issues.

STEP 1: Run your standard review scope on the file list above. While traversing, if you spot a high-confidence violation outside your primary scope, report it with a `[cross-cutting]` tag.

Return findings as a Korean-language list where each item has:
- severity: "major" (confidence 91-100) | "minor" (80-90) | "check" (70-79)
- file: <path>
- line: <line number in the new file>
- what: one sentence, Korean
- why: one-paragraph reasoning, Korean (this becomes the <details> body)
- suggestion: the fix code (no prose)
- reviewer: "<your agent name>"
- cross_cutting: true | false

Only return findings. Do not post anything to the PR.
```

## Step 4 — Collect results

Wait for all spawned agents to return. Aggregate into a single list of findings.

**Dedup rules** (apply in order):
1. Group findings by (`file`, `line`, semantically equivalent `what`).
2. Within a group, prefer the finding from the reviewer whose **primary scope** owns that file (no `[cross-cutting]` tag) over a `[cross-cutting]` finding.
3. If a group contains only `[cross-cutting]` findings (i.e. the primary-scope reviewer didn't flag it), keep the highest-confidence one and strip the tag in the final aggregated list.
4. Findings in different files / lines are never dedup'd, even if `what` is similar.

Also compute:
- `major_count` = findings with severity `major`
- `minor_count` = findings with severity `minor`
- `check_count` = findings with severity `check`
- `per_reviewer_counts` = map of reviewer name → count

## Step 5 — Hand off to pr-review-summary

Invoke the `pr-review-summary` skill with:
- The collected findings list
- The counts computed in Step 4
- `PR_NUMBER`
- The file list from Step 1 (for the scope table and the mermaid diagram source)

The summary skill is responsible for everything from this point: PR metadata fetch, summary body generation, mermaid diagram selection, `gh api` posting.

## Rules

- **Parallelism is mandatory** — when multiple buckets apply, spawn all of them in one assistant turn. Sequential spawning wastes minutes per run
- **Never post anything directly** from this skill — only `pr-review-summary` posts
- **Never modify code** — reviewers and orchestrator are read-only
- **If a reviewer fails** (timeout, error), still proceed with the remaining reviewers and note the failure in the handoff to `pr-review-summary`
- **Idempotency is out of scope** for this skill — the workflow-level `on.pull_request.types: [opened]` guarantees one invocation per PR
