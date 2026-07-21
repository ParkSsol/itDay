> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: pr
description: Creates a GitHub PR for the current branch following the It-Day PR template. Reads git diff and commit history to fill in the template automatically. Usage: /pr [issue number]
---

You will create a GitHub Pull Request for the current branch following the project's PR template and conventions.

## Step 1 — Gather Information

Run these commands in parallel to understand what changed:
- `git log develop..HEAD --oneline` — commit list
- `git diff develop...HEAD --stat` — changed files summary
- `git diff develop...HEAD` — full diff (for understanding changes)

Then read `.github/pull-request-template.md` to get the exact template format. Use the template as-is — do not invent or skip any section.

## Step 2 — Determine PR Type

Based on the changes, determine the type label:
- `[Feature]` — new functionality
- `[Bug]` — bug fix
- `[Refactor]` — code improvement without behavior change
- `[Chore]` — build, config, dependency changes
- `[Design]` — architecture or design decisions

## Step 3 — Check Line Count

Parse the `--stat` output and sum the insertions + deletions.
- If over 200 lines: warn the user and ask for confirmation before proceeding (project rule: PR must be ≤ 200 lines)
- Exception: if the branch contains a `stacked-pr` label or the user confirms, proceed anyway

## Step 4 — Fill the Template

Read `.github/pull-request-template.md` and fill in each section from the diff and commit history:
- **PR title**: `[Type] 한 줄 요약` (match the title format shown at the top of the template)
- **PR 개요**: one-line summary of what this PR does
- **작업 내용**: checkbox list derived from actual changes
- **관련 이슈**: use `Closes #N` if an issue number was passed as argument (e.g. `/pr 42` → `Closes #42`); omit if none
- **화면 캡처**: leave placeholder text as-is (user fills this manually)
- **체크 사항**: keep all checkboxes unchecked — do not pre-check any
- **기타 참고 사항**: brief note for reviewers about non-obvious decisions or tradeoffs, if any

## Step 5 — Create the PR

Use `gh pr create` with:
- `--base develop`
- `--title "[Type] 한 줄 요약"`
- `--body` containing the filled template (use HEREDOC to preserve formatting)

## Final Output

Print the PR URL after creation.
If line count exceeds 200, print a warning **before** creating and ask the user to confirm.
