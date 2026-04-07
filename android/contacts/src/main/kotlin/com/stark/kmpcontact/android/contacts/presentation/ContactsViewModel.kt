package com.stark.kmpcontact.android.contacts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stark.kmpcontact.android.contacts.data.remote.NetworkStatusNotifier
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.CreateNoteContactUseCase
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.domain.usecase.UpdateContactUseCase
import com.stark.kmpcontact.support.paging.ContactsPaginator
import com.stark.kmpcontact.support.paging.PagingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    getContactsUseCase: GetContactsUseCase,
    private val createNoteContactUseCase: CreateNoteContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    networkStatusNotifier: NetworkStatusNotifier,
) : ViewModel() {
    private val contactsPaginator = ContactsPaginator(
        scope = viewModelScope,
        getContactsUseCase = getContactsUseCase,
    )

    val contactsState: StateFlow<PagingState<Contact>> = contactsPaginator.state

    private val _destination = MutableStateFlow(ContactsDestination.LIST)
    val destination: StateFlow<ContactsDestination> = _destination.asStateFlow()

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _createDraft = MutableStateFlow(ContactEditorState())
    val createDraft: StateFlow<ContactEditorState> = _createDraft.asStateFlow()

    private val _editDraft = MutableStateFlow(ContactEditorState())
    val editDraft: StateFlow<ContactEditorState> = _editDraft.asStateFlow()

    private val _createdContacts = MutableStateFlow<List<Contact>>(emptyList())
    private val _updatedContacts = MutableStateFlow<Map<String, Contact>>(emptyMap())

    val visibleContacts: StateFlow<List<Contact>> = combine(
        contactsState,
        _createdContacts,
        _updatedContacts,
        _searchQuery,
    ) { pagingState, createdContacts, updatedContacts, searchQuery ->
        val syncedContacts = pagingState.items.map { contact ->
            updatedContacts[contact.stableKey()] ?: contact
        }
        val allContacts = (createdContacts + syncedContacts)
            .distinctBy(Contact::stableKey)

        if (searchQuery.isBlank()) {
            allContacts
        } else {
            allContacts.filter { contact -> contact.matchesQuery(searchQuery) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    init {
        contactsPaginator.refresh()

        viewModelScope.launch {
            networkStatusNotifier.connectionLostEvents.collect {
                _toastMessages.emit("нет соединения с сервером")
            }
        }
    }

    fun onContactClick(contact: Contact) {
        _selectedContact.value = contact
        _destination.value = ContactsDestination.INFO
    }

    fun onContactInfoBack() {
        _destination.value = ContactsDestination.LIST
        _selectedContact.value = null
    }

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun openCreateContact() {
        _createDraft.value = ContactEditorState()
        _destination.value = ContactsDestination.CREATE
    }

    fun dismissCreateContact() {
        _destination.value = ContactsDestination.LIST
    }

    fun openEditContact() {
        val currentContact = _selectedContact.value ?: return
        _editDraft.value = currentContact.toEditorState()
        _destination.value = ContactsDestination.EDIT
    }

    fun dismissEditContact() {
        _destination.value = ContactsDestination.INFO
    }

    fun updateCreateField(
        field: ContactEditorField,
        value: String,
    ) {
        _createDraft.update { draft -> draft.update(field = field, value = value) }
    }

    fun updateEditField(
        field: ContactEditorField,
        value: String,
    ) {
        _editDraft.update { draft -> draft.update(field = field, value = value) }
    }

    fun saveCreateDraft() {
        val draft = _createDraft.value
        if (draft.name.isBlank()) {
            pushToast("Name is required.")
            return
        }
        if (draft.email.isBlank() && draft.phone.isBlank()) {
            pushToast("Phone or email is required.")
            return
        }

        viewModelScope.launch {
            runCatching {
                createNoteContactUseCase(draft.toDraft())
            }.onSuccess { createdContact ->
                _createdContacts.update { contacts ->
                    listOf(createdContact) + contacts
                }
                _selectedContact.value = createdContact
                _destination.value = ContactsDestination.INFO
                _createDraft.value = ContactEditorState()
                pushToast("Contact created.")
            }.onFailure { throwable ->
                pushToast(throwable.message ?: "Failed to create contact.")
            }
        }
    }

    fun saveEditDraft() {
        val baseContact = _selectedContact.value ?: return
        val draft = _editDraft.value
        if (draft.name.isBlank()) {
            pushToast("Name is required.")
            return
        }
        val contactId = baseContact.contact?.contactId
        if (contactId.isNullOrBlank()) {
            pushToast("This contact cannot be updated yet: missing contact id.")
            return
        }

        viewModelScope.launch {
            runCatching {
                updateContactUseCase(
                    contactId = contactId,
                    draft = draft.toDraft(),
                    currentContact = baseContact,
                )
            }.onSuccess { updatedContact ->
                val baseKey = baseContact.stableKey()

                _updatedContacts.update { contacts ->
                    contacts + (baseKey to updatedContact)
                }
                _createdContacts.update { contacts ->
                    contacts.map { contact ->
                        if (contact.stableKey() == baseKey) {
                            updatedContact
                        } else {
                            contact
                        }
                    }
                }

                _selectedContact.value = updatedContact
                _destination.value = ContactsDestination.INFO
                pushToast("Contact updated.")
            }.onFailure { throwable ->
                pushToast(throwable.message ?: "Failed to update contact.")
            }
        }
    }

    fun onMessageClick() {
        pushToast("TODO: wire message action.")
    }

    fun onAudioCallClick() {
        pushToast("TODO: wire audio call action.")
    }

    fun onVideoCallClick() {
        pushToast("TODO: wire video call action.")
    }

    fun loadNextPage() {
        contactsPaginator.loadNextPage()
    }

    fun retryLoading() {
        contactsPaginator.retry()
    }

    private fun pushToast(message: String) {
        viewModelScope.launch {
            _toastMessages.emit(message)
        }
    }
}
