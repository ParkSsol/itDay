> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: workflow
description: Check CI workflow status, view logs, and rerun failed steps. Usage: /workflow [status|check|rerun <id>|list]
---

You will manage GitHub Actions workflows for It-Day.

Default action: `status` (if no subcommand provided)

## Subcommand: status

Show the 10 most recent workflow runs on the current branch.

```bash
git branch --show-current | xargs -I {} gh run list --branch {} --limit 10 --json name,status,conclusion,createdAt,databaseId
```

Format output as a readable table showing:
- Workflow name
- Status (한국어 매핑)
- Conclusion (한국어 매핑)
- Time created (relative time, e.g., "2 hours ago")
- Run ID

**Status & Conclusion Mapping (한국어):**

| API Value | 표시 |
|-----------|------|
| completed | ✅ 완료 |
| in_progress | 🏃 진행 중 |
| queued | ⏳ 대기 중 |
| requested | 📮 신청됨 |
| success | ✅ 성공 |
| failure | ❌ 실패 |
| cancelled | ⛔ 취소 |
| skipped | ⏭️ 스킵 |
| neutral | ⚪ 중립 |

Example output:
```
브랜치: feature/benefit-list 기준 최근 10개 실행 결과
────────────────────────────────────────────────────
워크플로우 이름           상태        결과      생성       ID
code-style-check      ✅ 완료      ✅ 성공   2시간 전  12345
test-coverage         ✅ 완료      ❌ 실패   2시간 전  12344
pr-labeler            ✅ 완료      ✅ 성공   1시간 전  12343
build-android         🏃 진행 중   —         30초 전   12342
```

## Subcommand: check

Show HEAD commit's workflow status and failed step logs across all concurrent workflows.

Steps:
1. Get all runs for current commit (remove `--limit 1` to get all concurrent runs):
   ```bash
   git rev-parse HEAD | xargs -I {} gh run list --commit {} --limit 20 --json databaseId,name,status,conclusion,createdAt | jq -r '.[] | "\(.databaseId) \(.name) \(.status) \(.conclusion)"'
   ```

2. For each failed run, view full details:
   ```bash
   gh run view <run_id> --log
   ```

3. Aggregate results: show all failed workflows and their failed step names.

Print format (example with multiple workflows):
```
HEAD 커밋 기준 워크플로우 실행 결과
─────────────────────────────────────────
✅ code-style-check #12346 — 성공
✅ pr-line-limit #12345 — 성공
❌ test-unit-tests #12344 — 실패

❌ test-unit-tests 실패 단계:
- unit-tests (종료 코드 1)
  > 에러: Database connection timeout
- integration-tests (종료 코드 1)
  > 에러: Test timeout

자세한 로그: gh run view 12344 --log
```

If all workflows passed:
```
✅ 모든 워크플로우 성공 (HEAD 커밋)
```

If HEAD not yet pushed to GitHub:
```
⚠️  현재 HEAD 커밋에 대한 워크플로우 실행 내역이 없습니다.
커밋이 아직 push되지 않았거나 워크플로우가 시작되지 않았습니다.
```

## Subcommand: rerun <id_or_name>

Rerun only failed steps for a workflow run with polling for result.

Steps:
1. If input is non-numeric, resolve workflow name to most recent run ID:
   ```bash
   gh workflow list --json name,id | jq -r '.[] | select(.name == "<name>") | .id' | head -1
   ```
   Then get most recent run:
   ```bash
   gh run list --workflow <resolved_workflow_id> --limit 1 --json databaseId | jq -r '.[0].databaseId'
   ```

2. Rerun failed steps only:
   ```bash
   gh run rerun <run_id> --failed-only
   ```

3. Poll for completion (15초 간격, 최대 5회):
   ```bash
   for i in {1..5}; do
     sleep 15
     gh run view <run_id> --json status,conclusion
   done
   ```

4. Print status with final result:
   ```
   🔄 워크플로우 재실행 중: run #<id>

   [15초 경과] 상태: 진행 중
   [30초 경과] 상태: 진행 중
   [45초 경과] 상태: 완료

   ✅ 재실행 완료: #<id> — 성공
   자세한 로그: gh run view <id>
   ```

   If failed:
   ```
   ❌ 재실행 완료: #<id> — 실패
   다시 재실행하려면: /workflow rerun <id>
   ```

**Known workflow names** (for reference; dynamic lookup is primary):
- code-style-check
- test-coverage
- issue-labeler
- labels-sync
- pr-labeler
- pr-line-limit
- pr-merge-status
- pr-title-labeler

## Subcommand: list

List all workflows in the repository with their current state.

```bash
gh workflow list --json name,state,path
```

Format as a table with:
- Workflow name
- State (active → ✅ 활성, disabled → ⛔ 비활성, deleted → 🗑️ 삭제)
- Filename (.github/workflows/*.yml)

Example:
```
워크플로우 이름            상태     파일 경로
code-style-check    ✅ 활성   .github/workflows/code-style-check.yml
test-coverage       ✅ 활성   .github/workflows/test-coverage.yml
issue-labeler       ✅ 활성   .github/workflows/issue-labeler.yml
deprecated-check    ⛔ 비활성  .github/workflows/deprecated-check.yml
```

## Error Handling

**Invalid subcommand**:
```
❌ 유효하지 않은 서브커맨드입니다.
사용법: /workflow [status|check|rerun <id|name>|list]
```

**Run ID not found**:
```
❌ 해당 ID의 워크플로우 실행을 찾을 수 없습니다.
최근 실행 목록: /workflow status
```

**Workflow name not found**:
```
❌ 워크플로우 '{name}'을(를) 찾을 수 없습니다.
사용 가능한 워크플로우: /workflow list
```

**Network error / GitHub CLI error**:
```
❌ GitHub CLI 오류가 발생했습니다.
- 인터넷 연결을 확인하세요
- GitHub 인증을 확인하세요: gh auth status
```

**HEAD not pushed yet**:
(Handled in `check` subcommand output)
