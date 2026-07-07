> It-Day 적용 메모: 원문의 상세 절차와 설명 범위를 유지하면서 UI 관련 내용을 Compose 전용 방식으로 변환한 문서입니다.

---
name: api
description: Scaffolds a complete Retrofit API endpoint for It-Day — API interface, response model, repository interface, repository implementation, and Hilt binding. Usage: /api <endpoint description>
---

You will scaffold a new REST API endpoint for It-Day using the architecture and networking libraries currently approved and configured in the codebase. The patterns below are proposals, not proof that Retrofit, Hilt, or Clean Architecture is already installed.

The endpoint description follows after this prompt.

## Step 1 — Analyze Existing Patterns

Before writing any code, read an existing similar feature's data layer to understand patterns:
- Read files in `app/src/main/java/com/umc/itday/feature/setting/data/` as the reference
- Note the exact naming conventions, annotation usage, and error handling style

## Step 2 — Determine the Feature Module

From the endpoint description, identify which feature module this belongs to (or if a new one is needed).
Path pattern: `app/src/main/java/com/umc/itday/feature/<module>/`

## Step 3 — Create Files in This Order

### 1. Response Model
`data/model/<EntityName>Response.kt`
- `@Serializable` data class (or match existing serialization library)
- Include all fields from the API response

### 2. API Interface
`data/api/<FeatureName>Api.kt`
- Retrofit interface
- Annotate with `@GET`/`@POST`/`@PUT`/`@DELETE` as appropriate
- Use `suspend fun` returning `Response<T>` or the project's wrapper type

### 3. Repository Interface
`domain/repository/<FeatureName>Repository.kt`
- Pure Kotlin interface (no Android imports)
- Return type: `Result<T>` or the project's domain result type

### 4. Repository Implementation
`data/repository/<FeatureName>RepositoryImpl.kt`
- Implements the domain interface
- Handles HTTP error codes and maps to domain result
- Injected with `@Inject constructor`

### 5. Hilt Module Binding
`di/<FeatureName>Module.kt`
- `@Module @InstallIn(SingletonComponent::class)`
- `@Binds` to wire `RepositoryImpl` → `Repository`
- Add `Api` provision if not already in `NetworkModule`

## Step 4 — Output

List every created file with its full path and complete code.
Note any assumptions made about the API response shape.
