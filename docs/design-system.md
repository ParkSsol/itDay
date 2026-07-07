# Design system

Place shared design code under `core/designsystem`.

```text
core/designsystem/
|-- component/ItDayButton.kt
`-- theme/
    |-- Color.kt
    |-- Dimens.kt
    |-- Shape.kt
    |-- Theme.kt
    `-- Type.kt
```

- Use semantic colors such as `MaterialTheme.colorScheme.primary` in screens.
- Keep raw palette values private to theme construction where possible.
- Use shade names such as `Primary400` and `Primary600` only in the palette.
- Use semantic spacing tokens instead of repeated raw `dp` values.
- Do not name spacing `margin`; Compose expresses it through layout arrangement or padding.

The spacing token names and values are not finalized. Names such as `Tight` and `Basic`,
and values such as `12.dp` and `24.dp`, are examples to review against Figma before implementation.
Document the agreed mapping before adding it to `Dimens.kt`.

The first shared component is `ItDayButton`, covering primary, outlined, enabled, disabled, and loading states. Feature-branded cards remain in their feature. Shared components need previews for important states and must support accessibility, large fonts, and touch targets.
