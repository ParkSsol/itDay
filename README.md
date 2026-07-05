# It-Day

<p align="center">
  <img src="assets/logo.png" width="120" alt="It-Day 로고" />
</p>

## 프로젝트 소개

**It-Day(잇데이)**는 파편화된 통신사 멤버십 및 제휴 혜택을 사용자가 쉽게 찾아보고 바로 꺼낼 수 있도록 돕는 위치 기반 혜택 알림 서비스입니다.

사용자는 현재 위치를 기준으로 주변 제휴 매장을 확인하고, 홈 화면에서 원탭으로 통신사 멤버십 바코드를 사용할 수 있습니다. 또한 혜택 사용 기록을 기반으로 리포트를 확인하며, 포인트 적립 및 차감을 통해 기프티콘을 발급받을 수 있습니다.

## 팀원 소개 및 역할 분담

| 팀원 | 파트 | 역할 |
| --- | --- | --- |
| 동/김동찬 | Spring | 백엔드 API 개발 |
| 세리/박솔 | Spring | 백엔드 API 개발 |
| 라바/조상혁 | Spring Boot | 백엔드 API 개발 |
| 진/김예진 | Design | UI/UX 디자인 및 프로토타입 제작 |
| 찬/박연찬 | PM | 기획, 일정 관리, 팀 커뮤니케이션 |
| 망고/강민교 | Android | Android 앱 개발, 화면 구현, API 연동 |
| 심씨/심규석 | Android | Android 앱 개발, 화면 구현, API 연동 |

## 기술 스택

### Android

| 구분 | 기술 |
| --- | --- |
| Language | Kotlin |
| Architecture | MVVM |
| 주요 라이브러리 | Jetpack Compose, Jetpack Navigation |
| 위치/지도 | Android Location, 지도 API |
| 개발 환경 | Android Studio, Gradle |

## 프로젝트 폴더 구조

Android 프로젝트 생성 후 실제 구조에 맞춰 업데이트 예정입니다.

```text
itDay/
|-- README.md
|-- app/
|-- gradle/
|-- build.gradle.kts
`-- settings.gradle.kts
```

## 컨벤션 문서

프로젝트 협업 시 아래의 브랜치, 커밋 메시지, Pull Request, 코드 및 패키지 규칙을 따릅니다.

### Branch Naming Convention

브랜치는 작업 종류와 내용을 확인할 수 있도록 다음 형식으로 작성합니다.

```text
<type>/<description>
```

| Type | 용도 | 예시 |
| --- | --- | --- |
| `feature` | 새로운 기능 개발 | `feature/login-screen` |
| `fix` | 버그 수정 | `fix/map-location-error` |
| `docs` | 문서 작성 및 수정 | `docs/readme` |
| `refactor` | 기능 변경 없는 코드 개선 | `refactor/home-viewmodel` |
| `test` | 테스트 코드 작성 및 수정 | `test/membership-repository` |
| `chore` | 빌드 설정 등 기타 작업 | `chore/gradle-setting` |

- 브랜치 이름은 영문 소문자로 작성하고, 단어는 하이픈(`-`)으로 구분합니다.

### Commit Message Convention

커밋 메시지는 작업 종류와 내용을 확인할 수 있도록 다음 형식으로 작성합니다.

```text
<type>: <subject>
```

| Type | 용도 | 예시 |
| --- | --- | --- |
| `feat` | 새로운 기능 추가 | `feat: 카카오 로그인 화면 구현` |
| `fix` | 버그 수정 | `fix: 지도 위치 갱신 오류 수정` |
| `docs` | 문서 작성 및 수정 | `docs: README 화면 플로우 추가` |
| `refactor` | 기능 변경 없는 코드 개선 | `refactor: 홈 화면 상태 처리 분리` |
| `test` | 테스트 코드 작성 및 수정 | `test: 멤버십 저장소 테스트 추가` |
| `chore` | 빌드 설정 등 기타 작업 | `chore: Gradle 의존성 설정` |

- 제목은 한글로 간결하고 명확하게 작성하며 문장 끝에 마침표를 사용하지 않습니다.
- 하나의 커밋에는 하나의 논리적인 변경사항만 포함합니다.

### Pull Request Convention

- PR 제목은 커밋 메시지와 동일하게 `<type>: <subject>` 형식으로 작성합니다.
- PR 리뷰는 GitHub Actions 기반의 AI 코드 리뷰를 사용합니다.
- AI 리뷰 결과를 확인하고 필요한 수정사항을 반영한 후 병합합니다.

### Kotlin Naming Convention

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| Class | PascalCase | `HomeViewModel` |
| Interface | PascalCase | `MembershipRepository` |
| Enum | PascalCase | `CarrierType` |
| Function | lowerCamelCase | `loadNearbyStores()` |
| Variable | lowerCamelCase | `selectedCarrier` |
| Parameter | lowerCamelCase | `membershipGrade` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_RADIUS_METER` |
| Package | lowercase | `com.example.itday` |

