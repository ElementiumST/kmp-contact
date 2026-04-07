package com.stark.kmpcontact.domain.usecase

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDraft
import com.stark.kmpcontact.domain.repository.ContactsRepository

class CreateNoteContactUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(
        draft: ContactDraft,
    ): Contact = contactsRepository.createNoteContact(draft)
}
