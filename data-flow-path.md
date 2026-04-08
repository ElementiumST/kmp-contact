# Путь данных: от нажатия на кнопку до отображения на экране

Ниже схема для текущего проекта `kmp-contact`: как пользовательское действие проходит через `UI -> ViewModel -> Domain -> Data -> platform executors` и возвращается обратно в состояние экрана.

## Общая схема

```text
Пользователь
  |
  v
Compose UI
  |
  | onClick / onValueChange
  v
ViewModel
  |
  | обновляет локальный state
  | и при необходимости запускает coroutine
  v
UseCase (domain)
  |
  v
Repository interface (domain contract)
  |
  v
Repository implementation (data)
  |
  +--> NetworkRequestExecutor
  |
  +--> DatabaseRequestExecutor
  |
  v
Domain model / result / error
  |
  v
ViewModel обновляет StateFlow / SharedFlow
  |
  v
Compose collectAsState() / LaunchedEffect
  |
  v
Экран перерисовывается
```

## Ключевая идея

- `UI` не знает, как устроены сеть и база;
- `ViewModel` координирует сценарий и хранит состояние экрана;
- `domain` задает контракт и use case;
- `data` решает, откуда брать данные: сеть, локальный кеш, комбинированный сценарий;
- затем результат снова поднимается вверх в `ViewModel`, а Compose автоматически отображает новое состояние.

## Схема в виде стрелок

```text
Button click
  -> Composable callback
  -> ViewModel method
  -> UseCase
  -> Repository
  -> Executor(s)
  -> Repository maps result to domain model
  -> ViewModel updates StateFlow
  -> Compose observes StateFlow
  -> UI recomposes
```

## Сценарий 1: пользователь нажал на контакт в списке

### Что происходит

1. В `ListItemContact` пользователь нажимает на карточку контакта.
2. `ContactsScreen` передает событие в `viewModel.onContactClick(contact)`.
3. `ViewModel` кладет контакт в `selectedContact` и меняет `destination` на экран деталей.
4. Compose видит новое состояние через `collectAsState()`.
5. Вместо списка отображается `ContactInfoScreen`.

## Схема

```text
ListItemContact.onClick
  -> ContactsScreen.onContactClick(contact)
  -> ContactsViewModel.onContactClick(contact)
  -> selectedContact.value = contact
  -> destination.value = INFO
  -> collectAsState() получает новое состояние
  -> ContactsScreen переключает route
  -> ContactInfoScreen(contact) отображается
```

## Привязка к коду

`ListItemContact` отдает наружу callback:

```24:33:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/ui/ListItemContact.kt
fun ListItemContact(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
```

`ContactsScreen` прокидывает событие во `ViewModel`:

```58:68:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/ui/ContactsScreen.kt
ContactsDestination.LIST -> ContactsListRoute(
    contacts = visibleContacts,
    contactsState = contactsState,
    searchQuery = searchQuery,
    onSearchChange = viewModel::onSearchQueryChange,
    onCreateClick = viewModel::openCreateContact,
    onRetry = viewModel::retryLoading,
    onContactClick = viewModel::onContactClick,
    onLoadMore = viewModel::loadNextPage,
```

Во `ViewModel` меняется state:

```89:92:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/presentation/ContactsViewModelImpl.kt
override fun onContactClick(contact: Contact) {
    selectedContact.value = contact
    destination.value = ContactsDestination.INFO
}
```

## Сценарий 2: пользователь меняет поля и нажимает `Save`

Это самый показательный путь, потому что здесь задействованы все слои.

### Пошаговый поток

1. Пользователь вводит текст в `ContactEditorScreen`.
2. Каждый `onValueChange` идет в `ViewModel.updateCreateField(...)` или `updateEditField(...)`.
3. `ViewModel` обновляет `createDraft` или `editDraft`.
4. Когда пользователь нажимает `Save`, вызывается `saveCreateDraft()` или `saveEditDraft()`.
5. `ViewModel` валидирует данные и запускает coroutine.
6. `ViewModel` вызывает use case из `domain`.
7. Use case вызывает `ContactsRepository`.
8. Реализация `ContactsRepositoryImpl` обращается к сети.
9. После успешного ответа репозиторий маппит DTO в domain model.
10. Репозиторий обновляет локальный кеш через `DatabaseRequestExecutor`.
11. Готовый `Contact` возвращается обратно во `ViewModel`.
12. `ViewModel` обновляет `createdContacts`, `updatedContacts`, `selectedContact`, `destination`.
13. `visibleContacts` пересчитывается через `combine(...)`.
14. Compose получает новые значения через `collectAsState()`.
15. Экран автоматически перерисовывается и показывает обновленные данные.

