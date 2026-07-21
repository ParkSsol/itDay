# It-Day repository guidance

## Project

It-Day is a location-based Android service for finding carrier membership benefits and opening membership barcodes. The confirmed UI stack is Kotlin, Jetpack Compose, and Material 3. Treat MVVM, feature-first packages, and additional libraries as proposed choices until they are approved and configured.

## Structure

- `app`: application entry point, root navigation, and app-level DI.
- `core`: feature-neutral code shared by multiple features.
- `feature/<name>`: feature-owned UI and, when needed, `data`, `domain`, and `presentation` layers.
- Do not create empty layers. Add a layer only when code belongs there.
- Dependency direction is `presentation -> domain <- data`. Domain must not depend on Android or data implementations.

Read the relevant document before changing that area:

- Architecture: `../docs/architecture.md`
- Libraries: `../docs/libraries.md`
- UI/design system: `../docs/design-system.md`
- Kotlin/Compose conventions: `../docs/coding-conventions.md`
- API integration: `../docs/api-integration.md`
- Testing: `../docs/testing.md`
- Git and review: `../docs/git-workflow.md`

## Required behavior

- Prefer immutable UI state exposed as `StateFlow`.
- Keep composables side-effect free; send actions through callbacks or events.
- Use Material theme. Use It-Day spacing tokens after their names and values are approved and implemented; until then, avoid spreading repeated raw values.
- Put reusable UI in `core/designsystem`; keep feature-specific UI inside its feature.
- Use the dependency-injection approach configured in the project. Hilt is a proposal; do not add or assume it until its Gradle plugins and dependencies are approved.
- If the approved architecture separates data and domain, keep API DTOs in data and map them before presentation code uses them.
- Never commit API keys. Kakao keys and server URLs come from local properties or CI secrets.
- Firebase is excluded until a concrete Firebase product is approved.
- Do not assume Retrofit, Hilt, Room, JaCoCo, detekt, or other proposed tools are installed. Check Gradle files before using them.

## Verification

```text
cd android
./gradlew ktlintCheck detekt       # only after ktlint and detekt are configured
./gradlew testDebugUnitTest
./gradlew jacocoDebugReport        # only after JaCoCo is configured
./gradlew assembleDebug
```

On Windows use `gradlew.bat`. If a task is not configured, report that fact; do not add a tool or Gradle template unless the task explicitly requests it.

## Change discipline

- Keep each commit focused on one logical change.
- Use `<type>: <Korean subject>` for commits. Use `[Feature] 제목` 등 `.github/pull_request_template.md`의 형식 for PR titles.
- Preserve unrelated user changes.
- Test business logic, mappings, and ViewModel state transitions when behavior changes.
