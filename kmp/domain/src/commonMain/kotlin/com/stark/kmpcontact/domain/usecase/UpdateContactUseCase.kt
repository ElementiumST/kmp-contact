package com.stark.kmpcontact.domain.usecase

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDraft
import com.stark.kmpcontact.domain.repository.ContactsRepository

class UpdateContactUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(
        contactId: String,
        draft: ContactDraft,
        currentContact: Contact,
    ): Contact = contactsRepository.updateContact(
        contactId = contactId,
        draft = draft,
        currentContact = currentContact,
    )
}
