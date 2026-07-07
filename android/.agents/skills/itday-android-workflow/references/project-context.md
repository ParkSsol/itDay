# Codex Project Context — It-Day

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Package Responsibilities](#package-responsibilities)
- [Detailed Rules](#detailed-rules)
- [Build Configuration](#build-configuration)
- [Test Structure](#test-structure)
- [Commit Convention](#commit-convention)
- [Code Style Checking](#code-style-checking)
- [Key Project Files](#key-project-files)
- [Next Steps](#next-steps)

## Project Overview

**It-Day** is a location-based Android application that helps users find carrier membership benefits, access membership barcodes, and review benefit usage.

- **Package**: read the current `namespace` and `applicationId` from `android/app/build.gradle.kts`; do not infer them from examples
- **Target API**: API 36 (Android 16)
- **Min API**: API 24 (Android 7.0)
- **Language**: Kotlin
- **Build System**: Gradle 8.x + KSP

## Architecture

The proposed target is **Clean Architecture** with feature-first packages. It is not yet implemented. Match the current codebase and introduce structure incrementally after team approval.

### Top-level Package Structure

```
com.itday/
├── app/              # App entry point, DI, navigation
├── core/             # Common utilities, network, UI components
└── feature/          # Business logic modules
    ├── auth/         # Authentication/Login
    ├── onboarding/   # Carrier, grade, favorite brands, barcode registration
    ├── home/         # Benefits and membership barcode
    ├── character/    # Character and points
    ├── report/       # Benefit usage report
    ├── map/          # Nearby partner stores
    └── setting/      # User and membership settings
```

### Layer Structure for Each Feature

Features with data access or business rules use the 3-layer Clean Architecture pattern. Simple UI-only features do not create empty layers.

```
feature/[featureName]/
├── data/
│   ├── api/                    # Retrofit API interfaces
│   ├── repository/             # Repository implementations (data layer)
│   ├── model/                  # API request/response DTOs
│   ├── local/                  # Local data source when required
│   └── service/                # Feature services when required
│
├── domain/
│   ├── repository/             # Repository interfaces (abstraction)
│   ├── model/                  # Domain models (UI-agnostic pure models)
│   └── usecase/                # Use cases (business logic)
│
└── presentation/
    ├── ui/                     # Compose screens and components
    ├── viewmodel/              # ViewModels
    └── navigation/             # Feature routes when required
```

## Technology Stack

The lists below describe selected directions and proposals. Before generating imports or configuration, verify that each dependency is present in `android/gradle/libs.versions.toml` and the relevant Gradle build file.

### Core Dependencies
- **Kotlin**: Android Studio-generated compatible version
- **AndroidX**: Core libraries (lifecycle, Navigation Compose)
- **Coroutines**: Async operations
- **StateFlow**: UI state management
- **Jetpack Compose**: Declarative UI

### Proposed Network & API
- **Retrofit**: HTTP client
- **OkHttp 4**: HTTP interceptors, logging
- **Kotlin Serialization**: JSON serialization
- **AuthInterceptor**: Automatic JWT token injection

### Dependency Injection
- **Hilt**: Proposed compile-time dependency injection; use only after it is configured
- Module location: `app/di/`
- Scope: Primarily `SingletonComponent::class`

### Data & Storage
- **DataStore**: Lightweight preferences when required
- **Room**: Deferred until structured offline storage is required

### Proposed Code Quality
- **ktlint**: Kotlin code style checking
- **detekt**: Static code analysis
- **JaCoCo**: Proposed test coverage measurement; use only after it is configured

### Selected UI Direction
- **Navigation Compose**: Screen navigation
- **Material Design 3**: UI components
- **Coil Compose**: Image loading
- **Kakao Map SDK**: Map display

## Package Responsibilities

### `app/`
- **ItDayApp**: Proposed application class if app-level initialization becomes necessary
- **MainActivity**: Navigation host, root container
- **di/**: Dependency bindings only after a DI framework is selected and configured
  - `NetworkModule`: Retrofit, OkHttp client, all API services
  - `AuthModule`: Authentication-related dependencies
  - `SettingModule`: User info-related dependencies
  - `LocationModule`: Location-related dependencies

### `core/`
- **network/**: BaseResponse, AuthInterceptor, common network classes
- **designsystem/**: Common Compose components, theme, and design tokens
- **util/**: Common utility functions
- **location/**: Location permissions, GPS providers
- **permission/**: Permission management
- **notification/**: Notification helpers

### `feature/[name]/`
- **data/**: All network/database access logic
- **domain/**: Business logic, Repository interfaces
- **presentation/**: Compose UI, ViewModels, UI state, and events

## Detailed Rules

For detailed rules, refer to the files in `references/rules/`:

- [`architecture.md`](rules/architecture.md) — Clean Architecture design and patterns
- [`coding-conventions.md`](rules/coding-conventions.md) — Kotlin coding conventions
- [`commit-conventions.md`](rules/commit-conventions.md) — Git commit and branch conventions
- [`hilt-di.md`](rules/hilt-di.md) — Hilt dependency injection guide
- [`api-integration.md`](rules/api-integration.md) — Retrofit and network layer guide
- [`testing.md`](rules/testing.md) — Test writing rules
- [`ui-patterns.md`](rules/ui-patterns.md) — UI layer patterns and ViewModel guide

## Build Configuration

### BuildConfig Fields
The project reads the following fields from `gradle.properties`:

```properties
SERVER_BASE_URL=<server-url>
KAKAO_NATIVE_APP_KEY=<your-native-app-key>
```

### Network Timeouts
All HTTP requests: **30 seconds** (connection, read, write, call timeouts are the same)

## Test Structure

```
app/src/
├── test/              # Unit tests
│   └── java/.../      # JUnit tests
└── androidTest/       # Instrumentation tests
    └── java/.../      # AndroidJUnit tests
```

## Commit Convention

This project uses **Conventional Commits** format in **Korean**:

```
<type>(<scope>): <subject>

<body>
```

**Types**: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `perf`
**Scope**: Feature name (e.g., `auth`, `benefit`, `network`)
**Subject**: Brief description (max 50 chars)

→ For comprehensive commit examples: [commit-conventions.md — Examples](rules/commit-conventions.md#examples)

## Code Style Checking

```bash
./gradlew ktlintCheck  # Kotlin format checking
./gradlew detekt       # Static analysis
./gradlew compileDebugKotlin  # Compilation checking
```

## Key Project Files

- `build.gradle.kts` — Root build configuration
- `app/build.gradle.kts` — App module configuration (Hilt, KSP, plugins)
- `settings.gradle.kts` — Module composition
- `gradle.properties` — Global configuration (excluded from git)
- `.github/` — GitHub Actions, issue/PR templates, label config
- `android/.agents/skills/itday-android-workflow/` — Codex workflow and detailed references

## Next Steps

When adding a new feature:

1. Read `references/rules/` documentation to understand architecture patterns
2. Refer to an existing It-Day feature and copy only the layers the new feature needs
3. Implement in order: data → domain → presentation
4. Add dependency bindings only when the configured DI approach requires them
5. Run code style checks (`ktlintCheck`) and tests before committing

---

**Last Updated**: 2026-03-31
**Maintained By**: It-Day Team
