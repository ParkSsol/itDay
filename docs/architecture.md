# Architecture

Start with a single `app` Gradle module and feature-first packages. Split modules only when build time, ownership, or dependency boundaries justify it.

```text
com.itday/
|-- app/
|   |-- ItDayApplication.kt
|   |-- MainActivity.kt
|   |-- navigation/
|   `-- di/
|-- core/
|   |-- designsystem/
|   |-- location/
|   |-- map/
|   |-- model/
|   |-- network/
|   `-- util/
`-- feature/
    |-- auth/
    |-- onboarding/
    |-- home/
    |-- character/
    |-- report/
    |-- map/
    `-- setting/
```

## Feature layers

Create only the layers a feature needs:

```text
feature/home/
|-- data/
|   |-- remote/
|   |-- model/
|   |-- mapper/
|   `-- repository/
|-- domain/
|   |-- model/
|   |-- repository/
|   `-- usecase/
`-- presentation/
    |-- component/
    |-- HomeScreen.kt
    |-- HomeViewModel.kt
    |-- HomeUiState.kt
    `-- HomeUiEvent.kt
```

- `presentation` depends on domain contracts, never Retrofit DTOs.
- `data` implements domain repository contracts and owns DTO mapping.
- `domain` contains business rules and no Android dependencies.
- `core` contains only shared, feature-neutral code.
- A `BenefitCard` remains in `feature/home` unless another feature genuinely shares it.
- Pass stable IDs or small immutable values through navigation.

```text
User action -> Screen callback -> ViewModel -> UseCase/Repository
                                      |
                                      v
                                 StateFlow<UiState>
                                      |
                                      v
                                   Compose UI
```

Use one immutable state per screen. Keep one-time navigation or snackbar effects separate from persistent state.
