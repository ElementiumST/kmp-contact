package com.stark.kmpcontact.android.contacts.presentation

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDraft

enum class ContactsDestination {
    LIST,
    INFO,
    EDIT,
    CREATE,
}

enum class ContactEditorField {
    NAME,
    EMAIL,
    PHONE,
    NOTE,
    TAGS,
}

data class ContactEditorState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val note: String = "",
    val tagsText: String = "",
)

fun ContactEditorState.update(
    field: ContactEditorField,
    value: String,
): ContactEditorState {
    return when (field) {
        ContactEditorField.NAME -> copy(name = value)
        ContactEditorField.EMAIL -> copy(email = value)
        ContactEditorField.PHONE -> copy(phone = value)
        ContactEditorField.NOTE -> copy(note = value)
        ContactEditorField.TAGS -> copy(tagsText = value)
    }
}

fun Contact.toEditorState(): ContactEditorState {
    return ContactEditorState(
        name = name,
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        note = contact?.note.orEmpty(),
        tagsText = contact?.tags?.joinToString(", ").orEmpty(),
    )
}

fun ContactEditorState.toDraft(): ContactDraft {
    return ContactDraft(
        name = name.trim(),
        email = email.trim().ifEmpty { null },
        phone = phone.trim().ifEmpty { null },
        note = note.trim().ifEmpty { null },
        tags = tagsText
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct(),
    )
}

internal fun Contact.stableKey(): String {
    return contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name
}

internal fun Contact.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true

    val normalizedQuery = query.trim().lowercase()
    val haystack = buildList {
        add(name)
        email?.let(::add)
        phone?.let(::add)
        contact?.note?.let(::add)
        addAll(contact?.tags.orEmpty())
        profile?.aboutSelf?.let(::add)
        profile?.customStatus?.statusText?.let(::add)
    }.joinToString(separator = "\n").lowercase()

    return normalizedQuery in haystack
}
