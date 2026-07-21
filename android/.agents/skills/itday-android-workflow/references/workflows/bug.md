> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: bug
description: Runs a 3-stage pipeline (analyze → fix → review) to resolve a bug in It-Day. Usage: /bug <description>
---

You will fix a bug in It-Day using a chained sub-agent pipeline. The bug description follows after this prompt.

Execute the following 3 stages **sequentially**.

---

## Stage 1 — Analyze (bug-analyzer)

Launch the `bug-analyzer` agent with the bug description. Wait for the full root cause analysis and fix plan before proceeding.

```
Input: bug description provided by the user
```

---

## Stage 2 — Fix (feature-coder)

Launch the `feature-coder` agent. Pass it:
- The original bug description
- The complete analysis and fix plan from Stage 1

Instruct it to implement the minimal fix — do not add features, do not refactor unrelated code.

Wait for the agent to finish. Collect the **"Modified Files" list** from its output before proceeding.

---

## Stage 3 — Review (feature-reviewer)

Launch the `feature-reviewer` agent. Pass it:
- The list of file paths from Stage 2 (the agent will read the current state from disk)
- Instruct it to verify the fix is correct and does not introduce regressions

---

## Final Output

```
## Bug Fix Complete

### Root Cause
[from Stage 1]

### Files Changed
[list]

### Review Result
[findings or "No issues found"]
```

If the reviewer finds any **High severity** issues, highlight them and ask the user whether to proceed.
