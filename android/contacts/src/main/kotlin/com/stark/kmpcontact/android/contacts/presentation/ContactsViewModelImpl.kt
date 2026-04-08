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
class ContactsViewModelImpl @Inject constructor(
    getContactsUseCase: GetContactsUseCase,
    private val createNoteContactUseCase: CreateNoteContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    networkStatusNotifier: NetworkStatusNotifier,
) : ContactsViewModel, ViewModel() {
    private val contactsPaginator = ContactsPaginator(
        scope = viewModelScope,
        getContactsUseCase = getContactsUseCase,
    )

    override var contactsState: StateFlow<PagingState<Contact>> = contactsPaginator.state

    override val destination = MutableStateFlow(ContactsDestination.LIST)

    override val selectedContact = MutableStateFlow<Contact?>(null)

    override val searchQuery = MutableStateFlow("")

    override val createDraft = MutableStateFlow(ContactEditorState())

    override val editDraft = MutableStateFlow(ContactEditorState())

    override val toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val createdContacts = MutableStateFlow<List<Contact>>(emptyList())

    private val updatedContacts = MutableStateFlow<Map<String, Contact>>(emptyMap())

    override val visibleContacts: StateFlow<List<Contact>> = combine(
        contactsState,
        createdContacts,
        updatedContacts,
        searchQuery,
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

    init {
        contactsPaginator.refresh()

        viewModelScope.launch {
            networkStatusNotifier.connectionLostEvents.collect {
                toastMessages.emit("нет соединения с сервером")
            }
        }
    }

    override fun onContactClick(contact: Contact) {
        selectedContact.value = contact
        destination.value = ContactsDestination.INFO
    }

    override fun onContactInfoBack() {
        destination.value = ContactsDestination.LIST
        selectedContact.value = null
    }

    // 1775
    override fun onSearchQueryChange(value: String) {
        searchQuery.value = value
    }

    override fun openCreateContact() {
        createDraft.value = ContactEditorState()
        destination.value = ContactsDestination.CREATE
    }

    override fun dismissCreateContact() {
        destination.value = ContactsDestination.LIST
    }

    override fun openEditContact() {
        val currentContact = selectedContact.value ?: return
        editDraft.value = currentContact.toEditorState()
        destination.value = ContactsDestination.EDIT
    }

    override fun dismissEditContact() {
        destination.value = ContactsDestination.INFO
    }

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

    override fun saveCreateDraft() {
        val draft = createDraft.value
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
                createdContacts.update { contacts ->
                    listOf(createdContact) + contacts
                }
                selectedContact.value = createdContact
                destination.value = ContactsDestination.INFO
                createDraft.value = ContactEditorState()
                pushToast("Contact created.")
            }.onFailure { throwable ->
                pushToast(throwable.message ?: "Failed to create contact.")
            }
        }
    }

    override fun saveEditDraft() {
        val baseContact = selectedContact.value ?: return
        val draft = editDraft.value
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

                updatedContacts.update { contacts ->
                    contacts + (baseKey to updatedContact)
                }
                createdContacts.update { contacts ->
                    contacts.map { contact ->
                        if (contact.stableKey() == baseKey) {
                            updatedContact
                        } else {
                            contact
                        }
                    }
                }

                selectedContact.value = updatedContact
                destination.value = ContactsDestination.INFO
                pushToast("Contact updated.")
            }.onFailure { throwable ->
                pushToast(throwable.message ?: "Failed to update contact.")
            }
        }
    }

    override fun onMessageClick() {
        pushToast("TODO: wire message action.")
    }

    override fun onAudioCallClick() {
        pushToast("TODO: wire audio call action.")
    }

    override fun onVideoCallClick() {
        pushToast("TODO: wire video call action.")
    }

    override fun loadNextPage() {
        contactsPaginator.loadNextPage()
    }

    override fun retryLoading() {
        contactsPaginator.retry()
    }

    private fun pushToast(message: String) {
        viewModelScope.launch {
            toastMessages.emit(message)
        }
    }
}
