# ISSUE_TEMPLATE

이슈 생성 시 작업 유형에 맞는 양식을 선택할 수 있도록 5종의 템플릿을 제공합니다.
각 템플릿은 GitHub 이슈 생성 화면에서 자동으로 선택지로 표시됩니다.

## 템플릿 목록

| 파일 | 이슈 유형 | 제목 접두사 | 자동 부착 라벨 |
|---|---|---|---|
| `bug.md` | 버그 제보 및 수정 | `[Bug]` | `bug` |
| `feature.md` | 신규 기능 개발 | `[Feature]` | `feature` |
| `refactor.md` | 코드 구조 개선 | `[Refactor]` | `refactor` |
| `chore.md` | 설정 / 빌드 / 기타 | `[Chore]` | `chore` |
| `design.md` | 설계 및 기술 논의 | `[Design]` | `design` |

## 각 템플릿 구성

### bug.md
- 문제 상황 / 재현 방법 / 예상 동작 / 실제 동작
- 환경 정보 (OS, Device, App Version)
- 원인 분석 → 수정 → 회귀 테스트 체크리스트

### feature.md
- 기능 개요 및 목표 (사용자 가치 / 해결 문제)
- 설계 요약 (UI / Domain / Data 레이어)
- Tasks 체크리스트 (UI → ViewModel → UseCase → Repository → 예외 처리)
- Definition of Done (DoD)

### refactor.md
- 개선 내용 및 목표 (가독성 / 유지보수성 / 성능)
- 영향 범위 (변경 모듈 / 기존 기능 영향)
- 검증 방법 및 완료 조건

### chore.md
- 작업 내용 및 목적
- 빌드 / 런타임 영향 범위
- 빌드 성공 및 기존 기능 정상 동작 확인

### design.md
- 논의 주제 및 목적
- 옵션 비교 (옵션 A vs 옵션 B)
- 트레이드오프 / 리스크 분석
- 결론 업데이트 (논의 후 작성)

## 이슈 생성 흐름

```
이슈 생성
  → 템플릿 선택 (유형 결정)
  → 제목에 접두사 포함 ex) [Bug] 로그인 버튼 클릭 시 앱 크래시
  → issue-labeler 워크플로우가 키워드/접두사 기반으로 라벨 자동 부착
  → 📌 status: todo 라벨 자동 부착
```
