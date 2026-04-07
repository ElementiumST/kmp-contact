package com.stark.kmpcontact.data.repository

import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import com.stark.kmpcontact.data.mapper.toDomain
import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.data.remote.dto.ContactsResponseDto
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDraft
import com.stark.kmpcontact.domain.model.ContactsPage
import com.stark.kmpcontact.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val serverUrlProvider: ServerUrlProvider,
    private val networkRequestExecutor: NetworkRequestExecutor,
    private val databaseRequestExecutor: DatabaseRequestExecutor,
) : ContactsRepository {

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

            contacts.forEach { contact ->
                cacheContact(contact)
            }

            ContactsPage(
                data = contacts,
                hasNext = networkResult.hasNext,
                totalCount = uniqueNetworkContacts.size,
            )
        } catch (_: NetworkException) {
            getContactsFromLocalDatabase(page = page)
        }
    }

    override suspend fun getContact(contactId: String): Contact? {
        return when (val databaseResult = databaseRequestExecutor.execute(
            statement = "contacts.findById",
            operation = DatabaseOperation.QUERY,
            arguments = mapOf("contactId" to contactId),
        )) {
            is Contact -> databaseResult
            is ContactEntity -> databaseResult.toDomain()
            is DatabaseException -> throw databaseResult
            null -> null
            else -> throw DatabaseException(
                code = null,
                message = "Unexpected database response for contact lookup.",
            )
        }
    }

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

    override suspend fun updateContact(
        contactId: String,
        draft: ContactDraft,
        currentContact: Contact,
    ): Contact {
        networkRequestExecutor.executeWithoutResponse(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/contacts/$contactId",
            method = HttpMethod.PATCH,
            requestJsonBody = draft.toUpdateContactJson(),
        )

        val updatedContact = currentContact.mergeWith(draft)
        cacheContact(updatedContact)
        return updatedContact
    }

    private suspend fun getContactsFromLocalDatabase(page: Int): ContactsPage {
        val pageSize = DEFAULT_PAGE_SIZE
        val offset = (page - 1).coerceAtLeast(0) * pageSize
        val cachedContacts = databaseRequestExecutor.execute(
            statement = "contacts.selectPage",
            operation = DatabaseOperation.QUERY,
            arguments = mapOf(
                "limit" to pageSize,
                "offset" to offset,
            ),
        )
        val totalCount = databaseRequestExecutor.execute(
            statement = "contacts.count",
            operation = DatabaseOperation.QUERY,
        ) as? Int ?: 0

        val cachedEntities = (cachedContacts as? List<*>)
            .orEmpty()
            .filterIsInstance<ContactEntity>()
            .distinctBy { it.stableKey() }

        if (cachedEntities.isEmpty()) {
            return ContactsPage(
                data = emptyList(),
                hasNext = false,
                totalCount = 0,
            )
        }

        return ContactsPage(
            data = cachedEntities.map(ContactEntity::toDomain),
            hasNext = offset + cachedEntities.size < totalCount,
            totalCount = cachedEntities.size,
        )
    }

    private fun ContactDto.stableKey(): String {
        return contact?.contactId
            ?: profile?.profileId
            ?: email
            ?: phone
            ?: name
    }

    private fun ContactEntity.stableKey(): String {
        return email ?: phone ?: id
    }

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

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}

private fun ContactDraft.toCreateNoteContactJson(): String {
    return buildJsonObject(
        "name" to name.trim().toJsonValue(),
        "email" to email.normalizeNullable().toJsonValue(),
        "phone" to phone.normalizeNullable().toJsonValue(),
        "note" to note.normalizeNullable().toJsonValue(),
        "tags" to tags.normalizeTags().toJsonArray(),
    )
}

private fun ContactDraft.toUpdateContactJson(): String {
    return buildJsonObject(
        "name" to name.trim().toJsonValue(),
        "email" to buildNullableValueJson(email.normalizeNullable()),
        "phone" to buildNullableValueJson(phone.normalizeNullable()),
        "note" to buildNullableValueJson(note.normalizeNullable()),
        "tags" to buildNullableListValueJson(tags.normalizeTags().ifEmpty { null }),
    )
}

private fun Contact.mergeWith(
    draft: ContactDraft,
): Contact {
    return copy(
        name = draft.name.trim(),
        email = draft.email.normalizeNullable(),
        phone = draft.phone.normalizeNullable(),
        contact = contact?.copy(
            note = draft.note.normalizeNullable(),
            tags = draft.tags.normalizeTags(),
        ),
    )
}

private fun Contact.stableCacheKey(): String {
    return contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name
}

private fun buildJsonObject(vararg fields: Pair<String, String>): String {
    return fields.joinToString(
        prefix = "{",
        postfix = "}",
        separator = ",",
    ) { (key, value) ->
        "\"${key.escapeJson()}\":$value"
    }
}

private fun buildNullableValueJson(value: String?): String {
    return buildJsonObject(
        "value" to value.toJsonValue(),
    )
}

private fun buildNullableListValueJson(values: List<String>?): String {
    val payload = values?.toJsonArray() ?: "null"

    return buildJsonObject(
        "value" to payload,
    )
}

private fun String?.normalizeNullable(): String? {
    return this?.trim()?.ifEmpty { null }
}

private fun List<String>.normalizeTags(): List<String> {
    return map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
}

private fun String?.toJsonValue(): String {
    return this?.let { "\"${it.escapeJson()}\"" } ?: "null"
}

private fun List<String>.toJsonArray(): String {
    return joinToString(
        prefix = "[",
        postfix = "]",
        separator = ",",
    ) { it.toJsonValue() }
}

private fun String.escapeJson(): String {
    return buildString(length + 8) {
        for (char in this@escapeJson) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (char < ' ') {
                        append("\\u")
                        append(char.code.toString(16).padStart(4, '0'))
                    } else {
                        append(char)
                    }
                }
            }
        }
    }
}