## Схема сохранения

```text
User presses Save
  -> ContactEditorScreen.onSave
  -> ContactsViewModel.saveCreateDraft() / saveEditDraft()
  -> validate input
  -> CreateNoteContactUseCase / UpdateContactUseCase
  -> ContactsRepository
  -> ContactsRepositoryImpl
  -> OkHttpNetworkRequestExecutor
  -> server response DTO
  -> map DTO -> Contact
  -> SQLiteDatabaseRequestExecutor caches contact
  -> Contact returns to ViewModel
  -> ViewModel updates flows
  -> Compose recomposes
  -> new screen state is visible
```

## Привязка к коду

Экран редактора передает изменения и сохранение наружу:

```29:35:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/ui/ContactEditorScreen.kt
fun ContactEditorScreen(
    title: String,
    state: ContactEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onFieldChange: (ContactEditorField, String) -> Unit,
```

Кнопка `Save` вызывает callback:

```53:63:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/ui/ContactEditorScreen.kt
actions = {
    TextButton(
        onClick = onSave,
        enabled = state.name.isNotBlank(),
    ) {
        Text(
            text = "Save",
            color = if (state.name.isNotBlank()) MainTabBarIcons else Color.Gray,
        )
    }
}
```

`ContactsScreen` подставляет методы `ViewModel`:

```100:107:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/ui/ContactsScreen.kt
ContactEditorScreen(
    title = "Create contact",
    state = createDraft,
    onBack = viewModel::dismissCreateContact,
    onSave = viewModel::saveCreateDraft,
    onFieldChange = viewModel::updateCreateField,
    modifier = modifier,
)
```

`ViewModel` обновляет draft:

```122:134:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/presentation/ContactsViewModelImpl.kt
override fun updateCreateField(
    field: ContactEditorField,
    value: String,
) {
    createDraft.update { draft -> draft.update(field = field, value = value) }
}

override fun updateEditField(
    field: ContactEditorField,
    value: String,
) {
    editDraft.update { draft -> draft.update(field = field, value = value) }
}
```

`ViewModel` запускает use case:

```136:160:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/presentation/ContactsViewModelImpl.kt
override fun saveCreateDraft() {
    val draft = createDraft.value
    // ...
    viewModelScope.launch {
        runCatching {
            createNoteContactUseCase(draft.toDraft())
        }.onSuccess { createdContact ->
            createdContacts.update { contacts ->
                listOf(createdContact) + contacts
            }
            selectedContact.value = createdContact
            destination.value = ContactsDestination.INFO
```

Use case делегирует работу в репозиторий:

```7:12:C:/Users/stark/StudioProjects/kmp-contact/kmp/domain/src/commonMain/kotlin/com/stark/kmpcontact/domain/usecase/CreateNoteContactUseCase.kt
class CreateNoteContactUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(
        draft: ContactDraft,
    ): Contact = contactsRepository.createNoteContact(draft)
}
```

Репозиторий вызывает сеть и кеш:

```68:78:C:/Users/stark/StudioProjects/kmp-contact/kmp/data/src/commonMain/kotlin/com/stark/kmpcontact/data/repository/ContactsRepositoryImpl.kt
override suspend fun createNoteContact(draft: ContactDraft): Contact {
    val response = networkRequestExecutor.execute(
        url = "${serverUrlProvider.serverUrl.trimEnd('/')}/contacts/create-note",
        method = HttpMethod.POST,
        responseClass = ContactDto::class,
        requestJsonBody = draft.toCreateNoteContactJson(),
    )

    val createdContact = response.toDomain()
    cacheContact(createdContact)
    return createdContact
}
```

Сетевая реализация исполняет запрос:

```31:46:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/data/remote/OkHttpNetworkRequestExecutor.kt
override suspend fun <T : Any> execute(
    url: String,
    method: HttpMethod,
    responseClass: KClass<T>,
    headers: Map<String, String>,
    requestJsonBody: String?,
): T = withContext(Dispatchers.IO) {
    executeInternal(
        url = url,
        method = method,
        responseClass = responseClass,
        headers = headers,
        requestJsonBody = requestJsonBody,
        allowReLogin = true,
    )
}
```

Локальный кеш пишется через DB executor:

```145:156:C:/Users/stark/StudioProjects/kmp-contact/kmp/data/src/commonMain/kotlin/com/stark/kmpcontact/data/repository/ContactsRepositoryImpl.kt
private suspend fun cacheContact(contact: Contact) {
    databaseRequestExecutor.execute(
        statement = "contacts.upsert",
        operation = DatabaseOperation.INSERT,
        arguments = mapOf(
            "id" to contact.stableCacheKey(),
            "name" to contact.name,
            "phone" to contact.phone,
            "email" to contact.email,
            "interlocutorType" to contact.interlocutorType,
        ),
    )
}
```

