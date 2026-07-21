# Testing

- Unit test use cases, business rules, mappers, repositories with fakes, and ViewModel state transitions.
- Compose UI test critical interactions and accessibility behavior.
- Instrumentation test only behavior requiring Android or full navigation integration.
- Exclude generated code, models, previews, Hilt modules, and Compose-generated classes from coverage.

```kotlin
@Test
fun `혜택 조회 성공 시 콘텐츠 상태를 노출한다`() = runTest {
    // Given
    // When
    // Then
}
```

- Use `kotlinx-coroutines-test` and replace `Dispatchers.Main` in ViewModel tests.
- Prefer fakes for domain tests; use MockK for meaningful interaction verification.
- Never use real network, maps, location, clocks, or randomness in unit tests.

Keep JaCoCo configured from project setup. CI initially generates and uploads reports without failing by percentage. Add a team-agreed threshold after meaningful tests exist and raise it gradually.
