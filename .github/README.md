# .github

이 디렉토리는 GitHub 협업 자동화 설정 전체를 관리합니다.

## 디렉토리 구조

```
.github/
├── ISSUE_TEMPLATE/       # 이슈 유형별 작성 양식
├── labels/               # 라벨 정의 및 자동 매핑 설정
├── workflows/            # GitHub Actions CI/CD 워크플로우
└── pull_request_template.md  # PR 작성 양식
```

## pull_request_template.md

PR 생성 시 자동으로 채워지는 본문 양식입니다.

| 섹션 | 설명 |
|---|---|
| PR 개요 | 작업 내용 한 줄 요약 |
| 작업 내용 | 기능 / UI / 버그 수정 체크리스트 |
| 관련 이슈 | 연결할 이슈 번호 (`#12`) |
| 화면 캡처 | UI 변경 시 스크린샷 첨부 |
| 체크 사항 | 빌드 / 기능 / 로그 / 컨벤션 확인 |
| 기타 참고 | 리뷰어에게 전달할 추가 내용 |

> PR 본문에 `Closes #이슈번호`, `Fixes #이슈번호`, `Resolves #이슈번호` 형식으로 연결하면, PR 머지 시 해당 이슈에 `✅ status: done` 라벨이 자동으로 부착됩니다.