### Compose Naming Convention

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| Screen | PascalCase | `HomeScreen` |
| ViewModel | PascalCase | `HomeViewModel` |
| UI State | PascalCase | `HomeUiState` |
| Event | PascalCase | `HomeEvent` |
| Preview | PascalCase | `HomeScreenPreview` |

### 패키지 구조 규칙

프로젝트는 `ui`와 `data`를 분리하는 계층형 구조를 기본으로 하며, `ui/screen` 내부는 기능별로 구성합니다.

```text
com.example.itday/
|-- MainActivity.kt
|-- data/
|   |-- local/                  # 기기 내부 데이터 저장 및 접근
|   |-- model/                  # API 응답 및 앱 데이터 모델
|   |-- remote/                 # 서버 및 외부 API 통신
|   `-- repository/             # 데이터 접근 로직 관리
`-- ui/
    |-- component/              # 여러 화면에서 사용하는 공통 Compose UI
    |-- navigation/             # 화면 경로와 하단 탭 관리
    |-- screen/
    |   |-- login/              # 로그인
    |   |-- onboarding/         # 초기 설정
    |   |-- home/               # 홈 및 바코드
    |   |-- character/          # 캐릭터
    |   |-- report/             # 사용 리포트
    |   |-- map/                # 지도 및 주변 매장
    |   `-- setting/            # 설정
    `-- theme/                  # 색상, 글꼴 및 Compose 테마
```

- 화면 코드는 `ui/screen` 아래에서 기능별 패키지로 구분합니다.
- `Screen`, `ViewModel`, `UiState` 등 하나의 화면과 관련된 코드는 같은 기능 패키지에 배치합니다.
- 여러 화면에서 사용하는 UI는 `ui/component`에, 특정 화면에서만 사용하는 UI는 해당 기능의 `component`에 배치합니다.
- 서버, 지도 API 등 외부 데이터는 `data/remote`, 기기 내부 데이터는 `data/local`에서 관리합니다.
- 데이터 접근 로직은 `data/repository`를 통해 UI 계층에 제공합니다.

## 빌드 및 실행 방법

Android Studio를 기준으로 앱을 빌드하고 실행합니다.

### 실행 환경

| 항목 | 내용 |
| --- | --- |
| IDE | Android Studio |
| 실행 기기 | Medium Phone API 36.1 |
| Android Version | Android 16.0 |
| ABI | x86_64 |

### 실행 방법

1. Android Studio에서 프로젝트를 엽니다.
2. Gradle Sync가 완료될 때까지 기다립니다.
3. 실행 기기로 `Medium Phone API 36.1` 에뮬레이터를 선택합니다.
4. 상단의 Run 버튼을 눌러 앱을 실행합니다.

## 화면 목록 & 플로우

### 화면 목록

