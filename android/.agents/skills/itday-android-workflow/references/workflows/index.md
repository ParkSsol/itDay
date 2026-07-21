> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

# 슬래시 커맨드 (스킬) 폴더

이 폴더에는 Codex에서 재사용할 **작업 워크플로** 정의 파일이 있습니다.

## 사용법

`/커맨드명 인자` 형태로 호출합니다. 파일의 내용이 프롬프트로 확장되어 Claude에게 전달됩니다.

## 커맨드 목록

| 커맨드 | 용도 | 에이전트 체인 |
|--------|------|--------------|
| `/feature` | 새 기능 전체 구현 | planner → coder → refactorer → reviewer |
| `/bug` | 버그 수정 | analyzer → coder → reviewer |
| `/pr` | PR 자동 생성 | 단독 (git + gh 명령) |
| `/api` | Retrofit 엔드포인트 스캐폴딩 | 단독 (coder 패턴 참조) |
| `/test` | 유닛 테스트 자동 생성 | 단독 |
| `/issue` | GitHub 이슈 생성 | github-issue-helper |
| `/workflow` | CI 워크플로우 상태 확인·재실행 | 단독 (gh run 명령) |

---

> **참고**: `/issue`·`/workflow`는 GitHub CLI(`gh`)가 설치·인증된 상태에서만 동작합니다.
> 미설치 시: https://cli.github.com | 인증: `gh auth login`

---

## 상세 설명

### `/feature` — 기능 구현 파이프라인

새 기능을 플래너 → 코더 → 리팩터 → 리뷰어 순으로 구현합니다.

```
/feature 약국 즐겨찾기. 추가/제거 가능하고 로컬 DB에 저장
```

---

### `/bug` — 버그 수정 파이프라인

버그 설명을 받아 원인 분석 → 최소 수정 → 회귀 리뷰를 수행합니다.

```
/bug 약국 목록 화면에서 앱이 크래시남. 스크롤 내리면 NPE 발생
```

---

### `/pr` — PR 자동 생성

현재 브랜치의 변경사항을 읽고 프로젝트 PR 템플릿에 맞게 자동으로 PR을 생성합니다.
- 200줄 초과 시 경고 후 확인 요청 (프로젝트 규칙)
- 이슈 번호를 인자로 전달하면 `Closes #번호` 자동 추가

```
/pr
/pr 42
```

---

### `/api` — API 엔드포인트 스캐폴딩

REST 엔드포인트 설명을 받아 클린 아키텍처 5개 파일을 한 번에 생성합니다:
Response Model → API Interface → Repository Interface → Repository Impl → Hilt Module

```
/api GET /stores/nearby?lat={lat}&lng={lng} 주변 매장 목록 반환
```

---

### `/test` — 유닛 테스트 생성

파일 경로 또는 클래스명을 받아 ViewModel / Repository / UseCase 유닛 테스트를 작성합니다.
Happy path, Error path, Edge case를 모두 커버합니다.

```
/test feature/benefit/presentation/BenefitViewModel.kt
/test SettingRepositoryImpl
```

---

### `/issue` — GitHub 이슈 생성

이슈 타입과 설명을 받아 템플릿을 자동으로 채워 GitHub 이슈를 생성합니다.
- 타입: `bug`/`버그`, `feature`/`기능`, `chore`/`보수`, `refactor`/`리팩`, `design`/`디자인`
- 생성 전 타이틀·본문 미리보기 확인 단계 포함
- label(`🐞 bug` 등), `📌 status: todo` 자동 적용

```
/issue bug 약국 목록 화면에서 스크롤 시 NPE 발생
/issue feature 즐겨찾기 기능 추가
/issue chore gradle 버전 업그레이드
```

---

### `/workflow` — CI 워크플로우 관리

GitHub Actions 워크플로우 상태 확인, 로그 조회, 실패 재실행을 수행합니다.

| 서브커맨드 | 용도 |
|-----------|------|
| `status` (기본) | 현재 브랜치의 최근 10개 실행 결과 표시 |
| `check` | HEAD 커밋의 모든 워크플로우 결과 집계 (실패 로그 포함) |
| `rerun <id\|name>` | 실패한 단계만 재실행 + 완료까지 폴링 |
| `list` | 저장소의 전체 워크플로우 목록 |

```
/workflow
/workflow check
/workflow rerun test-coverage
/workflow rerun 12344
/workflow list
```

---

## 커맨드 추가 방법

```markdown
---
name: 커맨드-이름
description: 커맨드 역할 한 줄 설명
---

슬래시 커맨드 실행 시 Claude에게 전달될 프롬프트...
```

> 역할별 작업 지침은 [`../roles/index.md`](../roles/index.md)를 참고하세요.
