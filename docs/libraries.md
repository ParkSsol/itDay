# Libraries

## Selected

| Purpose | Library | Notes |
| --- | --- | --- |
| UI | Jetpack Compose, Material 3 | Compose-first UI. |
| Navigation | Navigation Compose | Prefer typed routes where supported. |
| DI | Hilt with KSP | Use constructor injection. |
| Network | Retrofit, OkHttp | Configure in `core/network`. |
| Serialization | Kotlin Serialization | Prefer over Gson for new code. |
| Async | Kotlin Coroutines, Flow | Expose UI state as StateFlow. |
| Image | Coil Compose | Compose image loading. |
| Map | Kakao Maps SDK | It-Day map provider. |
| Location | Play services Location | Separate location from map rendering. |
| Quality | ktlint, detekt | Run locally and in CI. |
| Tests | JUnit, coroutines-test, MockK | Test behavior and state. |
| Coverage | JaCoCo | Report first; enforce later. |

## Deferred

- Firebase: add only after selecting FCM, Crashlytics, Analytics, or another concrete product.
- Lottie: add only with an approved Lottie asset and a need beyond Compose animation.
- Room: add when structured offline persistence is required.

## Excluded from the current scope

- Alternative map/image stacks, CameraX, ML Kit OCR, EXIF helpers
- Medical, prescription, hospital, AI consultation, and Google OAuth dependencies
- View-based UI and adapter dependencies

Merge `gradle-template/libs.versions.toml` into the Android Studio-generated catalog. Preserve newer compatible AGP, Kotlin, and Compose versions generated for It-Day.
