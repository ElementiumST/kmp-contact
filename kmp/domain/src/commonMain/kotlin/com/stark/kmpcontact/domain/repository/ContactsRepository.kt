package com.stark.kmpcontact.domain.repository

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDraft
import com.stark.kmpcontact.domain.model.ContactsPage

interface ContactsRepository {
    suspend fun getContacts(page: Int): ContactsPage

    suspend fun getContact(contactId: String): Contact?

    suspend fun createNoteContact(draft: ContactDraft): Contact

    suspend fun updateContact(
        contactId: String,
        draft: ContactDraft,
        currentContact: Contact,
    ): Contact
}
