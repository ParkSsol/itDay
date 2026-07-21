> It-Day 적용 메모: 원문의 상세 테스트 범위를 유지하면서 UI와 상태 테스트를 Compose·StateFlow 전용 방식으로 변환한 문서입니다.

# Testing Rules & Patterns

## Table of Contents

- [Test Structure](#test-structure)
- [Testing Tools](#testing-tools)
- [Unit Tests](#unit-tests)
- [Instrumentation Tests (Android)](#instrumentation-tests-android)
- [Best Practices](#best-practices)
- [Running Tests](#running-tests)
- [Coverage Requirements](#coverage-requirements)
- [Common Assertions](#common-assertions)
- [Mocking Patterns](#mocking-patterns)
- [Example Test Files](#example-test-files)

## Test Structure

Tests are organized by layer, mirroring the source structure:

```
app/src/
├── test/java/                        # Unit tests (JVM)
│   └── com/umc/itday/
│       └── feature/[name]/
│           ├── data/
│           │   ├── api/
│           │   │   └── MembershipApiTest.kt
│           │   └── repository/
│           │       └── MembershipRepositoryImplTest.kt
│           ├── domain/
│           │   └── usecase/
│           │       └── SearchMembershipsUseCaseTest.kt
│           └── presentation/
│               └── viewmodel/
│                   └── BenefitViewModelTest.kt
│
└── androidTest/java/                 # Instrumentation tests (Android)
    └── com/umc/itday/
        └── integration/
            └── MyIntegrationTest.kt
```

## Testing Tools

### Dependencies
- **JUnit 4**: Testing framework
- **Mockk**: Kotlin mocking library
- **Coroutines Test**: Coroutine testing utilities
- **Hilt Testing**: Hilt for instrumentation tests
- **AssertJ**: Assertion library (for fluent assertions)

## Unit Tests

### 1. Repository Tests

> **Note**: API DTO는 Kotlin Serialization의 `@Serializable`을 사용하고 화면 간에는 DTO가 아닌 안정적인 ID를 전달합니다. 아래 예제는 설명을 위해 필드를 단순화합니다.

Test data layer transformations:

```kotlin
// feature/benefit/data/repository/MembershipRepositoryImplTest.kt
class MembershipRepositoryImplTest {

    private lateinit var repository: MembershipRepositoryImpl
    private val membershipApi: MembershipApi = mockk()

    @Before
    fun setup() {
        repository = MembershipRepositoryImpl(membershipApi)
    }

    @Test
    fun `searchMemberships returns domain models on success`() = runTest {
        // Arrange
        val dtoList = listOf(
            MembershipSearchItem("1", "Aspirin", "acetylsalicylic acid"),
            MembershipSearchItem("2", "Ibuprofen", "ibuprofen")
        )
        val response = BaseResponse(
            success = true,
            code = "000",
            message = "Success",
            result = dtoList
        )
        every { runBlocking { membershipApi.searchMemberships("pain") } } returns response

        // Act
        val result = repository.searchMemberships("pain")

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("1")
        assertThat(result[0].name).isEqualTo("Aspirin")
        verify { runBlocking { membershipApi.searchMemberships("pain") } }
    }

    @Test
    fun `searchMemberships throws exception on failure`() = runTest {
        // Arrange
        val response = BaseResponse(
            success = false,
            code = "400",
            message = "Bad request",
            result = emptyList<MembershipSearchItem>()
        )
        every { runBlocking { membershipApi.searchMemberships("") } } returns response

        // Act & Assert
        assertThatThrownBy { runBlocking { repository.searchMemberships("") } }
            .isInstanceOf(Exception::class.java)
            .hasMessage("Bad request")
    }
}
```

**Rules**:
- Use `mockk()` for mocking API calls
- Test success and failure paths
- Verify API was called with correct parameters
- Use `runTest { }` for suspend functions
- Arrange-Act-Assert pattern

### 2. Use Case Tests

Test business logic:

```kotlin
// feature/benefit/domain/usecase/SearchMembershipsUseCaseTest.kt
class SearchMembershipsUseCaseTest {

    private lateinit var useCase: SearchMembershipsUseCase
    private val repository: MembershipRepository = mockk()

    @Before
    fun setup() {
        useCase = SearchMembershipsUseCase(repository)
    }

    @Test
    fun `invoke returns empty list for blank keyword`() = runTest {
        // Act
        val result = useCase("   ")

        // Assert
        assertThat(result).isEmpty()
        verify(exactly = 0) { runBlocking { repository.searchMemberships(any()) } }
    }

    @Test
    fun `invoke trims keyword before calling repository`() = runTest {
        // Arrange
        val memberships = listOf(
            Membership("1", "Aspirin", "acetylsalicylic acid", "500mg")
        )
        every { runBlocking { repository.searchMemberships("aspirin") } } returns memberships

        // Act
        val result = useCase("  aspirin  ")

        // Assert
        assertThat(result).isEqualTo(memberships)
        verify { runBlocking { repository.searchMemberships("aspirin") } }
    }

    @Test
    fun `invoke propagates repository exceptions`() = runTest {
        // Arrange
        val exception = RuntimeException("Network error")
        every { runBlocking { repository.searchMemberships(any()) } } throws exception

        // Act & Assert
        assertThatThrownBy { runBlocking { useCase("test") } }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Network error")
    }
}
```

**Rules**:
- Test business logic independently of data layer
- Mock repository to test use case logic only
- Test edge cases (empty input, null, exceptions)
- Verify parameters passed to dependencies

### 3. ViewModel Tests

> **Example only**: `BenefitViewModel` and `MembershipApi` are illustrative names. Inspect the actual production constructor before writing a test.

Test state management:

```kotlin
// feature/benefit/presentation/BenefitViewModelTest.kt
class BenefitViewModelTest {

    private lateinit var viewModel: BenefitViewModel
    private val searchUseCase: SearchMembershipsUseCase = mockk()
    private val getPlansUseCase: GetBenefitPlansUseCase = mockk()

    @Before
    fun setup() {
        viewModel = BenefitViewModel(searchUseCase, getPlansUseCase)
    }

    @Test
    fun `searchMemberships updates UI state correctly`() = runTest {
        // Arrange
        val memberships = listOf(
            Membership("1", "Aspirin", "acetylsalicylic acid", "500mg")
        )
        every { runBlocking { searchUseCase("aspirin") } } returns memberships

        // Act
        viewModel.searchMemberships("aspirin")

        // Assert - check loading state
        assertThat(viewModel.uiState.value).isInstanceOf(BenefitSearchUiState.Loading::class.java)

        // Wait for coroutine
        advanceUntilIdle()

        // Assert - check success state
        assertThat(viewModel.uiState.value).isInstanceOf(BenefitSearchUiState.Success::class.java)
        assertThat(viewModel.searchResults.value).isEqualTo(memberships)
    }

    @Test
    fun `searchMemberships updates UI with error state on failure`() = runTest {
        // Arrange
        val exception = RuntimeException("Network error")
        every { runBlocking { searchUseCase("aspirin") } } throws exception

        // Act
        viewModel.searchMemberships("aspirin")

        // Wait for coroutine
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(BenefitSearchUiState.Error::class.java)
        val errorState = state as BenefitSearchUiState.Error
        assertThat(errorState.message).contains("Network error")
    }

    @Test
    fun `updateInputText updates StateFlow`() {
        // Act
        viewModel.updateInputText("test input")

        // Assert
        assertThat(viewModel.inputText.value).isEqualTo("test input")
    }
}
```

**Rules**:
- Use `runTest { advanceUntilIdle() }` for coroutines
- Test state transitions (Idle → Loading → Success/Error)
- Verify UI observables are updated correctly

## Instrumentation Tests (Android)

Test with Android framework and Hilt-injected dependencies:

```kotlin
// app/src/androidTest/java/.../integration/BenefitIntegrationTest.kt
@HiltAndroidTest
class BenefitIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `full flow from API to ViewModel works`() {
        // Test with actual Hilt-injected dependencies
        // Note: Use test doubles for remote API in test modules
    }
}
```

See [hilt-di.md](hilt-di.md#testing-with-hilt) for Hilt testing patterns.

### Compose UI Tests

Test user-visible semantics and interactions without depending on the internal layout tree:

```kotlin
class BenefitSearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `검색 버튼을 누르면 검색 이벤트를 전달한다`() {
        var searchClicked = false

        composeRule.setContent {
            ItDayTheme {
                BenefitSearchScreen(
                    uiState = BenefitSearchUiState(query = "카페"),
                    onEvent = { event ->
                        searchClicked = event == BenefitSearchUiEvent.SearchClicked
                    },
                    onBenefitClick = {},
                )
            }
        }

        composeRule.onNodeWithText("검색").performClick()

        assertThat(searchClicked).isTrue()
    }
}
```

- Locate nodes with text, roles, content descriptions, or stable test tags.
- Verify semantics and behavior rather than row/column implementation details.
- Inject screen state directly into stateless screen composables.
- Use navigation/instrumentation tests only for behavior requiring the Android host.

## Best Practices

### 1. Test Naming
Use clear, descriptive names:

```kotlin
// Good
@Test
fun `searchMemberships returns empty list for blank keyword`() { }

@Test
fun `searchMemberships trims whitespace from keyword`() { }

@Test
fun `updateInputText updates StateFlow`() { }

// Bad
@Test
fun testSearch() { }

@Test
fun test1() { }
```

### 2. AAA Pattern (Arrange-Act-Assert)
```kotlin
@Test
fun myTest() {
    // Arrange - setup
    val expected = listOf(Membership("1", "Aspirin", "...", "..."))
    every { runBlocking { repository.search("aspirin") } } returns expected

    // Act - execute
    val result = useCase("aspirin")

    // Assert - verify
    assertThat(result).isEqualTo(expected)
}
```

### 3. One Assert Per Test (When Possible)
```kotlin
// Good
@Test
fun `searchMemberships returns non-empty list`() { ... }

@Test
fun `searchMemberships calls repository with trimmed keyword`() { ... }

// Avoid
@Test
fun `searchMemberships works correctly`() {
    // Multiple assertions about different behaviors
}
```

### 4. Mock External Dependencies
```kotlin
// Good - mock API layer
private val api: MembershipApi = mockk()

// Good - provide real business logic
val useCase = SearchMembershipsUseCase(mockRepository)

// Bad - create actual HTTP calls in tests
val api = Retrofit.Builder()...
```

### 5. Use Fixtures for Repeated Setup
```kotlin
// In a shared test utils file
object TestData {
    val defaultMembership = Membership("1", "Aspirin", "acetylsalicylic acid", "500mg")
    val defaultResponse = BaseResponse(
        success = true,
        code = "000",
        message = "Success",
        result = listOf(defaultMembership)
    )
}

// In test
@Test
fun test() {
    every { runBlocking { api.search("") } } returns TestData.defaultResponse
}
```

### 6. Test Error Cases
```kotlin
@Test
fun `repository throws on API failure`() { ... }

@Test
fun `ViewModel handles exceptions gracefully`() { ... }

@Test
fun `useCase returns empty list on error`() { ... }
```

## Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.itday.feature.benefit.*"

# Run instrumentation tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

## Coverage Requirements

Target coverage per layer:
- **Domain layer**: 80%+ (core business logic)
- **Data layer**: 70%+ (APIs and repositories)
- **Presentation layer**: 50%+ (harder to test, but try for ViewModels)

## Common Assertions

```kotlin
// Size/content
assertThat(list).hasSize(3)
assertThat(list).isEmpty()
assertThat(list).isNotEmpty()

// Equality
assertThat(actual).isEqualTo(expected)
assertThat(actual).isNotEqualTo(other)

// Boolean
assertThat(flag).isTrue()
assertThat(flag).isFalse()

// Strings
assertThat(string).contains("substring")
assertThat(string).startsWith("prefix")
assertThat(string).endsWith("suffix")

// Exceptions
assertThatThrownBy { function() }
    .isInstanceOf(Exception::class.java)
    .hasMessage("Expected message")

// Type checks
assertThat(obj).isInstanceOf(MyClass::class.java)
```

## Mocking Patterns

### Mockk Basics
```kotlin
// Create mock
val mock: MyClass = mockk()

// Setup return value
every { mock.getValue() } returns 42

// Verify call
verify { mock.getValue() }

// Verify call count
verify(exactly = 2) { mock.getValue() }

// Verify not called
verify(exactly = 0) { mock.getValue() }

// Argument matching
every { mock.function(any()) } returns true
every { mock.function("specific") } returns false
```

### Testing Suspend Functions
```kotlin
// Mock suspend function
every { runBlocking { api.search("test") } } returns result

// Or
coEvery { api.search("test") } returns result

// Verify suspend function
verify { runBlocking { api.search("test") } }

// Or
coVerify { api.search("test") }
```

## Example Test Files

See existing test structure for reference:
- Check `app/src/test/` for unit test examples
- Check `app/src/androidTest/` for instrumentation test examples

## Related Documentation

- [Architecture](architecture.md) - Layer structure and testing organization
- [API Integration](api-integration.md) - Network layer testing patterns
- [Hilt DI](hilt-di.md) - Dependency injection for tests
- [Coding Conventions](coding-conventions.md) - Code style in tests

---

**Last Updated**: 2026-03-31
