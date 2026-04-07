package com.stark.kmpcontact.ios.contacts.platform

import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class IosDatabaseRequestExecutor(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : DatabaseRequestExecutor {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun execute(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
    ): Any? = withContext(Dispatchers.Default) {
        when {
            statement == CONTACTS_UPSERT && operation == DatabaseOperation.INSERT -> {
                upsertContact(arguments)
                null
            }

            statement == CONTACTS_FIND_BY_ID && operation == DatabaseOperation.QUERY -> {
                findContactById(arguments)
            }

            statement == CONTACTS_SELECT_PAGE && operation == DatabaseOperation.QUERY -> {
                selectContactsPage(arguments)
            }

            statement == CONTACTS_COUNT && operation == DatabaseOperation.QUERY -> {
                loadContacts().size
            }

            else -> throw DatabaseException(
                code = null,
                message = "Unsupported database request: $statement / $operation",
            )
        }
    }

    private fun upsertContact(arguments: Map<String, Any?>) {
        val id = arguments["id"] as? String
            ?: throw missingArgument("id", CONTACTS_UPSERT)
        val name = arguments["name"] as? String
            ?: throw missingArgument("name", CONTACTS_UPSERT)
        val interlocutorType = arguments["interlocutorType"] as? String
            ?: throw missingArgument("interlocutorType", CONTACTS_UPSERT)
        val phone = arguments["phone"] as? String
        val email = arguments["email"] as? String

        val updatedContacts = loadContacts()
            .filterNot { it.id == id }
            .plus(
                StoredContactEntity(
                    id = id,
                    name = name,
                    phone = phone,
                    email = email,
                    interlocutorType = interlocutorType,
                ),
            )

        saveContacts(updatedContacts)
    }

    private fun findContactById(arguments: Map<String, Any?>): ContactEntity? {
        val contactId = arguments["contactId"] as? String
            ?: throw missingArgument("contactId", CONTACTS_FIND_BY_ID)

        return loadContacts()
            .firstOrNull { it.id == contactId }
            ?.toDomain()
    }

    private fun selectContactsPage(arguments: Map<String, Any?>): List<ContactEntity> {
        val limit = arguments["limit"] as? Int
            ?: throw missingArgument("limit", CONTACTS_SELECT_PAGE)
        val offset = arguments["offset"] as? Int
            ?: throw missingArgument("offset", CONTACTS_SELECT_PAGE)

        return loadContacts()
            .sortedBy { it.name.lowercase() }
            .drop(offset)
            .take(limit)
            .map(StoredContactEntity::toDomain)
    }

    private fun loadContacts(): List<StoredContactEntity> {
        val payload = userDefaults.stringForKey(CONTACTS_CACHE_KEY).orEmpty()
        if (payload.isBlank()) return emptyList()

        return runCatching {
            json.decodeFromString<StoredContactsCache>(payload).contacts
        }.getOrElse {
            emptyList()
        }
    }

    private fun saveContacts(contacts: List<StoredContactEntity>) {
    /**
        val payload = json.encodeToString(
            StoredContactsCache(
                contacts = contacts,
            ),
        )
        userDefaults.setObject(payload, forKey = CONTACTS_CACHE_KEY)
        */
    }

    private fun missingArgument(argumentName: String, statement: String): DatabaseException {
        return DatabaseException(
            code = null,
            message = "Missing $argumentName argument for $statement.",
        )
    }

    private companion object {
        private const val CONTACTS_UPSERT = "contacts.upsert"
        private const val CONTACTS_FIND_BY_ID = "contacts.findById"
        private const val CONTACTS_SELECT_PAGE = "contacts.selectPage"
        private const val CONTACTS_COUNT = "contacts.count"
        private const val CONTACTS_CACHE_KEY = "contacts.local.cache"
    }
}

@Serializable
private data class StoredContactsCache(
    val contacts: List<StoredContactEntity> = emptyList(),
)

@Serializable
private data class StoredContactEntity(
    val id: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val interlocutorType: String,
) {
    fun toDomain(): ContactEntity {
        return ContactEntity(
            id = id,
            name = name,
            phone = phone,
            email = email,
            interlocutorType = interlocutorType,
        )
    }
}
