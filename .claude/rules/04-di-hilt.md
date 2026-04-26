# Dependency Injection (Hilt)

## Component Scopes
- **Presenters:** `ActivityRetainedComponent` (via `@CircuitInject(Screen::class, ActivityRetainedComponent::class)`)
- **Repositories / DataSources / DataStore / Database:** `SingletonComponent`
- **UseCases:** unscoped (constructor `@Inject`, fresh instance per injection)

## Constructor Injection
- UseCases: `class Foo @Inject constructor(...)`
- Presenters: `class FooPresenter @AssistedInject constructor(@Assisted navigator: Navigator, ...)` + `@AssistedFactory @CircuitInject fun interface FooPresenterFactory`
- Repositories: `class FooRepositoryImpl @Inject constructor(private val dataSource: FooDataSource)`

## Module Locations
| Binding | Module |
|---|---|
| Repository `@Binds` (`FooRepository → FooRepositoryImpl`) | `:core:data-impl/di/RepositoryModule.kt` |
| DataSource `@Binds` (flavor-specific) | `:core:data-source/{prod,demo}/di/DataSourceModule.kt` |
| Room Database / DAO `@Provides` | `:core:data-source/prod/di/DatabaseModule.kt` |
| DataStore `@Provides` | `:core:data/datastore/di/DataStoreModule.kt` |
| Network (Retrofit, OkHttp) `@Provides` | `:core:data-source/prod/di/NetworkServiceModule.kt` |

## Rules
- Never `@AndroidEntryPoint` or `@HiltViewModel` in feature modules — Circuit Presenters replace ViewModel.
- DataSource bindings are flavor-specific. Use `prodImplementation` / `demoImplementation` in build files when needed.
- Use `@Binds` (abstract function) for interface→impl mappings; `@Provides` only when construction logic is required (e.g., Room).
- `:core:domain` has no Hilt plugin and no DI modules — UseCases are pure constructor-injected classes.
