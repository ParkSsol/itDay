> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: issue
description: Creates a GitHub issue using templates and auto-fills content with github-issue-helper agent. Usage: /issue <type> <description>
---

You will create a GitHub issue for It-Day following the project's issue templates.

## Step 1 — Parse Issue Type

Extract the first argument as the issue type. Support both English and Korean:

**Type Mapping:**
- `bug` | `버그` → `.github/ISSUE_TEMPLATE/bug.md`
- `feature` | `기능` → `.github/ISSUE_TEMPLATE/feature.md`
- `chore` | `보수` | `유지` → `.github/ISSUE_TEMPLATE/chore.md`
- `refactor` | `리팩` | `리팩터` → `.github/ISSUE_TEMPLATE/refactor.md`
- `design` | `디자인` → `.github/ISSUE_TEMPLATE/design.md`

If the type is invalid or empty, respond with an error message listing valid types.

## Step 2 — Read the Template

Read the matching template from `.github/ISSUE_TEMPLATE/<type>.md`.
- Extract the YAML frontmatter to get `labels` field (e.g., `labels: bug`)
- Extract the body sections (everything after `---`)

## Step 3 — Validate GitHub CLI

Check if GitHub CLI is installed and authenticated:

```bash
gh auth status
```

If failed:
- Not installed: "❌ GitHub CLI가 설치되지 않았습니다. https://cli.github.com 에서 설치하세요."
- Not authenticated: "❌ GitHub 인증이 필요합니다. `gh auth login`을 실행하세요."

Proceed only if successful.

## Step 4 — Call github-issue-helper Agent

Pass the template content and user's description to the `github-issue-helper` agent:

```
Invoke: Agent(subagent_type="github-issue-helper", prompt="""
Issue type: {type}
User description: {rest_of_args}
Template body:
{template_body}
""")
```

The agent will return a two-part output:
- **SUMMARY: ...** (single line)
- **---** (separator)
- **Issue body** (markdown template)

## Step 5 — Parse Agent Output

Extract from agent response:
1. **First line** matching `SUMMARY: ...` → Extract text after `SUMMARY: ` as issue title
2. **Content after `---` separator** → Complete markdown body

Structure:
```
SUMMARY: 약국 목록 NPE 버그

---

## 🐞 문제 상황
...
```

Parse to get:
- Title: `약국 목록 NPE 버그`
- Body: everything after `---`

## Step 6 — Preview Issue Content

Print to user:
```
─────────────────────────────────────
📋 생성할 이슈 미리보기
─────────────────────────────────────
제목: [Bug] 약국 목록 NPE 버그

본문:
## 🐞 문제 상황
...
─────────────────────────────────────
이 내용으로 이슈를 생성하시겠습니까? (y/n)
```

If user enters `n`: Offer to edit specific sections (optional — for now, abort and ask user to retry)
If user enters `y`: Proceed to Step 7

## Step 7 — Create the Issue

Run `gh issue create` with:
- `--title "[{TYPE_PREFIX}] {title_from_summary}"`
- `--body "{body_from_agent}"`
- `--label "{emoji_label}"` (from mapping table, not frontmatter)
- Additional label: `📌 status: todo`

**Type Prefix & Emoji Label Mapping (authoritative source):**

| Type | Prefix | Label |
|------|--------|-------|
| bug | Bug | 🐞 bug |
| feature | Feature | ✨ feature |
| chore | Chore | 🧹 chore |
| refactor | Refactor | 🔧 refactor |
| design | Design | 💡 design |

**IMPORTANT**: Always use the values from this table, ignoring template frontmatter `labels:` field.

Command template:
```bash
gh issue create \
  --title "[Bug] 약국 목록 NPE 버그" \
  --body "## 🐞 문제 상황
..." \
  --label "🐞 bug" \
  --label "📌 status: todo"
```

## Step 8 — Print Result

Output the created issue URL and number in format:
```
✅ 이슈 생성 완료: #123
🔗 URL: https://github.com/<organization>/<repository>/issues/123
```

## Error Handling

**Invalid type**:
```
❌ 유효하지 않은 타입입니다. 다음 중 하나를 사용하세요:
bug, 버그, feature, 기능, chore, 보수, refactor, 리팩, design, 디자인
```

**Template file missing**:
```
❌ 템플릿을 찾을 수 없습니다: .github/ISSUE_TEMPLATE/{type}.md
```

**GitHub CLI not installed/authenticated**:
(Caught in Step 3)

**gh issue create failed**:
```
❌ 이슈 생성에 실패했습니다.
오류: {error message}
GitHub CLI 인증을 확인하세요: gh auth status
```

**User cancels preview**:
```
❌ 이슈 생성이 취소되었습니다.
다시 시도하려면 `/issue {type} {description}`을 실행하세요.
```
