# labels

GitHub 라벨의 정의, PR 파일 경로 기반 매핑, 이슈 키워드 기반 매핑 설정을 관리합니다.

## 파일 목록

| 파일 | 역할 |
|---|---|
| `labels.yml` | 프로젝트에서 사용하는 모든 라벨 목록 정의 |
| `labeler.yml` | PR 변경 파일 경로 기준 라벨 자동 매핑 규칙 |
| `issue-labeler.yml` | 이슈 본문/제목 키워드 기준 라벨 자동 매핑 규칙 |

---

## labels.yml

프로젝트 전체 라벨을 4가지 그룹으로 정의합니다.
`labels-sync.yml` 워크플로우가 이 파일 기준으로 GitHub 라벨을 동기화합니다.

### 기본 타입
| 라벨 | 색상 | 설명 |
|---|---|---|
| `🐞 bug` | 빨강 | 버그/크래시/오동작 |
| `✨ feature` | 하늘 | 새 기능 추가/개선 |
| `🔧 refactor` | 보라 | 코드 리팩토링 |
| `📖 docs` | 파랑 | 문서/주석 |
| `🧹 chore` | 노랑 | 설정/빌드/기타 작업 |
| `💡 design` | 분홍 | 설계 및 기술 논의 |

### 우선순위
| 라벨 | 설명 |
|---|---|
| `🚨 P0` | 크리티컬 — 즉시 대응 필요 |
| `🔥 P1` | 높음 |
| `⚠️ P2` | 보통 |
| `🔽 P3` | 낮음 |

### 안드로이드 레이어
| 라벨 | 해당 경로/대상 |
|---|---|
| `📱 ui/presentation` | Compose Screen / Component |
| `🧠 domain/usecase` | UseCase / Repository 인터페이스 / domain 패키지 |
| `🗂 data/repository` | Repository 구현체 / DTO |
| `🌐 network/api` | API / 네트워크 |
| `💾 local/db` | Room / DAO / 로컬 저장소 |
| `🧪 test` | 테스트 코드 |
| `⚙️ build/gradle` | 빌드 / Gradle 설정 |

### 상태 흐름
```
📌 status: todo
  → 🏃 status: in progress   (PR 오픈 시 자동 부착)
  → 🔍 status: code review   (리뷰 요청 시 자동 부착)
  → ✅ status: done           (PR 머지 시 자동 부착)
  (🚧 status: blocked 는 수동 부착)
```

---

## labeler.yml

PR에서 변경된 파일 경로를 기준으로 안드로이드 레이어 라벨을 자동 부착합니다.
`pr-labeler.yml` 워크플로우가 이 파일을 참조합니다.

| 라벨 | 감지 경로 |
|---|---|
| `📱 ui/presentation` | `**/ui/**`, `**/presentation/**`, `**/designsystem/**` |
| `🧠 domain/usecase` | `**/*UseCase*`, `**/*Repository*`, `**/domain/**` |
| `🗂 data/repository` | `**/data/**`, `**/*Dto*` |
| `🌐 network/api` | `**/network/**`, `**/api/**` |
| `💾 local/db` | `**/local/**`, `**/*Dao*` |
| `🧪 test` | `**/test/**`, `**/androidTest/**` |
| `⚙️ build/gradle` | `build.gradle*`, `settings.gradle*`, `gradle.properties` |

---

## issue-labeler.yml

이슈 제목 + 본문에서 키워드를 감지하여 타입 라벨을 자동 부착합니다.
`issue-labeler.yml` 워크플로우가 이 파일의 키워드 목록을 참조합니다.

| 라벨 | 감지 키워드 |
|---|---|
| `🐞 bug` | 버그, 크래시, 오류, crash, exception |
| `✨ feature` | 기능, 추가, feature, 새로운 |
| `🔧 refactor` | 리팩토링, refactor, 개선 |
| `📖 docs` | 문서, docs, readme |
| `🧹 chore` | 설정, chore, 빌드, gradle |
| `💡 design` | 설계, 구조, architecture, 논의 |
