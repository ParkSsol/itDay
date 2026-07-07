> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

# Architecture Rules — Clean Architecture & Feature-based Design

## Table of Contents

- [Overview](#overview)
- [Core Principles](#core-principles)
- [Layer Structure in Detail](#layer-structure-in-detail)
  - [Data Layer](#data-layer)
  - [Domain Layer](#domain-layer)
  - [Presentation Layer](#presentation-layer)
- [Cross-feature Communication](#cross-feature-communication)
- [Testing Structure](#testing-structure)
- [Common Patterns](#common-patterns)
- [Important Rules](#important-rules)

## Overview

It-Day follows **Clean Architecture** with **feature-based modular** organization. Each feature is self-contained and implements the three core layers: data, domain, and presentation.

For comprehensive project context and setup, see [project-context.md](../project-context.md).

## Core Principles

### 1. Dependency Direction

```
Presentation → Domain ← Data
     ↓
   (never goes up)
```

- **Presentation layer** depends on **Domain** (interfaces & models)
- **Data layer** implements **Domain** interfaces
- **Domain layer** is independent (no dependencies on other layers)
- No layer should depend on presentation

### 2. Feature Isolation

Each feature is self-contained:
- Features communicate through navigation, not direct imports
- Feature A should NOT import from feature B's implementation
- Cross-feature sharing goes through `core/` package

### 3. Single Responsibility

Each class has one reason to change:
- API calls → Api/Repository layer
- Business logic → Domain layer
- UI logic → ViewModel/Presentation layer

## Layer Structure in Detail

### Data Layer

**Location**: `feature/[name]/data/`

Responsibilities:
- Network API calls (Retrofit)
- Database operations (Room)
- Local file storage
- Data transformation (API DTO → Domain Model)

Structure:
```
feature/benefit/data/
├── api/                  # Retrofit API interfaces
│   └── MembershipApi.kt
├── repository/           # Repository implementations
│   └── MembershipRepositoryImpl.kt
├── model/                # DTOs and API models
│   ├── MembershipSearchItem.kt
│   └── MembershipResponse.kt
├── database/             # Room DAOs and entities
│   ├── BenefitPlanEntity.kt
│   ├── BenefitPlanDao.kt
│   └── BenefitDatabase.kt
└── service/              # Services (alarms, notifications)
    └── BenefitAlarmManager.kt
```

**Key Rules**:
- API models (DTOs) stay in `data/model/`
- Never expose DTO to presentation layer
- Repository implementation uses DTOs internally, returns domain models to presentation
- Use `@Inject` constructor injection, registered in Hilt modules

Example:
```kotlin
// data/model/MembershipSearchItem.kt
data class MembershipSearchItem(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("ingredient")
    val ingredient: String
)

// data/repository/MembershipRepositoryImpl.kt
class MembershipRepositoryImpl @Inject constructor(
    private val membershipApi: MembershipApi
) : MembershipRepository {
    override suspend fun searchMemberships(keyword: String): List<Membership> {
        val response = membershipApi.searchMemberships(keyword)
        return response.result.map { it.toDomain() }
    }
}

// Helper extension
private fun MembershipSearchItem.toDomain(): Membership =
    Membership(id, name, ingredient)
```

### Domain Layer

**Location**: `feature/[name]/domain/`

Responsibilities:
- Define repository interfaces (contracts)
- Define domain models (pure, UI-agnostic)
- Implement business logic through use cases

Structure:
```
feature/benefit/domain/
├── repository/           # Repository interfaces
│   └── MembershipRepository.kt
├── model/                # Domain models
│   └── Membership.kt
└── usecase/              # Use cases (business logic)
    ├── SearchMembershipsUseCase.kt
    └── GetFavoriteMembershipsUseCase.kt
```

**Key Rules**:
- Repository interfaces define the contract
- Domain models are plain Kotlin classes/data classes
- No references to UI frameworks or Android classes
- Use cases encapsulate business logic
- No imports from `data/` or `presentation/` packages

Example:
```kotlin
// domain/repository/MembershipRepository.kt
interface MembershipRepository {
    suspend fun searchMemberships(keyword: String): List<Membership>
}

// domain/model/Membership.kt
data class Membership(
    val id: String,
    val name: String,
    val ingredient: String
)

// domain/usecase/SearchMembershipsUseCase.kt
class SearchMembershipsUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(keyword: String): List<Membership> {
        return if (keyword.isBlank()) emptyList()
        else repository.searchMemberships(keyword.trim())
    }
}
```

### Presentation Layer

**Location**: `feature/[name]/presentation/`

Responsibilities:
- Display data using ViewModels
- Handle user interactions
- Manage UI state (loading, error, success)
- Navigate between screens

Structure:
```
feature/benefit/presentation/
├── component/            # Feature-specific composables
│   └── BenefitListItem.kt
├── BenefitSearchRoute.kt # State collection and navigation boundary
├── BenefitSearchScreen.kt
├── BenefitViewModel.kt
├── BenefitUiState.kt
└── BenefitUiEvent.kt
```

**Key Rules**:
- ViewModel receives dependencies (use cases or repositories) through constructor injection with `@HiltViewModel`
- Use StateFlow for screen state management
- Route composables should be thin; delegate logic to ViewModel and render through stateless screens
- Bind only domain models in UI, never DTOs
- UI state should be sealed class or simple state holder

→ For the canonical ViewModel pattern with UI state management: [ui-patterns.md — ViewModel Architecture](ui-patterns.md#viewmodel-architecture)

Example:
```kotlin
// presentation/BenefitSearchRoute.kt
@Composable
fun BenefitSearchRoute(
    onBenefitClick: (String) -> Unit,
    viewModel: BenefitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BenefitSearchScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBenefitClick = onBenefitClick,
    )
}
```

## Cross-feature Communication

When multiple features need to communicate:

### Option 1: Shared Domain Models
Create shared models in `core/` package if needed across features:
```
core/model/
├── CommonUser.kt
└── CommonResult.kt
```

### Option 2: Navigation Arguments
Pass IDs through typed Navigation Compose routes (preferred):
```kotlin
@Serializable
data class BenefitDetailRoute(val membershipId: String)

composable<BenefitSearchRoute> {
    BenefitSearchRoute(
        onBenefitClick = { membershipId ->
            navController.navigate(BenefitDetailRoute(membershipId))
        },
    )
}

composable<BenefitDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<BenefitDetailRoute>()
    BenefitDetailRoute(membershipId = route.membershipId)
}
```

### Option 3: Shared Repository
For data that multiple features need, create a shared repository in `core/`:
```
core/repository/
├── UserRepository.kt         # Manages user data
└── UserRepositoryImpl.kt      # Implementation
```

## Testing Structure

Each layer should have corresponding tests. See [testing.md](testing.md) for comprehensive testing guidelines.

```
app/src/test/java/com/umc/itday/feature/benefit/
├── data/
│   ├── repository/MembershipRepositoryImplTest.kt
│   └── api/MembershipApiTest.kt
├── domain/
│   └── usecase/SearchMembershipsUseCaseTest.kt
└── presentation/
    └── viewmodel/BenefitViewModelTest.kt
```

**Testing Rules**:
- Unit test data layer with mocked API
- Unit test domain layer independently
- Unit test ViewModel with mocked use cases
- Use JUnit 4 + Mockk for mocking
- Keep tests fast (< 100ms each)

→ For comprehensive test examples (repository, use case, ViewModel) with Arrange-Act-Assert structure: [testing.md — Unit Tests](testing.md#unit-tests)

## Common Patterns

> **Example only**: The benefit classes below demonstrate the proposed dependency direction. They do not describe classes that currently exist in It-Day.

### Repository Pattern
```kotlin
// Interface in domain
interface BenefitRepository {
    suspend fun searchMemberships(keyword: String): List<Membership>
    suspend fun getMembership(id: String): Membership
}

// Implementation in data
class BenefitRepositoryImpl @Inject constructor(
    private val api: MembershipApi,
    private val dao: BenefitPlanDao
) : BenefitRepository {
    override suspend fun searchMemberships(keyword: String): List<Membership> {
        return api.searchMemberships(keyword).result.map { it.toDomain() }
    }

    override suspend fun getMembership(id: String): Membership {
        return api.getMembership(id).result.toDomain()
    }
}
```

### ViewModel with StateFlow
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<UIState>(UIState.Loading)
    val state: StateFlow<UIState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = UIState.Loading
            try {
                val data = useCase()
                _state.value = UIState.Success(data)
            } catch (e: Exception) {
                _state.value = UIState.Error(e)
            }
        }
    }
}
```

### Extension Functions for Transformation
```kotlin
// In data layer for DTO → Domain transformation
internal fun MembershipSearchItemDto.toDomain(): Membership =
    Membership(
        id = this.id,
        name = this.name,
        ingredient = this.ingredient
    )
```

## Important Rules

1. **No Circular Dependencies**: If A imports B, B must not import A
2. **No Presentation in Domain**: Domain layer must never import Android or presentation classes
3. **No Business Logic in UI**: All logic goes to ViewModel/UseCase
4. **Suspend Functions for Async**: Always use `suspend` for coroutine-based async operations
5. **Constructor Injection**: Always use constructor injection with `@Inject` and Hilt
6. **Single Responsibility**: One class = one reason to change
7. **SOLID Principles**: Especially Interface Segregation and Dependency Inversion

## Related Documentation

- [Coding Conventions](coding-conventions.md) - Code style and Kotlin idioms
- [API Integration](api-integration.md) - Network layer implementation
- [Hilt DI](hilt-di.md) - Dependency injection setup
- [UI Patterns](ui-patterns.md) - ViewModel and Compose screen patterns
- [Testing](testing.md) - Testing each layer
- [Commit Conventions](commit-conventions.md) - Git and PR practices

---

**Last Updated**: 2026-03-31
