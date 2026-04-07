package com.stark.kmpcontact.web.contacts.platform

import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.json

class IndexedDbDatabaseRequestExecutor : DatabaseRequestExecutor {
    override suspend fun execute(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
    ): Any? {
        console.log("DB start: operation=$operation statement=$statement args=$arguments")

        val result = try {
            when {
                statement == "contacts.upsert" && operation == DatabaseOperation.INSERT -> {
                    upsertContact(arguments)
                    null
                }

                statement == "contacts.findById" && operation == DatabaseOperation.QUERY -> {
                    val contactId = arguments["contactId"] as? String
                        ?: throw DatabaseException(
                            code = null,
                            message = "Missing contactId argument for contacts.findById.",
                        )
                    findById(contactId)
                }

                statement == "contacts.selectPage" && operation == DatabaseOperation.QUERY -> {
                    val limit = arguments["limit"] as? Int
                        ?: throw DatabaseException(
                            code = null,
                            message = "Missing limit argument for contacts.selectPage.",
                        )
                    val offset = arguments["offset"] as? Int
                        ?: throw DatabaseException(
                            code = null,
                            message = "Missing offset argument for contacts.selectPage.",
                        )
                    selectPage(limit = limit, offset = offset)
                }

                statement == "contacts.count" && operation == DatabaseOperation.QUERY -> {
                    countContacts()
                }

                else -> throw DatabaseException(
                    code = null,
                    message = "Unsupported database request: $statement / $operation",
                )
            }
        } catch (databaseException: DatabaseException) {
            console.error("DB failure: operation=$operation statement=$statement message=${databaseException.message}")
            throw databaseException
        } catch (throwable: Throwable) {
            val wrapped = DatabaseException(
                code = null,
                message = "IndexedDB request failed for $statement.",
                cause = throwable,
            )
            console.error("DB failure: operation=$operation statement=$statement message=${wrapped.message}")
            throw wrapped
        }

        console.log("DB success: operation=$operation statement=$statement result=$result")
        return result
    }

    private suspend fun upsertContact(arguments: Map<String, Any?>) {
        val db = openDatabase()
        try {
            val transaction = db.transaction(STORE_NAME, "readwrite")
            val store = transaction.objectStore(STORE_NAME)
            awaitRequest(
                store.put(
                    json(
                        "id" to (arguments["id"] as String),
                        "name" to (arguments["name"] as String),
                        "phone" to arguments["phone"],
                        "email" to arguments["email"],
                        "interlocutorType" to (arguments["interlocutorType"] as String),
                    ),
                ),
            )
        } finally {
            db.close()
        }
    }

    private suspend fun findById(contactId: String): ContactEntity? {
        val db = openDatabase()
        try {
            val transaction = db.transaction(STORE_NAME, "readonly")
            val store = transaction.objectStore(STORE_NAME)
            val result = awaitRequest(store.get(contactId))
            return toContactEntityOrNull(result)
        } finally {
            db.close()
        }
    }

    private suspend fun selectPage(limit: Int, offset: Int): List<ContactEntity> {
        val db = openDatabase()
        try {
            val transaction = db.transaction(STORE_NAME, "readonly")
            val store = transaction.objectStore(STORE_NAME)
            val result = awaitRequest(store.getAll())
            return arrayFromDynamic(result)
                .mapNotNull { toContactEntityOrNull(it) }
                .sortedBy { it.name }
                .drop(offset)
                .take(limit)
        } finally {
            db.close()
        }
    }

    private suspend fun countContacts(): Int {
        val db = openDatabase()
        try {
            val transaction = db.transaction(STORE_NAME, "readonly")
            val store = transaction.objectStore(STORE_NAME)
            val result = awaitRequest(store.count())
            return (result as Number).toInt()
        } finally {
            db.close()
        }
    }

    private suspend fun openDatabase(): dynamic {
        val indexedDb = window.asDynamic().indexedDB
            ?: throw DatabaseException(
                code = null,
                message = "IndexedDB is not available in this browser.",
            )

        return suspendCoroutine { continuation ->
            val request = indexedDb.open(DATABASE_NAME, DATABASE_VERSION)

            request.onupgradeneeded = {
                val db = request.result
                db.createObjectStore(STORE_NAME, json("keyPath" to "id"))
            }
            request.onsuccess = {
                continuation.resume(request.result)
            }
            request.onerror = {
                continuation.resumeWithException(
                    DatabaseException(
                        code = null,
                        message = "Failed to open IndexedDB database.",
                    ),
                )
            }
        }
    }

    private suspend fun awaitRequest(request: dynamic): dynamic {
        return suspendCoroutine { continuation ->
            request.onsuccess = {
                continuation.resume(request.result)
            }
            request.onerror = {
                continuation.resumeWithException(
                    DatabaseException(
                        code = null,
                        message = "IndexedDB request execution failed.",
                    ),
                )
            }
        }
    }

    private fun arrayFromDynamic(value: dynamic): List<dynamic> {
        val jsArray = js("Array.from(value || [])")
        return (jsArray as Array<dynamic>).toList()
    }

    private fun toContactEntityOrNull(value: dynamic): ContactEntity? {
        if (value == null || isNullOrUndefined(value)) return null

        return ContactEntity(
            id = value.id?.toString() ?: return null,
            name = value.name?.toString() ?: return null,
            phone = value.phone?.toString(),
            email = value.email?.toString(),
            interlocutorType = value.interlocutorType?.toString() ?: "UNKNOWN",
        )
    }

    private fun isNullOrUndefined(value: dynamic): Boolean = js("value === null || value === undefined") as Boolean

    private companion object {
        const val DATABASE_NAME = "kmp-contact-web-db"
        const val DATABASE_VERSION = 1
        const val STORE_NAME = "contacts"
    }
}
