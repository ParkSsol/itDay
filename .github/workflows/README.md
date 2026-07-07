# workflows

PR 생성부터 머지까지 전 과정을 자동화하는 GitHub Actions 워크플로우 모음입니다.

## 워크플로우 목록

| 파일 | 트리거 | 역할 |
|---|---|---|
| `code-style-check.yml` | PR 생성/업데이트 | Kotlin 코드 스타일 검사 |
| `test-coverage.yml` | PR 생성/업데이트 | 테스트 커버리지 검증 |
| `pr-line-limit.yml` | PR 생성/업데이트/라벨 변경 | PR 변경 줄 수 제한 |
| `pr-title-validate.yml` | PR 생성/수정 | PR 제목 `<type>: <subject>` 형식 검증 |
| `pr-title-labeler.yml` | PR 생성/수정 | PR 제목 기반 타입 라벨 자동 부착 |
| `pr-stacked-detector.yml` | PR 생성/수정/업데이트 | Stacked PR 자동 감지 및 라벨 부착 |
| `commit-message-lint.yml` | PR 생성/업데이트 | 커밋 메시지 Conventional Commits 검증 |
| `pr-labeler.yml` | PR 이벤트 전체 | 파일 경로 기반 라벨 + 상태 라벨 관리 |
| `issue-labeler.yml` | 이슈 생성/수정 | 이슈 상태/키워드 기반 라벨 부착 |
| `labels-sync.yml` | develop/main 브랜치 push / 수동 | GitHub 라벨 목록 동기화 |
| `pr-merge-status.yml` | PR 머지 완료 | 연결 이슈 상태 라벨 완료 처리 |

---

## 상세 설명

### code-style-check.yml
- **트리거**: PR opened / synchronize
- **도구**: 설정된 경우 `ktlint`와 `detekt` 실행, 미설정 상태에서는 안내 후 건너뜀
- **환경**: JDK 17, Gradle 캐시 적용 (빌드 속도 최적화)
- **범위**: `android/**` 변경이 있는 PR에서만 실행

### test-coverage.yml
- **트리거**: PR opened / synchronize
- **도구**: 단위 테스트는 항상 실행하고, 설정된 경우 JaCoCo (`jacocoDebugReport`)도 실행
- **초기 운영**: 테스트가 충분해지기 전까지 커버리지 리포트만 생성하고 임계값으로 실패시키지 않음
- **산출물**: `android/app/build/reports/jacoco/` HTML 리포트가 있으면 Artifact로 업로드

### pr-line-limit.yml
- **트리거**: PR opened / synchronize / labeled
- **기준**: 추가 줄 수 기준 (삭제 줄 제외), 테스트/문서/XML 리소스 제외
- **임계값**:
  - 180줄 이상 → ⚠️ 경고 댓글 + 리뷰어 추가 권장
  - 200줄 초과 → 🚫 실패 + PR 분리 요청 댓글
- **예외** (검사 스킵):
  - `design` 라벨 붙은 PR
  - `stacked-pr` 라벨 붙은 PR
  - `chore` 타입 PR (제목이 `chore:` 또는 `chore(scope):`로 시작)

### pr-title-validate.yml
- **트리거**: PR opened / edited
- **검증**: PR 제목이 프로젝트 템플릿 형식을 따르는지 확인
- **형식**: `<type>: <subject>` 또는 `<type>(<scope>): <subject>`
- **type**: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `perf`
- **Subject**: 전체 제목 기준 최대 72자, 마침표 없음
- **위반 시**: 🚫 실패 + 상세한 규칙 설명 댓글

### pr-title-labeler.yml
- **트리거**: PR opened / edited
- **동작**: PR 제목의 Conventional Commit `type`을 감지하여 라벨 자동 부착
- **Type → 라벨 매핑**:

| Type | 부착 라벨 |
|---|---|
| `feat` | `✨ feature` |
| `fix` | `🐞 bug` |
| `docs` | `📖 docs` |
| `refactor`, `perf` | `🔧 refactor` |
| `test` | `🧪 test` |
| `chore` | `🧹 chore` |
| `ci` | `⚙️ build/gradle` |