## Сценарий 3: первая загрузка списка контактов

Это поток не от кнопки, а от старта экрана, но он очень важен для понимания обратного движения данных.

### Пошаговый поток

1. `ContactsScreen` получает `ContactsViewModel`.
2. В `ContactsViewModelImpl.init` вызывается `contactsPaginator.refresh()`.
3. `ContactsPaginator` вызывает `GetContactsUseCase(page)`.
4. Use case вызывает `ContactsRepository.getContacts(page)`.
5. `ContactsRepositoryImpl` идет в сеть.
6. Если сеть успешна, DTO преобразуются в `Contact`, затем кэшируются в SQLite.
7. Если сеть недоступна, репозиторий делает fallback в локальную базу.
8. Результат попадает в `PagingState`.
9. `contactsState` меняется.
10. `visibleContacts` пересчитывается.
11. Compose перечитывает `contactsState` и `visibleContacts`.
12. На экране отображается список.

## Схема загрузки списка

```text
Screen opened
  -> ViewModel init
  -> ContactsPaginator.refresh()
  -> GetContactsUseCase(page=1)
  -> ContactsRepository.getContacts(page=1)
  -> network request
      -> success -> map DTO -> cache in DB
      -> failure -> fallback to DB
  -> ContactsPage
  -> PagingState<Contact>
  -> visibleContacts
  -> LazyColumn(items = contacts)
```

## Привязка к коду

Во `ViewModel` загрузка стартует в `init`:

```79:87:C:/Users/stark/StudioProjects/kmp-contact/android/contacts/src/main/kotlin/com/stark/kmpcontact/android/contacts/presentation/ContactsViewModelImpl.kt
init {
    contactsPaginator.refresh()

    viewModelScope.launch {
        networkStatusNotifier.connectionLostEvents.collect {
            toastMessages.emit("нет соединения с сервером")
        }
    }
}
```

`ContactsPaginator` вызывает use case:

```138:147:C:/Users/stark/StudioProjects/kmp-contact/kmp/support/src/commonMain/kotlin/com/stark/kmpcontact/support/paging/ContactsPaginator.kt
private val delegate = PageNumberPaginator(
    scope = scope,
    loadPage = { page ->
        val contactsPage = getContactsUseCase(page = page)
        PagingChunk(
            items = contactsPage.data,
            hasNext = contactsPage.hasNext,
        )
    },
```

Репозиторий делает network-first с fallback в базу:

```25:48:C:/Users/stark/StudioProjects/kmp-contact/kmp/data/src/commonMain/kotlin/com/stark/kmpcontact/data/repository/ContactsRepositoryImpl.kt
override suspend fun getContacts(page: Int): ContactsPage {
    return try {
        val limit = DEFAULT_PAGE_SIZE
        val offset = (page - 1).coerceAtLeast(0) * limit
        val networkResult = networkRequestExecutor.execute(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/contacts?offset=$offset&limit=$limit",
            method = HttpMethod.GET,
            responseClass = ContactsResponseDto::class,
        )
        val uniqueNetworkContacts = networkResult.data.distinctBy { it.stableKey() }
        val contacts = uniqueNetworkContacts.map { it.toDomain() }
```

```46:48:C:/Users/stark/StudioProjects/kmp-contact/kmp/data/src/commonMain/kotlin/com/stark/kmpcontact/data/repository/ContactsRepositoryImpl.kt
    } catch (_: NetworkException) {
        getContactsFromLocalDatabase(page = page)
    }
```

## Что идет обратно вверх

Когда нижние слои закончили работу, вверх возвращается не UI-специфичный объект, а уже нормализованный результат:

- `Contact`;
- `ContactsPage`;
- исключение с понятным сообщением;
- либо обновленный `PagingState`.

Это важно, потому что `UI` не знает о `DTO`, SQL, `OkHttp`, JSON и деталях авторизации.

## Короткая формула

```text
Событие вниз:
UI -> ViewModel -> UseCase -> Repository -> Executor

Данные вверх:
Executor -> Repository -> Domain model/result -> ViewModel state -> Compose UI
```

## Почему это удобно

- изменения в сети не должны ломать экран напрямую;
- замена базы или API ограничена нижними слоями;
- `ViewModel` удобно тестировать отдельно от Compose;
- `domain` можно переиспользовать для других платформ;
- поток данных остается предсказуемым: событие вниз, состояние вверх.
