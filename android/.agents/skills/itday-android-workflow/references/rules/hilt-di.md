> It-Day 적용 메모: 원문의 상세 DI 설명을 유지하면서 UI 진입점과 테스트 예제를 Compose 전용 구조로 변환한 문서입니다.

# Hilt Dependency Injection Guide

> The package names and benefit classes in this document are illustrative. Read the actual namespace from Gradle and do not assume these classes exist.

## Table of Contents

- [Overview](#overview)
- [Module Structure](#module-structure)
- [Module Definition Pattern](#module-definition-pattern)
- [Scopes](#scopes)
- [Constructor Injection](#constructor-injection)
- [Qualifiers](#qualifiers)
- [Bindings](#bindings-interface-to-implementation)
- [Entry Points](#entry-points)
- [Database Provision Pattern](#database-provision-pattern)
- [Context Injection](#context-injection)
- [Testing with Hilt](#testing-with-hilt)
- [Common Patterns](#common-patterns)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [Module Registration](#module-registration)

## Overview

This is a proposed **Hilt** guide. Hilt is not assumed to be installed. Apply these rules only after the team approves Hilt and its Gradle plugins and dependencies are present.

**Key Benefits**:
- Compile-time safety (errors caught at build time)
- Scope management (Singleton, Activity scope, etc.)
- Boilerplate reduction
- Auto-wiring through `@Inject`

## Module Structure

DI modules are organized **per feature**, located in `feature/[name]/di/`:

```
feature/benefit/
├── di/
│   └── BenefitModule.kt         # Benefit-specific dependencies
├── data/
├── domain/
└── presentation/

feature/home/
├── di/
│   └── HomeModule.kt         # Home-specific dependencies
├── data/
├── domain/
└── presentation/

feature/auth/
├── di/
│   └── AuthModule.kt         # Auth-specific dependencies
├── data/
├── domain/
└── presentation/

app/di/
├── NetworkModule.kt          # Retrofit, OkHttp, API services (app-level)
├── AuthModule.kt             # Auth-related (if shared)
├── SettingModule.kt         # User info-related
└── LocationModule.kt         # Location-related
```

## Module Definition Pattern

### Feature-level Module

Each feature has its own module in `feature/[name]/di/`:

> **Example only**: `BenefitModule`, `BenefitDatabase`, and related providers illustrate a possible target pattern. They do not describe current It-Day production code.

```kotlin
// feature/benefit/di/BenefitModule.kt
package com.itday.feature.benefit.di

import android.content.Context
import androidx.room.Room
import com.itday.feature.benefit.data.database.BenefitDatabase
import com.itday.feature.benefit.data.database.BenefitPlanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BenefitModule {

    @Provides
    @Singleton
    fun provideBenefitDatabase(
        @ApplicationContext context: Context
    ): BenefitDatabase = Room.databaseBuilder(
        context,
        BenefitDatabase::class.java,
        "benefit_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideBenefitPlanDao(database: BenefitDatabase): BenefitPlanDao =
        database.benefitPlanDao()

    @Provides
    @Singleton
    fun provideBenefitRepository(
        membershipApi: MembershipApi,
        benefitPlanDao: BenefitPlanDao
    ): BenefitRepository = BenefitRepositoryImpl(membershipApi, benefitPlanDao)

    @Provides
    @Singleton
    fun provideSearchMembershipsUseCase(
        benefitRepository: BenefitRepository
    ): SearchMembershipsUseCase = SearchMembershipsUseCase(benefitRepository)
}
```

### App-level Modules

Global infrastructure goes in `app/di/`:

```kotlin
// app/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            Json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    // API Service Providers
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMembershipApi(retrofit: Retrofit): MembershipApi =
        retrofit.create(MembershipApi::class.java)

    @Provides
    @Singleton
    fun provideStoreApi(retrofit: Retrofit): StoreApi =
        retrofit.create(StoreApi::class.java)

    @Provides
    @Singleton
    fun provideSettingApi(retrofit: Retrofit): SettingApi =
        retrofit.create(SettingApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)
}
```

## Scopes

### SingletonComponent (App-wide)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Provides
    @Singleton
    fun provideRepository(api: MyApi): MyRepository =
        MyRepository(api)
}
```

Used for:
- API clients (Retrofit, OkHttp)
- Repositories
- Use Cases
- Database providers
- Context-dependent utilities

### ActivityComponent (Activity scope)
```kotlin
@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun provideActivityHelper(
        @ActivityContext context: Context
    ): ActivityHelper = ActivityHelper(context)
}
```

### ViewModelComponent (ViewModel scope)
```kotlin
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    fun provideBenefitFormatter(): BenefitFormatter =
        BenefitFormatter()
}
```

## Constructor Injection

### In ViewModels

```kotlin
@HiltViewModel
class BenefitViewModel @Inject constructor(
    private val searchMembershipsUseCase: SearchMembershipsUseCase,
    private val getBenefitPlansUseCase: GetBenefitPlansUseCase
) : ViewModel() {
    // Implementation
}
```

### In Repository Implementations

```kotlin
class BenefitRepositoryImpl @Inject constructor(
    private val membershipApi: MembershipApi,
    private val benefitPlanDao: BenefitPlanDao
) : BenefitRepository {
    // Implementation
}
```

### In Use Cases

```kotlin
class SearchMembershipsUseCase @Inject constructor(
    private val benefitRepository: BenefitRepository
) {
    suspend operator fun invoke(keyword: String): List<Membership> {
        // Implementation
    }
}
```

### In Interceptors

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Implementation
    }
}
```

## Qualifiers

Use when you need multiple implementations of the same interface:

### 1. Custom Qualifiers

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppPreferences
```

### 2. Using Qualifiers

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    @UserPreferences
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @AppPreferences
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)
}

// Injection
class MyRepository @Inject constructor(
    @UserPreferences private val userPrefs: SharedPreferences,
    @AppPreferences private val appPrefs: SharedPreferences
)
```

## Bindings (Interface to Implementation)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object BindModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindBenefitRepository(
        impl: BenefitRepositoryImpl
    ): BenefitRepository
}
```

Note: Use `abstract` functions with `@Binds` instead of `@Provides` for simpler interface-to-implementation bindings.

## Entry Points

### Application Class

```kotlin
@HiltAndroidApp
class ItDayApp : Application() {
    // Hilt initializes here
}
```

### Application, Activity, and ViewModel

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Hosts the Compose content tree.
}

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository,
) : ViewModel()

@Composable
fun MyRoute(
    viewModel: MyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MyScreen(uiState = uiState)
}
```

## Database Provision Pattern

The hypothetical `benefit` feature demonstrates this pattern. → See [Module Definition Pattern](#module-definition-pattern) for the full example.

```kotlin
// feature/home/di/HomeModule.kt
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideHomeDatabase(
        @ApplicationContext context: Context
    ): HomeDatabase = Room.databaseBuilder(
        context,
        HomeDatabase::class.java,
        "home_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideHomeSessionDao(
        database: HomeDatabase
    ): HomeSessionDao = database.homeSessionDao()
}
```

## Context Injection

```kotlin
class MyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getAppName(): String = context.getString(R.string.app_name)
}
```

## Feature Module Pattern

```kotlin
// feature/auth/di/AuthModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi
    ): AuthRepository = AuthRepositoryImpl(authApi)

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)
}
```

The `benefit` feature's module is in [Module Definition Pattern](#module-definition-pattern).

## Testing with Hilt

For unit tests, use Hilt's test modules:

```kotlin
// For integration tests
@HiltAndroidTest
class MyIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testWithHilt() {
        // Test with Hilt-injected dependencies
    }
}

// For unit tests with mocked dependencies
class MyRepositoryTest {
    private val api: MyApi = mockk()
    private val repository = MyRepository(api)

    @Test
    fun testSearch() {
        // Test without Hilt (simpler for unit tests)
    }
}
```

## Common Patterns

### Lazy Initialization

```kotlin
// feature/benefit/di/BenefitModule.kt
@Module
@InstallIn(SingletonComponent::class)
object BenefitModule {

    @Provides
    @Singleton
    fun provideLazyRepository(
        membershipApi: MembershipApi
    ): Lazy<BenefitRepository> = lazy {
        BenefitRepositoryImpl(membershipApi, mockk())
    }
}

// Usage
class MyViewModel @Inject constructor(
    private val lazyRepository: Lazy<BenefitRepository>
) {
    fun loadData() {
        val repo = lazyRepository.value  // Initialized on first access
    }
}
```

### Provider (Factory Pattern)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {

    @Provides
    @Singleton
    fun provideRepositoryProvider(
        api: MyApi
    ): Provider<MyRepository> = Provider { MyRepository(api) }
}

// Usage - creates new instance each time
class MyService @Inject constructor(
    private val repositoryProvider: Provider<MyRepository>
) {
    fun useNewRepository() {
        val newRepo = repositoryProvider.get()
    }
}
```

## Best Practices

### 1. Organize Modules by Feature
```kotlin
// Good: modules stay with their features
feature/benefit/di/BenefitModule.kt
feature/auth/di/AuthModule.kt
feature/home/di/HomeModule.kt

// App-level shared modules
app/di/NetworkModule.kt
app/di/LocationModule.kt
```

### 2. Use Binds for Interfaces
```kotlin
// Good: explicit binding
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: RepositoryImpl): Repository
}
```

### 3. Scope Appropriately
```kotlin
// Singleton for expensive resources
@Provides
@Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit = ...

// Activity scope for activity-specific
@Provides
fun provideActivityHelper(@ActivityContext context: Context): Helper = ...
```

### 4. Use Qualifiers Sparingly
```kotlin
// Good: only when necessary
@Provides
@Singleton
@UserPreferences
fun provideUserPrefs(...): SharedPreferences = ...
```

### 5. Document Complex Bindings
```kotlin
/**
 * Provides the main API client for It-Day backend.
 * Includes authentication interceptor and 30-second timeouts.
 */
@Provides
@Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit = ...
```

## Troubleshooting

### Issue: "No binding provided"
```kotlin
// Error: No binding for MyClass
// Solution: Add @Inject constructor or provide in module
@Inject constructor(...)  // Auto-inject if has @Inject constructor
// or
@Provides fun provideMyClass(): MyClass = ...
```

### Issue: "Circular dependency"
```kotlin
// Error: Circular dependency
// Solution: Break the cycle with Lazy or Provider
class A @Inject constructor(lazyB: Lazy<B>)
class B @Inject constructor(a: A)
```

### Issue: "Scope mismatch"
```kotlin
// Error: Scope mismatch
// Solution: Ensure scopes match
@Singleton  // Module
fun provideRepository(...): Repository = ...

// Can be injected into Singleton, but not Activity scope
```

## Module Registration

When you create a new feature module in `feature/[name]/di/`, Hilt automatically discovers it via `@InstallIn(SingletonComponent::class)`. No explicit registration needed.

However, ensure:
1. Module has `@Module` annotation
2. Module has `@InstallIn(SingletonComponent::class)` or appropriate component
3. Providers/Bindings have `@Provides/@Binds` annotations
4. Build with `./gradlew build` to trigger Hilt code generation

## Related Documentation

- [Architecture](architecture.md) - Dependency direction and layer responsibilities
- [Coding Conventions](coding-conventions.md) - Hilt-specific code style
- [API Integration](api-integration.md) - Network module configuration
- [Testing](testing.md) - Hilt testing patterns

---

**Last Updated**: 2026-03-31
