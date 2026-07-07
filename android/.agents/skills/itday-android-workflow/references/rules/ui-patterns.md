# UI Patterns & ViewModel Guide

## Table of Contents

- [Overview](#overview)
- [ViewModel Architecture](#viewmodel-architecture)
- [Route and Screen Implementation](#route-and-screen-implementation)
- [Navigation Compose](#navigation-compose)
- [Lazy Lists](#lazy-lists)
- [Dialogs and Bottom Sheets](#dialogs-and-bottom-sheets)
- [UI State Management Patterns](#ui-state-management-patterns)
- [Events vs State](#events-vs-state)
- [Lifecycle and Side Effects](#lifecycle-and-side-effects)
- [Performance and Stability](#performance-and-stability)
- [Design System Usage](#design-system-usage)
- [Animation](#animation)
- [Images](#images)
- [Permissions](#permissions)
- [State Restoration](#state-restoration)
- [Preview and Testing](#preview-and-testing)
- [Accessibility](#accessibility)
- [Best Practices](#best-practices)

## Overview

It-Day is a Compose-only application. Build UI with:

- Jetpack Compose and Material 3
- immutable `UiState` values exposed through `StateFlow`
- stateless screen and component composables
- route composables that connect a ViewModel to a screen
- Navigation Compose with typed routes when supported
- `LazyColumn` and `LazyRow` for large collections
- `AlertDialog`, `ModalBottomSheet`, and Compose state for transient surfaces
- design tokens from `core/designsystem`

Build every screen and reusable component as composable functions, with state and lifecycle ownership outside rendered UI nodes.

## ViewModel Architecture

### Basic ViewModel Structure

```kotlin
data class BenefitSearchUiState(
    val query: String = "",
    val benefits: List<Benefit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface BenefitSearchUiEvent {
    data class QueryChanged(val value: String) : BenefitSearchUiEvent
    data object SearchClicked : BenefitSearchUiEvent
    data object ErrorDismissed : BenefitSearchUiEvent
}

@HiltViewModel
class BenefitSearchViewModel @Inject constructor(
    private val searchBenefits: SearchBenefitsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BenefitSearchUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: BenefitSearchUiEvent) {
        when (event) {
            is BenefitSearchUiEvent.QueryChanged -> {
                _uiState.update { it.copy(query = event.value) }
            }
            BenefitSearchUiEvent.SearchClicked -> search()
            BenefitSearchUiEvent.ErrorDismissed -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun search() {
        val query = uiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { searchBenefits(query) }
                .onSuccess { benefits ->
                    _uiState.update {
                        it.copy(benefits = benefits, isLoading = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toUserMessage(),
                        )
                    }
                }
        }
    }
}
```

### ViewModel Rules

- Expose immutable `StateFlow`; keep `MutableStateFlow` private.
- Use one screen-level state object instead of many unrelated streams.
- Accept user actions through named functions or a sealed event type.
- Keep Android UI types out of ViewModels.
- Execute suspend work in `viewModelScope`.
- Inject dispatchers when repository code switches execution contexts.
- Convert technical failures to user-facing state at a defined boundary.
- Do not store navigation controllers, contexts, composable lambdas, or UI nodes.

### Derived State

Keep cheap display derivations in the state model or composable. Combine multiple asynchronous streams in the ViewModel:

```kotlin
val uiState: StateFlow<HomeUiState> = combine(
    membershipRepository.observeMembership(),
    benefitRepository.observeNearbyBenefits(),
) { membership, benefits ->
    HomeUiState.Content(membership, benefits)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = HomeUiState.Loading,
)
```

## Route and Screen Implementation

Separate state collection from reusable UI.

```kotlin
@Composable
fun BenefitSearchRoute(
    onBenefitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BenefitSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BenefitSearchScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBenefitClick = onBenefitClick,
        modifier = modifier,
    )
}

@Composable
fun BenefitSearchScreen(
    uiState: BenefitSearchUiState,
    onEvent: (BenefitSearchUiEvent) -> Unit,
    onBenefitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ItDaySpacing.Basic),
        verticalArrangement = Arrangement.spacedBy(ItDaySpacing.Tight),
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = {
                onEvent(BenefitSearchUiEvent.QueryChanged(it))
            },
            label = { Text(stringResource(R.string.benefit_search_label)) },
            modifier = Modifier.fillMaxWidth(),
        )

        ItDayButton(
            text = stringResource(R.string.search),
            onClick = { onEvent(BenefitSearchUiEvent.SearchClicked) },
            enabled = uiState.query.isNotBlank() && !uiState.isLoading,
        )

        when {
            uiState.isLoading -> LoadingContent()
            uiState.benefits.isEmpty() -> EmptyBenefitContent()
            else -> BenefitList(
                benefits = uiState.benefits,
                onBenefitClick = onBenefitClick,
            )
        }
    }
}
```

### Screen Rules

- Put `modifier: Modifier = Modifier` first among optional parameters.
- Pass immutable data and event callbacks.
- Do not retrieve a ViewModel inside leaf components.
- Do not trigger repository or navigation work during composition.
- Use `stringResource`, theme colors, typography, shapes, and spacing tokens.
- Model loading, empty, content, disabled, and error states explicitly.
- Keep route-specific dependencies out of previews.

## Navigation Compose

Prefer serializable typed routes when the Navigation version supports them.

```kotlin
@Serializable
data object HomeRoute

@Serializable
data class BenefitDetailRoute(val benefitId: Long)

@Composable
fun ItDayNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeRoute(
                onBenefitClick = { benefitId ->
                    navController.navigate(BenefitDetailRoute(benefitId))
                },
            )
        }
        composable<BenefitDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BenefitDetailRoute>()
            BenefitDetailRoute(
                benefitId = route.benefitId,
                onBack = navController::navigateUp,
            )
        }
    }
}
```

Navigation rules:

- Pass IDs and small stable values, not repositories or large DTOs.
- Keep `NavController` at the navigation boundary; pass callbacks to screens.
- Define routes in the owning feature or central navigation package.
- Avoid duplicate navigation by disabling repeated actions or using suitable launch options.
- Treat deep links and restored state as direct entry points.
- Keep domain decisions out of route parsing.

## Lazy Lists

Use stable keys and small item composables.

```kotlin
@Composable
fun BenefitList(
    benefits: List<Benefit>,
    onBenefitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = ItDaySpacing.Tight),
        verticalArrangement = Arrangement.spacedBy(ItDaySpacing.Tight),
    ) {
        items(
            items = benefits,
            key = Benefit::id,
            contentType = { "benefit" },
        ) { benefit ->
            BenefitItem(
                benefit = benefit,
                onClick = { onBenefitClick(benefit.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
```

- Do not use list indexes as keys when order can change.
- Pass only item data and callbacks to item composables.
- Use paging only after the data source requires it.
- Remember list state at the route/screen level when restoration matters.
- Avoid sorting or mapping large lists on every recomposition; prepare them in state or `remember` with correct keys.

## Dialogs and Bottom Sheets

The caller owns visibility state. Dialog content reports actions through callbacks.

```kotlin
@Composable
fun BarcodeUseConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.barcode_use_title)) },
        text = { Text(stringResource(R.string.barcode_use_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
```

- Make dismissal behavior explicit.
- Do not hide business work inside dialog composables.
- Use `ModalBottomSheet` for supplementary actions, not primary navigation.
- Hoist selected values when the result must survive configuration changes.

## UI State Management Patterns

### Sealed Screen State

```kotlin
sealed interface StoreListUiState {
    data object Loading : StoreListUiState
    data class Content(val stores: List<Store>) : StoreListUiState
    data object Empty : StoreListUiState
    data class Error(val message: String) : StoreListUiState
}
```

Use a sealed hierarchy when states are mutually exclusive. Use a data class when several properties can coexist.

### Saveable Local State

```kotlin
var selectedTab by rememberSaveable { mutableIntStateOf(0) }
```

Use local state for UI-only details such as expansion and tab selection. Move state to a ViewModel when it affects business behavior, is shared, or must outlive the destination.

### State Ownership

- ViewModel: loaded data, business selections, validation, requests.
- Route: state collection and destination-level effects.
- Screen: layout decisions and callback wiring.
- Component: internal visual state that no caller needs.

## Events vs State

Persistent facts belong in state. Actions that must be handled once may use callbacks or an effect stream.

```kotlin
sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
    data class OpenBarcode(val membershipId: Long) : HomeEffect
}

private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
val effects = _effects.receiveAsFlow()
```

```kotlin
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            is HomeEffect.OpenBarcode -> onOpenBarcode(effect.membershipId)
        }
    }
}
```

- Do not store events as nullable state and immediately clear them.
- Prefer direct callbacks for navigation initiated by visible UI state.
- Use an effect stream when the ViewModel must initiate a one-time action.
- Ensure effect loss or duplication behavior is acceptable for the use case.

## Lifecycle and Side Effects

- Collect screen state with `collectAsStateWithLifecycle()`.
- Use `LaunchedEffect(key)` for suspend effects tied to composition.
- Use `DisposableEffect` only when registering and unregistering external listeners.
- Use `rememberUpdatedState` when a long-lived effect needs the latest callback.
- Do not launch work directly in a composable body.
- Do not use global coroutine scopes.

```kotlin
@Composable
fun LocationPermissionEffect(
    shouldRequest: Boolean,
    requestPermission: () -> Unit,
) {
    val latestRequest by rememberUpdatedState(requestPermission)
    LaunchedEffect(shouldRequest) {
        if (shouldRequest) latestRequest()
    }
}
```

## Performance and Stability

- Use immutable/stable models where practical.
- Avoid premature `@Stable` or `@Immutable`; only promise what the type satisfies.
- Use `remember` for expensive pure calculations with complete keys.
- Use `derivedStateOf` when frequently changing inputs produce less frequently changing UI state.
- Keep lambdas and objects stable when profiling shows recomposition cost.
- Never move correctness-critical work into `remember` as an optimization.
- Profile before introducing custom caching.

## Design System Usage

Screens consume semantic design tokens instead of raw visual values.

```kotlin
@Composable
fun BenefitCard(
    benefit: Benefit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(ItDaySpacing.Basic),
            verticalArrangement = Arrangement.spacedBy(ItDaySpacing.Tight),
        ) {
            Text(
                text = benefit.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = benefit.description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
```

- Use `MaterialTheme.colorScheme` for semantic colors.
- Keep palette shades such as `Primary400` inside theme construction.
- Use `ItDaySpacing`, typography, shapes, and component defaults.
- Do not repeat raw dimensions across screen code.
- Add a design-system component only when multiple features need a feature-neutral abstraction.
- Keep feature-specific cards and sections in the owning feature.

## Animation

Prefer Compose animation primitives for state-driven transitions.

```kotlin
AnimatedContent(
    targetState = uiState,
    transitionSpec = {
        fadeIn() togetherWith fadeOut()
    },
    label = "benefit-state",
) { state ->
    when (state) {
        BenefitUiState.Loading -> LoadingContent()
        is BenefitUiState.Content -> BenefitList(state.benefits, onBenefitClick)
        BenefitUiState.Empty -> EmptyBenefitContent()
        is BenefitUiState.Error -> ErrorContent(state.message, onRetry)
    }
}
```

- Animate state changes, not arbitrary recompositions.
- Give animations stable labels for tooling.
- Keep motion short and purposeful.
- Respect system reduced-motion expectations where applicable.
- Use `animate*AsState` for one value, `AnimatedVisibility` for presence, and `updateTransition` for coordinated properties.
- Add Lottie only after an approved asset requires it; it is not a default dependency.

## Images

Use Coil Compose for remote and cached images.

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(brand.imageUrl)
        .crossfade(true)
        .build(),
    contentDescription = brand.name,
    modifier = Modifier
        .size(48.dp)
        .clip(MaterialTheme.shapes.small),
    contentScale = ContentScale.Crop,
)
```

- Provide placeholders and error content when the screen depends on the image.
- Use a null content description for decorative images only.
- Avoid creating a custom `ImageLoader` per composable.
- Keep authentication headers and image URL policy in shared image/network configuration.
- Constrain image size to avoid loading unnecessarily large bitmaps.

## Permissions

Permission state belongs at the destination boundary. The reusable screen receives only the result and callbacks.

```kotlin
@Composable
fun LocationPermissionRoute(
    onGranted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isGranted by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        isGranted = granted
        if (granted) onGranted()
    }

    LocationPermissionScreen(
        isGranted = isGranted,
        onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        onContinue = onGranted,
        modifier = modifier,
    )
}
```

- Explain why a permission is needed before requesting it when appropriate.
- Handle denial and permanent denial without request loops.
- Never assume a previously granted permission remains granted.
- Keep location acquisition in `core/location`, separate from map rendering.
- Test the screen using injected permission state rather than the platform dialog.

## State Restoration

Use the smallest mechanism that satisfies restoration requirements.

- `remember`: survives recomposition only.
- `rememberSaveable`: survives recreation for saveable UI state.
- `SavedStateHandle`: restores destination/ViewModel inputs and lightweight selections.
- Repository or local storage: persists durable user data.

```kotlin
@HiltViewModel
class StoreListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StoreRepository,
) : ViewModel() {
    private val categoryId = savedStateHandle.toRoute<StoreListRoute>().categoryId

    val uiState = repository.observeStores(categoryId)
        .map(StoreListUiState::Content)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StoreListUiState.Loading,
        )
}
```

- Do not save large lists or network responses in saved state.
- Restore stable identifiers and reload data from the repository.
- Give list items stable keys so remembered item state follows identity.
- Verify process recreation for onboarding progress and important selections.

## Preview and Testing

Provide previews for reusable components and representative screen states.

```kotlin
@Preview(showBackground = true)
@Composable
private fun BenefitSearchScreenPreview() {
    ItDayTheme {
        BenefitSearchScreen(
            uiState = BenefitSearchUiState(
                query = "카페",
                benefits = previewBenefits,
            ),
            onEvent = {},
            onBenefitClick = {},
        )
    }
}
```

Use semantics-based Compose tests:

```kotlin
@get:Rule
val composeRule = createComposeRule()

@Test
fun searchButton_sendsSearchEvent() {
    var clicked = false
    composeRule.setContent {
        ItDayTheme {
            ItDayButton(
                text = "검색",
                onClick = { clicked = true },
            )
        }
    }

    composeRule.onNodeWithText("검색").performClick()
    assertTrue(clicked)
}
```

Prefer semantic text, content descriptions, roles, and test tags at stable boundaries. Do not couple tests to internal layout trees.

## Accessibility

- Keep touch targets at least 48dp unless platform guidance explicitly differs.
- Provide content descriptions for meaningful non-text visuals.
- Set decorative images to a null content description.
- Merge semantics only when the merged reading order is clearer.
- Support font scaling without clipped text.
- Do not communicate state using color alone.
- Use Material disabled, selected, error, and role semantics.

## Best Practices

1. Keep route composables thin and screens stateless.
2. Expose immutable state and explicit events.
3. Keep navigation at destination boundaries.
4. Use design-system tokens instead of repeated colors or dimensions.
5. Use `LazyColumn`/`LazyRow` for scalable lists.
6. Model loading, empty, content, and error states.
7. Collect flows with lifecycle awareness.
8. Keep side effects inside effect APIs with stable keys.
9. Add previews and semantic tests for shared UI.
10. Verify accessibility and recomposition-sensitive code.

## Related Documentation

- [Architecture Rules](architecture.md)
- [Coding Conventions](coding-conventions.md)
- [Testing Rules](testing.md)
- [Hilt DI Guide](hilt-di.md)
