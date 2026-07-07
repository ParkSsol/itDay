> It-Day 적용 메모: 원문의 상세 API 절차를 유지하면서 직렬화와 화면 전달 방식을 Kotlin Serialization·Navigation Compose 기준으로 변환한 문서입니다.

# API Integration & Network Layer Guide

> The package names and membership classes in this document are illustrative. Read the actual namespace from Gradle and do not assume these classes or libraries exist.

## Table of Contents

- [Overview](#overview)
- [Network Architecture](#network-architecture)
- [Defining APIs](#defining-apis)
- [Repository Implementation](#repository-implementation)
- [Domain Layer Repository Interface](#domain-layer-repository-interface)
- [AuthInterceptor](#authinterceptor)
- [Retrofit Configuration](#retrofit-configuration)
- [Using APIs in ViewModels](#using-apis-in-viewmodels)
- [Error Handling](#error-handling)
- [Adding New API Endpoints](#adding-new-api-endpoints)
- [Request & Response Examples](#request--response-examples)
- [Testing Network Layer](#testing-network-layer)
- [Best Practices](#best-practices)

## Overview

This guide shows the proposed **Retrofit 2** and **OkHttp 4** approach. Use it only after those libraries and the related architecture have been approved and configured; otherwise follow the current Gradle setup.

For architecture details, see [architecture.md](architecture.md).

## Network Architecture

### Layer Organization

```
core/network/
├── BaseResponse.kt          # Wrapper for all API responses
├── AuthInterceptor.kt       # Auto-injects JWT tokens
└── status/
    ├── NetworkMonitor.kt    # Monitors network connectivity
    ├── NetworkStatus.kt     # Status enum
    └── NetworkViewModel.kt  # Network state management

feature/[name]/data/
├── api/
│   └── MyApi.kt             # Retrofit interface
├── model/
│   ├── MyRequest.kt         # Request DTOs
│   └── MyResponse.kt        # Response DTOs
└── repository/
    └── MyRepositoryImpl.kt   # Implementation (handles API calls)
```

### BaseResponse Wrapper

All API responses are wrapped in `BaseResponse`:

```kotlin
// core/network/BaseResponse.kt
data class BaseResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val result: T  // Generic for flexible types
)
```

Every API endpoint returns this structure:
```json
{
  "success": true,
  "code": "000",
  "message": "Success",
  "result": { /* actual data */ }
}
```

## Defining APIs

### 1. Create API Interface

```kotlin
// feature/benefit/data/api/MembershipApi.kt
package com.itday.feature.benefit.data.api

import com.itday.core.network.BaseResponse
import com.itday.feature.benefit.data.model.MembershipSearchItem
import retrofit2.http.GET
import retrofit2.http.Query

interface MembershipApi {
    @GET("v1/memberships/search")
    suspend fun searchMemberships(
        @Query("name") keyword: String
    ): BaseResponse<List<MembershipSearchItem>>

    @GET("v1/memberships/{id}")
    suspend fun getMembership(
        @Path("id") membershipId: String
    ): BaseResponse<MembershipSearchItem>
}
```

**Rules**:
- Always use `suspend` for async operations
- Return `BaseResponse<T>` (not just `T`)
- Use meaningful HTTP verbs (`@GET`, `@POST`, etc.)
- Document parameters with `@Query`, `@Path`, `@Body`
- Put API interface in `data/api/` package

### 2. Create DTOs (Request/Response Models)

```kotlin
// feature/benefit/data/model/MembershipSearchItem.kt
package com.itday.feature.benefit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipSearchItem(
    @SerialName("membershipId")
    val membershipId: String,
    @SerialName("membershipName")
    val membershipName: String,
    @SerialName("partnerName")
    val entpName: String,
    @SerialName("membershipImage")
    val membershipImage: String,
    @SerialName("benefitDescription")
    val efficacy: String
)
```

**Rules**:
- Use `@SerialName` only when the JSON field differs from the Kotlin property name
- Pass stable IDs through typed Navigation Compose routes instead of passing DTO objects
- Make fields nullable with `? = null` for optional fields
- Use data classes for immutability
- Put models in `data/model/` package
- Never expose DTOs in presentation layer

For coding conventions details, see [coding-conventions.md](coding-conventions.md).

## Repository Implementation

> **Example only**: The membership classes below illustrate a possible repository boundary. They do not describe classes that currently exist in It-Day.

### Data Layer Repository

```kotlin
// feature/benefit/data/repository/MembershipRepositoryImpl.kt
package com.itday.feature.benefit.data.repository

import com.itday.feature.benefit.data.api.MembershipApi
import com.itday.feature.benefit.data.model.MembershipSearchItem
import com.itday.feature.benefit.domain.model.Membership
import com.itday.feature.benefit.domain.repository.MembershipRepository
import javax.inject.Inject

class MembershipRepositoryImpl @Inject constructor(
    private val membershipApi: MembershipApi
) : MembershipRepository {

    override suspend fun searchMemberships(keyword: String): List<Membership> {
        // Call API
        val response = membershipApi.searchMemberships(keyword)

        // Check success and extract result
        return if (response.success) {
            response.result.map { it.toDomain() }
        } else {
            throw Exception(response.message)
        }
    }

    override suspend fun getMembership(membershipId: String): Membership {
        val response = membershipApi.getMembership(membershipId)

        return if (response.success) {
            response.result.toDomain()
        } else {
            throw Exception(response.message)
        }
    }
}

// Helper extension function for DTO -> Domain conversion
private fun MembershipSearchItem.toDomain(): Membership =
    Membership(
        id = this.id,
        name = this.name,
        ingredient = this.ingredient,
        dosage = this.dosage ?: "Unknown"
    )
```

**Rules**:
- Repository receives API client through constructor injection
- Always handle `BaseResponse` success check
- Transform DTOs to domain models with extension functions
- Throw exceptions with meaningful messages
- Put repository implementation in `data/repository/` package

## Domain Layer Repository Interface

```kotlin
// feature/benefit/domain/repository/MembershipRepository.kt
package com.itday.feature.benefit.domain.repository

import com.itday.feature.benefit.domain.model.Membership

interface MembershipRepository {
    suspend fun searchMemberships(keyword: String): List<Membership>
    suspend fun getMembership(membershipId: String): Membership
}
```

**Rules**:
- Define contract in domain layer
- Expose only domain models
- Use `suspend` for coroutine operations
- Put repository interfaces in `domain/repository/` package

## AuthInterceptor

Automatically injects JWT tokens into requests:

```kotlin
// core/network/AuthInterceptor.kt
package com.itday.core.network

import com.itday.feature.auth.presentation.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = tokenManager.getAccessToken()
        val newRequest = originalRequest.newBuilder().apply {
            if (accessToken.isNotEmpty()) {
                addHeader("Authorization", "Bearer $accessToken")
            }
            addHeader("Content-Type", "application/json")
            addHeader("Accept", "application/json")
        }.build()

        return chain.proceed(newRequest)
    }
}
```

**How it works**:
1. Extracts access token from `TokenManager`
2. Adds `Authorization: Bearer <token>` header
3. Adds content-type headers
4. Proceeds with request

## Retrofit Configuration

The `NetworkModule` shown below is a target example for a future Hilt-based setup. Do not assume that it or `MembershipApi` currently exists.

→ See [hilt-di.md — App-level Modules](hilt-di.md#app-level-modules) for the canonical `NetworkModule` code example.

**Configuration**:
- Base URL: `BuildConfig.SERVER_BASE_URL` (from `gradle.properties`)
- Timeouts: All set to **30 seconds**
- Converter: Kotlin Serialization for JSON serialization
- Interceptors: AuthInterceptor for token injection

## Using APIs in ViewModels

ViewModels call APIs (or use cases) within `viewModelScope.launch`. Always update UI state (Loading, Success, Empty, Error) to represent async operation status.

→ See [ui-patterns.md — ViewModel Architecture](ui-patterns.md#viewmodel-architecture) for the canonical `BenefitViewModel` and `BenefitSearchUiState` example (including debounce and Empty state patterns).

**Rules**:
- Always use use cases instead of calling repository directly
- Handle exceptions with try-catch
- Update UI state (Loading, Success, Error)
- Use viewModelScope for coroutine lifecycle

## Error Handling

### BaseResponse Pattern

```kotlin
// Check success flag
if (response.success) {
    return response.result
} else {
    // Use message for error
    throw ApiException(response.code, response.message)
}
```

### Custom Exception

```kotlin
data class ApiException(
    val code: String,
    override val message: String
) : Exception(message)
```

### In ViewModel

```kotlin
try {
    val result = searchUseCase(keyword)
    updateUI(result)
} catch (e: ApiException) {
    showError("Server error: ${e.message}")
} catch (e: IOException) {
    showError("Network error")
} catch (e: Exception) {
    showError("Unknown error: ${e.message}")
}
```

## Adding New API Endpoints

**Step-by-step**:

1. **Define DTO** in `feature/[name]/data/model/`:
```kotlin
data class MyResponse(
    @SerialName("field1") val field1: String,
    @SerialName("field2") val field2: Int
)
```

2. **Add endpoint** to `feature/[name]/data/api/MyApi.kt`:
```kotlin
@GET("v1/endpoint")
suspend fun getMyData(): BaseResponse<MyResponse>
```

3. **Register** in `app/di/NetworkModule.kt`:
```kotlin
@Provides
@Singleton
fun provideMyApi(retrofit: Retrofit): MyApi =
    retrofit.create(MyApi::class.java)
```

4. **Implement** repository in `feature/[name]/data/repository/`:
```kotlin
override suspend fun getMyData(): MyDomainModel {
    val response = api.getMyData()
    return if (response.success) {
        response.result.toDomain()
    } else throw Exception(response.message)
}
```

5. **Create use case** in `feature/[name]/domain/usecase/`:
```kotlin
class GetMyDataUseCase @Inject constructor(
    private val repo: MyRepository
) {
    suspend operator fun invoke(): MyDomainModel = repo.getMyData()
}
```

6. **Register use case** in `feature/[name]/di/MyModule.kt`:
```kotlin
@Provides
@Singleton
fun provideGetMyDataUseCase(repo: MyRepository): GetMyDataUseCase =
    GetMyDataUseCase(repo)
```

7. **Use in ViewModel**:
```kotlin
viewModelScope.launch {
    try {
        val data = useCase()
        updateUI(data)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

## Request & Response Examples

### POST with Body

```kotlin
// API
@POST("v1/auth/login")
suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>

// DTO
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

data class LoginResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("expiresIn") val expiresIn: Long
)

// Repository
override suspend fun login(email: String, password: String): LoginResponse {
    val request = LoginRequest(email, password)
    val response = api.login(request)
    return if (response.success) {
        response.result
    } else {
        throw ApiException(response.code, response.message)
    }
}
```

### Query Parameters

```kotlin
// API
@GET("v1/memberships/search")
suspend fun searchMemberships(
    @Query("name") keyword: String,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 20
): BaseResponse<List<Membership>>

// Usage
val results = api.searchMemberships(keyword = "aspirin", page = 1)
```

### Path Parameters

```kotlin
// API
@GET("v1/memberships/{id}")
suspend fun getMembershipById(
    @Path("id") membershipId: String
): BaseResponse<Membership>

// Usage
val membership = api.getMembershipById("123")
```

## Testing Network Layer

For comprehensive repository and API test examples (success and failure paths, Arrange-Act-Assert structure), see [testing.md — Unit Tests](testing.md#unit-tests).

## Best Practices

1. **Always use BaseResponse**: All endpoints return this wrapper
2. **Suspend functions**: All API calls use `suspend` for coroutines
3. **DTO to Domain mapping**: Transform in repository, not ViewModel
4. **Error handling**: Check `success` flag and throw on failure
5. **Timeout handling**: 30-second timeouts configured globally
6. **Token injection**: AuthInterceptor handles automatically
7. **Nullable fields**: Use `? = null` for optional JSON fields
8. **Meaningful names**: API interfaces and DTOs clearly reflect their purpose

## Related Documentation

- [Architecture](architecture.md) - Repository pattern and layer structure
- [Coding Conventions](coding-conventions.md) - Data class and naming styles
- [Hilt DI](hilt-di.md) - NetworkModule configuration
- [Testing](testing.md) - Repository and API testing patterns

---

**Last Updated**: 2026-03-31
