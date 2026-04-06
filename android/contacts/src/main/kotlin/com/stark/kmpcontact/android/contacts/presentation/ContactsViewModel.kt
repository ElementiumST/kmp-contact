package com.stark.kmpcontact.android.contacts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.stark.kmpcontact.android.contacts.paging.ContactsPagingSource
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val CONTACTS_PAGE_SIZE = 20

@HiltViewModel
class ContactsViewModel @Inject constructor(
    getContactsUseCase: GetContactsUseCase,
) : ViewModel() {

    val contactsPagingData: Flow<PagingData<Contact>> = Pager(
        config = PagingConfig(pageSize = CONTACTS_PAGE_SIZE),
        pagingSourceFactory = { ContactsPagingSource(getContactsUseCase) },
    ).flow.cachedIn(viewModelScope)

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    fun onContactClick(contact: Contact) {
        _selectedContact.value = contact
    }

    fun onContactInfoBack() {
        _selectedContact.value = null
    }
}