### pr-stacked-detector.yml
- **트리거**: PR opened / edited
- **동작**: Stacked PR 패턴을 자동으로 감지하고 `📚 stacked-pr` 라벨 부착
- **감지 패턴**:
  1. **파트 형식**: PR 제목에 `[1/3]`, `[2/3]` 등이 있는 경우
  2. **키워드**: PR 제목/본문에 `stacked`, `depends on`, `depends-on`, `stack of` 등이 있는 경우
- **자동 댓글**: 감지 사유와 함께 알림 댓글 자동 작성
- **라인 제한 스킵**: 자동으로 `pr-line-limit.yml` 체크 대상 제외

### commit-message-lint.yml
- **트리거**: PR opened / synchronize
- **검증**: PR의 모든 커밋 메시지가 Conventional Commits 형식을 따르는지 확인
- **검증 항목**:
  1. 형식: `<type>(<scope>): <subject>`
  2. Subject 길이: 최대 50자
- **Type, Scope**: 커밋 메시지용 Conventional Commits 규칙 적용
- **위반 시**: 🚫 실패 + 문제 있는 커밋 목록 + 규칙 설명 댓글

### pr-labeler.yml
- **트리거**: PR opened / synchronize / reopened / edited / review_requested / review_request_removed
- **동작 1**: `.github/labels/labeler.yml` 기준으로 변경 파일 경로에 따라 레이어 라벨 자동 부착
- **동작 2**: 상태 라벨 자동 전환

| 이벤트 | 라벨 변경 |
|---|---|
| PR 오픈 / 재오픈 | `🏃 status: in progress` 부착 |
| 리뷰 요청 | `🔍 status: code review` 부착 |
| 리뷰 요청 해제 | `🔍 status: code review` 제거 |

### issue-labeler.yml
- **트리거**: 이슈 opened / edited
- **동작 1**: 모든 신규 이슈에 `📌 status: todo` 자동 부착
- **동작 2**: `.github/labels/issue-labeler.yml` 의 키워드 목록 기준으로 제목+본문 스캔 후 타입 라벨 부착

### labels-sync.yml
- **트리거**: `develop` 또는 `main` 브랜치에 `.github/labels/labels.yml` 변경 push 또는 수동 실행
- **동작**: `crazy-max/ghaction-github-labeler` 를 사용해 `labels.yml` 정의 기준으로 GitHub 라벨을 동기화
- **주의**: `skip-delete: true` 설정으로 파일에 없는 기존 라벨은 삭제하지 않음

### pr-merge-status.yml
- **트리거**: PR closed (머지된 경우만 실행)
- **동작**: PR 본문에서 `#이슈번호` 패턴을 추출하여 연결된 이슈에 아래 처리 수행
  1. `🏃 status: in progress` 라벨 제거
  2. `✅ status: done` 라벨 부착

---

## 자동화 전체 흐름

```
이슈 생성
  └─ issue-labeler     → 📌 status: todo + 타입 라벨 부착

PR 생성 (제목: feat: 혜택 검색 기능, 본문: Closes #이슈번호)
  ├─ pr-title-validate     → ✅ 제목 형식 검증
  ├─ pr-title-labeler      → ✨ feature 라벨 부착
  ├─ pr-stacked-detector   → Stacked PR 패턴 감지 시 📚 stacked-pr 라벨
  ├─ commit-message-lint   → 커밋 메시지 형식 검증
  ├─ pr-labeler            → 🏃 status: in progress + 레이어 라벨 부착
  ├─ code-style-check      → 설정된 ktlint/detekt 검사
  ├─ test-coverage         → 테스트 실행 + 설정된 경우 커버리지 리포트 생성
  └─ pr-line-limit         → 200줄 초과 시 실패 (chore/design/stacked-pr은 스킵)

리뷰 요청
  └─ pr-labeler            → 🔍 status: code review 부착

PR 머지
  └─ pr-merge-status       → 연결 이슈에 ✅ status: done 부착
```

## 커밋/PR 컨벤션 규칙

자세한 규칙은 [commit-conventions.md](../../.agents/skills/itday-android-workflow/references/rules/commit-conventions.md) 참고
