> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: feature
description: Runs a 4-stage agent pipeline (plan → code → refactor → review) to implement a new feature in It-Day. Usage: /feature <description>
---

You will implement a new feature for It-Day using a chained sub-agent pipeline. The user's feature request follows after this prompt.

Execute the following 4 stages **sequentially**, passing each stage's output as input to the next.

---

## Stage 1 — Plan (feature-planner)

Launch the `feature-planner` agent with the feature description. Wait for its full output before proceeding.

```
Input: the feature description provided by the user
```

---

## Stage 2 — Code (feature-coder)

Launch the `feature-coder` agent. Pass it:
- The original feature description
- The complete plan output from Stage 1

Wait for the agent to finish writing all files. Collect the **"Modified Files" list** from its output before proceeding.

---

## Stage 3 — Refactor (feature-refactorer)

Launch the `feature-refactorer` agent. Pass it:
- The list of file paths from Stage 2 (paths only — the agent will read them directly)

Wait for the refactoring report before proceeding.

---

## Stage 4 — Review (feature-reviewer)

Launch the `feature-reviewer` agent. Pass it:
- The list of file paths from Stage 2 (the agent will read the current state from disk)
- Instruct it to focus on correctness, security, and architecture violations

---

## Final Output

After all 4 stages complete, present a summary to the user:

```
## Feature Implementation Complete

### Files Changed
[list all created/modified files]

### Refactoring Summary
[key improvements made]

### Review Report
[findings from the reviewer, or "No issues found"]
```

If the reviewer reports any **High severity** issues, tell the user which files need attention before merging.