| 화면 이름 | 스크린 ID | 진입 경로 | 담당자 |
| --- | --- | --- | --- |
| 스플래시 화면 | SplashScreen | 앱 최초 실행 | Android 파트 공동 |
| 네트워크 오류 화면 | NetworkErrorScreen | 앱 실행 중 네트워크 연결 실패 | Android 파트 공동 |
| 로그인 화면 | LoginScreen | 스플래시 이후 | Android 파트 공동 |
| 카카오 로그인 오류 화면 | KakaoLoginErrorScreen | 카카오 로그인 실패 시 | Android 파트 공동 |
| 이메일 추가 입력 화면 | EmailInputScreen | 카카오 계정 이메일 미동의 시 | Android 파트 공동 |
| 회원가입 화면 | SignUpScreen | 로그인 화면 > 회원가입 | Android 파트 공동 |
| 탈퇴 대기 화면 | WithdrawPendingScreen | 탈퇴 대기 계정 로그인 시 | Android 파트 공동 |
| 게스트 안내 화면 | GuestGuideScreen | 로그인 화면 > 게스트로 둘러보기 | Android 파트 공동 |
| 약관 동의 화면 | TermsAgreementScreen | 회원가입 이후 | Android 파트 공동 |
| 위치 권한 요청 화면 | LocationPermissionScreen | 약관 동의 이후 | Android 파트 공동 |
| 통신사 선택 화면 | CarrierSelectScreen | 온보딩 중 통신사 선택 단계 | Android 파트 공동 |
| 멤버십 등급 선택 화면 | MembershipGradeScreen | 통신사 선택 이후 | Android 파트 공동 |
| 선호 브랜드 선택 화면 | FavoriteBrandScreen | 멤버십 등급 선택 이후 | Android 파트 공동 |
| 멤버십 카드 등록 방법 화면 | CardRegisterMethodScreen | 회원 온보딩 중 바코드 등록 단계 | Android 파트 공동 |
| 바코드 번호 입력 화면 | BarcodeInputScreen | 카드 등록 방법 > 직접 입력 | Android 파트 공동 |
| 중복 바코드 오류 팝업 | DuplicateBarcodeDialog | 바코드 등록 중 중복 번호 감지 | Android 파트 공동 |
| 등록 완료 화면 | RegisterCompleteScreen | 온보딩 완료 후 | Android 파트 공동 |
| 메인 화면 | MainScreen | 온보딩 완료 후 | Android 파트 공동 |
| 홈 화면 | HomeScreen | 하단 탭 > 홈 | Android 파트 공동 |
| 캐릭터 화면 | CharacterScreen | 하단 탭 > 캐릭터 | Android 파트 공동 |
| 리포트 화면 | ReportScreen | 하단 탭 > 리포트 | Android 파트 공동 |
| 지도 화면 | MapScreen | 하단 탭 > 지도 | Android 파트 공동 |
| 설정 화면 | SettingScreen | 하단 탭 > 설정 | Android 파트 공동 |
| 혜택 비교 화면 | BenefitCompareScreen | 홈 > 통신사별 혜택 비교 | Android 파트 공동 |
| 바코드 화면 | BarcodeScreen | 홈 > 바코드 또는 근처 매장 팝업 | Android 파트 공동 |
| 바코드 사용 확인 팝업 | BarcodeUseConfirmDialog | 바코드 표시 후 사용 여부 확인 | Android 파트 공동 |
| 제휴 매장 카테고리 화면 | StoreCategoryScreen | 홈 > 주변 매장 또는 카테고리 | Android 파트 공동 |
| 제휴 매장 목록 화면 | StoreListScreen | 제휴 매장 카테고리 선택 후 | Android 파트 공동 |
| 매장 상세 화면 | StoreDetailScreen | 매장 목록 또는 지도 > 매장 선택 | Android 파트 공동 |
| 프로필 설정 화면 | ProfileSettingScreen | 설정 > 프로필 설정 | Android 파트 공동 |
| 멤버십 정보 화면 | MembershipInfoScreen | 설정 > 멤버십 정보 | Android 파트 공동 |
| 알림 설정 화면 | NotificationSettingScreen | 설정 > 알림 설정 | Android 파트 공동 |
| 개인정보 및 보안 화면 | PrivacySecurityScreen | 설정 > 개인정보 및 보안 | Android 파트 공동 |
| 고객센터 화면 | CustomerCenterScreen | 설정 > 고객센터 | Android 파트 공동 |
| 로그아웃 확인 팝업 | LogoutConfirmDialog | 설정 > 로그아웃 | Android 파트 공동 |

### 사용자 플로우 (메인 진입 전)

```text
SplashScreen
  |
  v
LoginScreen
  |-- KakaoLoginErrorScreen
  |-- EmailInputScreen
  |-- WithdrawPendingScreen
  |
  |-- SignUpScreen
  |   `-- TermsAgreementScreen
  |       `-- LocationPermissionScreen
  |           `-- CarrierSelectScreen
  |               `-- MembershipGradeScreen
  |                   `-- FavoriteBrandScreen
  |                       `-- CardRegisterMethodScreen
  |                           `-- BarcodeInputScreen
  |                               |-- DuplicateBarcodeDialog
  |                               `-- RegisterCompleteScreen
  |                                   `-- MainScreen
  |
  `-- GuestGuideScreen
      `-- CarrierSelectScreen
          `-- MembershipGradeScreen
              `-- FavoriteBrandScreen
                  `-- MainScreen
```

### 메인 진입 후 플로우

앱 진입 및 온보딩이 완료되면 `MainScreen`으로 이동하며, 기본 진입 화면은 `HomeScreen`입니다. `MainScreen`은 하단 탭 네비게이션을 통해 홈, 캐릭터, 리포트, 지도, 설정 5개의 주요 화면으로 이동합니다.

```text
MainScreen
  |
  v
HomeScreen (기본 진입 화면)

Bottom Navigation
  |-- HomeScreen
  |   |-- BarcodeScreen
  |   |   `-- BarcodeUseConfirmDialog
  |   |-- BenefitCompareScreen
  |   `-- StoreCategoryScreen
  |       `-- StoreListScreen
  |           `-- StoreDetailScreen
  |
  |-- CharacterScreen
  |
  |-- ReportScreen
  |
  |-- MapScreen
  |   `-- StoreDetailScreen
  |
  `-- SettingScreen
      |-- ProfileSettingScreen
      |-- MembershipInfoScreen
      |-- NotificationSettingScreen
      |-- PrivacySecurityScreen
      |-- CustomerCenterScreen
      `-- LogoutConfirmDialog
```
