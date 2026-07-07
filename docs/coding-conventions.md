# Kotlin and Compose conventions

## Naming

- Types and composables: `PascalCase`
- Functions and values: `lowerCamelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: lowercase without underscores
- UI: `HomeScreen`, `HomeViewModel`, `HomeUiState`, `HomeUiEvent`
- Repository: `StoreRepository`, `DefaultStoreRepository`

## Kotlin

- Prefer `val`, immutable collections, and exhaustive sealed hierarchies.
- Avoid `!!`; model absence explicitly or return early.
- Do not catch `Exception` indiscriminately. Preserve coroutine cancellation.
- Inject dispatchers for testable blocking work.
- Keep constants at the narrowest valid scope.

## Compose

- Hoist state and keep reusable composables stateless.
- Pass data and callbacks rather than ViewModels into leaf components.
- Put `modifier: Modifier = Modifier` first among optional parameters.
- Do not navigate, call networks, or mutate state during composition.
- Use `LaunchedEffect` only for lifecycle-bound effects with stable keys.
- Use string resources, theme colors, typography, and spacing tokens.
- Add previews for shared components and meaningful screen states.

ktlint enforces formatting and detekt enforces structural rules. Suppress only at the narrowest scope with a reason.
