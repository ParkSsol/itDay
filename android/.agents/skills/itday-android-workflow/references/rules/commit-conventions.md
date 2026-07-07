> It-Day 적용 메모: 원문의 상세 커밋·브랜치 규칙을 유지하면서 UI 예시를 Compose 전용 용어로 변환한 문서입니다.

# Commit & Branch Conventions

## Table of Contents

- [Conventional Commits Format](#conventional-commits-format)
- [Branch Naming](#branch-naming)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Commit Best Practices](#commit-best-practices)
- [Pre-commit Checks](#pre-commit-checks)
- [Collaboration Rules](#collaboration-rules)
- [Common Commands](#common-commands)
- [Issue Linking](#issue-linking)

## Conventional Commits Format

All commits follow **Conventional Commits** with **Korean language**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type
```
feat       - New feature
fix        - Bug fix
chore      - Maintenance, setup, dependency updates
docs       - Documentation changes
refactor   - Code refactoring (no feature change)
test       - Test-related changes
perf       - Performance improvements
ci         - CI/CD configuration changes
```

### Scope
Feature name or component affected:
```
auth       - Authentication/Login feature
benefit       - Membership search feature
home       - AI consultation feature
map   - Map/store location feature
setting   - User information feature
network    - Network/API layer
ui         - UI components
permission - Permission handling
```

### Subject
- **Max 50 characters**
- **Imperative mood**: "add", "fix", "refactor", not "added", "fixed", "refactored"
- **Korean language**: Write naturally in Korean
- **No period at the end**

### Body
- **Optional but recommended** for significant changes
- Separate from subject by blank line
- Explain **what** and **why**, not how
- Wrap at ~72 characters
- Use bullet points if describing multiple items

### Footer
- **Optional**: Reference GitHub issues
- Format: `Fixes #123`, `Closes #456`, `Related-To #789`

## Examples

### Simple Feature
```
feat(benefit): 혜택 검색 기능 구현
```

### Feature with Detail
```
feat(benefit): 혜택 검색 기능 구현 - 키워드 기반 검색

구현 사항:
- MembershipApi에 searchMemberships 엔드포인트 통합
- BenefitViewModel debounce 로직 추가 (300ms 지연)
- SearchScreen UI 개발 및 테스트
- 네트워크 요청 실패 시 에러 메시지 표시

Fixes #45
```

### Bug Fix
```
fix(auth): 토큰 만료 시 자동 로그인 처리 오류 수정

문제:
- AuthInterceptor에서 401 응답 시 토큰 갱신이 작동하지 않음
- 사용자가 임의로 로그아웃되는 버그 발생

해결:
- TokenManager에 토큰 갱신 로직 추가
- Retrofit 재시도 메커니즘 구현
- 테스트 케이스 추가

Fixes #89
```

### Refactoring
```
refactor(benefit): BenefitViewModel 상태 관리 개선

분산된 UI 상태를 단일 StateFlow로 정리:
- 더 나은 반응성과 테스트 용이성
- Cold stream에서 Hot stream으로 변경
- 메모리 누수 방지

Breaking Change: BenefitViewModel 구독자는 StateFlow.collect() 사용 필요
```

### Chore
```
chore(network): Retrofit 타임아웃 설정 조정

- 연결 타임아웃: 30초로 통일
- 읽기 타임아웃: 30초로 통일
- 쓰기 타임아웃: 30초로 통일
- 콜 타임아웃: 30초로 통일
```

### Documentation
```
docs: README 업데이트 - Clean Architecture 설명 추가

- 레이어별 책임 문서화
- 의존성 방향 다이어그램 추가
- 새 개발자 온보딩 가이드 포함
```

## Branch Naming

Use kebab-case (lowercase with hyphens):

```
feature/<feature-name>       - New feature
  example: feature/benefit-search, feature/user-profile

fix/<issue-name>             - Bug fix
  example: fix/token-refresh, fix/network-timeout

chore/<task-name>            - Maintenance
  example: chore/update-gradle, chore/cleanup-imports

docs/<doc-name>              - Documentation
  example: docs/readme-update, docs/architecture-guide

refactor/<component-name>    - Code refactoring
  example: refactor/auth-layer, refactor/ui-binding
```

### Branch Lifecycle

1. **Create** from `develop` branch:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-feature
   ```

2. **Work** on your branch:
   ```bash
   git add app/src/main/java/com/umc/itday/feature/benefit/...
   git commit -m "feat(benefit): add search functionality"
   ```

3. **Keep updated** with develop:
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

4. **Push** when ready:
   ```bash
   git push -u origin feature/my-feature
   ```

5. **Create PR** and merge to develop

6. **Delete** after merge:
   ```bash
   git branch -d feature/my-feature
   git push origin --delete feature/my-feature
   ```

## Pull Request Guidelines

### PR Title Format
Follow the same convention as commits:
```
feat(benefit): Add membership search functionality
fix(auth): Resolve token refresh issue
chore: Update dependencies
```

### PR Description Template

```markdown
## Summary
Brief description of what this PR does.

## Related Issue
Fixes #123

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Breaking change
- [ ] Documentation update

## Changes
- Bullet point 1
- Bullet point 2
- Bullet point 3

## Testing
- [ ] Unit tests added
- [ ] Integration tests added
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] ktlintCheck passes
- [ ] detekt passes
- [ ] No new warnings
- [ ] Tests pass
- [ ] Documentation updated if needed
```

## Commit Best Practices

### Do
- **Commit frequently**: Small, atomic commits are easier to review and revert
- **Write clear messages**: Future you will thank you
- **Reference issues**: Use "Fixes #123" in footer
- **Test before commit**: Run `ktlintCheck` and tests locally
- **Group related changes**: One logical change per commit

### Don't
- **Mix concerns**: Don't combine feature changes with refactoring
- **Commit broken code**: Always test before committing
- **Use vague messages**: "fix stuff" or "updates" provide no context
- **Force push to shared branches**: Use `git rebase` instead
- **Commit sensitive data**: Keys, credentials, or tokens should NEVER be committed

## Pre-commit Checks

Before pushing, ensure:

```bash
# 1. Code style
./gradlew ktlintCheck

# 2. Static analysis
./gradlew detekt

# 3. Compilation
./gradlew compileDebugKotlin

# 4. Tests (if modified)
./gradlew testDebugUnitTest

# 5. View your commits
git log --oneline -5
```

## Rewriting History

### Amend Last Commit
```bash
# If commit not yet pushed
git add .
git commit --amend --no-edit
```

### Rebase Interactive (only local branches)
```bash
git rebase -i HEAD~3  # Last 3 commits
```

### Fix Accidental Push
```bash
# Never use --force on shared branches!
# Instead, create a new commit with fixes
git revert <commit-hash>
```

## Collaboration Rules

### For Feature Branches
- Always create a new branch from `develop`
- Push regularly to backup work
- Keep branch updated with `develop`
- Create PR before merging

### For Release/Main Branches
- Merge only through PR with approval
- No direct commits
- Use "Squash and merge" for clean history (optional)
- Tag releases with version (v1.0.0)

## Common Commands

```bash
# Show commit history with graph
git log --oneline --graph --all

# Show recent commits
git log --oneline -10

# Show specific file's history
git log -p app/src/main/.../MyFile.kt

# View current branch
git branch -v

# Switch to develop and pull latest
git checkout develop && git pull origin develop

# Fetch without merging
git fetch origin

# See unpushed commits
git log --oneline origin/develop..HEAD
```

## Issue Linking

In commit messages, reference related issues:

```
feat(benefit): Add search API integration

- Implement MembershipApi.searchMemberships()
- Add debounce logic to BenefitViewModel
- Display results in SearchScreen

Fixes #45         # GitHub will auto-close the issue
Related-To #46    # Link without closing
Depends-On #47    # This work depends on another
```

When the PR is merged, linked issues are automatically closed.

## Related Documentation

- [Coding Conventions](coding-conventions.md) - Code style to maintain in commits
- [Architecture](architecture.md) - Architecture patterns referenced in commits
- [Testing](testing.md) - Test additions referenced in commits

---

**Last Updated**: 2026-03-31
