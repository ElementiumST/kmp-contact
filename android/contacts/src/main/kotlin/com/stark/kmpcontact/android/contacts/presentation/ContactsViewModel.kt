package com.stark.kmpcontact.android.contacts.presentation

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.support.paging.PagingState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ContactsViewModel {

    val contactsState: StateFlow<PagingState<Contact>>

    val destination: StateFlow<ContactsDestination>

    val selectedContact: StateFlow<Contact?>

    val searchQuery: StateFlow<String>

    val createDraft: StateFlow<ContactEditorState>

    val editDraft: StateFlow<ContactEditorState>

    val visibleContacts: StateFlow<List<Contact>>

    val toastMessages: SharedFlow<String>

    fun onContactClick(contact: Contact)

    fun onContactInfoBack()

    fun onSearchQueryChange(value: String)

    fun openCreateContact()

    fun dismissCreateContact()

    fun openEditContact()

    fun dismissEditContact()

    fun updateCreateField(
        field: ContactEditorField,
        value: String,
    )

    fun updateEditField(
        field: ContactEditorField,
        value: String,
    )

    fun saveCreateDraft()

    fun saveEditDraft()

    fun onMessageClick()

    fun onAudioCallClick()

    fun onVideoCallClick()

    fun loadNextPage()

    fun retryLoading()
}