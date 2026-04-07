package com.stark.kmpcontact.android.contacts.data.local

import android.util.Log
import android.content.ContentValues
import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class SQLiteDatabaseRequestExecutor(
    private val contactsSQLiteOpenHelper: ContactsSQLiteOpenHelper,
) : DatabaseRequestExecutor {

    override suspend fun execute(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
    ): Any? = withContext(Dispatchers.IO) {
        logRequestStart(
            statement = statement,
            operation = operation,
            arguments = arguments,
        )

        var result: Any? = null
        var failure: Throwable? = null

        val elapsedMs = measureTimeMillis {
            try {
                result = when {
                    statement == "contacts.upsert" && operation == DatabaseOperation.INSERT -> {
                        contactsSQLiteOpenHelper.writableDatabase.insertWithOnConflict(
                            "contacts",
                            null,
                            ContentValues().apply {
                                put("id", arguments["id"] as String)
                                put("name", arguments["name"] as String)
                                put("phone", arguments["phone"] as? String)
                                put("email", arguments["email"] as? String)
                                put("interlocutor_type", arguments["interlocutorType"] as String)
                            },
                            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                        )
                        null
                    }

                    statement == "contacts.findById" && operation == DatabaseOperation.QUERY -> {
                        val contactId = arguments["contactId"] as? String
                            ?: throw DatabaseException(
                                code = null,
                                message = "Missing contactId argument for contacts.findById.",
                            )

                        contactsSQLiteOpenHelper.readableDatabase.query(
                            "contacts",
                            arrayOf("id", "name", "phone", "email", "interlocutor_type"),
                            "id = ?",
                            arrayOf(contactId),
                            null,
                            null,
                            null,
                        ).use { cursor ->
                            if (!cursor.moveToFirst()) {
                                null
                            } else {
                                ContactEntity(
                                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                                    phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                                    interlocutorType = cursor.getString(cursor.getColumnIndexOrThrow("interlocutor_type")),
                                )
                            }
                        }
                    }

                    statement == "contacts.selectPage" && operation == DatabaseOperation.QUERY -> {
                        val limit = (arguments["limit"] as? Int)
                            ?: throw DatabaseException(
                                code = null,
                                message = "Missing limit argument for contacts.selectPage.",
                            )
                        val offset = (arguments["offset"] as? Int)
                            ?: throw DatabaseException(
                                code = null,
                                message = "Missing offset argument for contacts.selectPage.",
                            )

                        contactsSQLiteOpenHelper.readableDatabase.query(
                            "contacts",
                            arrayOf("id", "name", "phone", "email", "interlocutor_type"),
                            null,
                            null,
                            null,
                            null,
                            "name ASC",
                            "$offset, $limit",
                        ).use { cursor ->
                            buildList {
                                while (cursor.moveToNext()) {
                                    add(
                                        ContactEntity(
                                            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                                            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                                            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                                            interlocutorType = cursor.getString(cursor.getColumnIndexOrThrow("interlocutor_type")),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    statement == "contacts.count" && operation == DatabaseOperation.QUERY -> {
                        contactsSQLiteOpenHelper.readableDatabase.rawQuery(
                            "SELECT COUNT(*) FROM contacts",
                            null,
                        ).use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getInt(0)
                            } else {
                                0
                            }
                        }
                    }

                    else -> throw DatabaseException(
                        code = null,
                        message = "Unsupported database request: $statement / $operation",
                    )
                }
            } catch (throwable: Throwable) {
                failure = throwable
            }
        }

        try {
            if (failure != null) {
                throw failure!!
            }
        } catch (databaseException: DatabaseException) {
            logRequestFailure(
                statement = statement,
                operation = operation,
                arguments = arguments,
                elapsedMs = elapsedMs,
                throwable = databaseException,
            )
            throw databaseException
        } catch (throwable: Throwable) {
            val wrappedException = DatabaseException(
                code = null,
                message = "SQLite request failed for $statement.",
                cause = throwable,
            )
            logRequestFailure(
                statement = statement,
                operation = operation,
                arguments = arguments,
                elapsedMs = elapsedMs,
                throwable = wrappedException,
            )
            throw wrappedException
        }

        logRequestSuccess(
            statement = statement,
            operation = operation,
            arguments = arguments,
            elapsedMs = elapsedMs,
            result = result,
        )

        result
    }

    private fun logRequestStart(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
    ) {
        Log.d(
            LOG_TAG,
            "DB start: operation=$operation statement=$statement args=${arguments.formatForLog()}",
        )
    }

    private fun logRequestSuccess(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
        elapsedMs: Long,
        result: Any?,
    ) {
        Log.d(
            LOG_TAG,
            "DB success: operation=$operation statement=$statement args=${arguments.formatForLog()} elapsedMs=$elapsedMs result=${result.formatResultForLog()}",
        )
    }

    private fun logRequestFailure(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?>,
        elapsedMs: Long,
        throwable: Throwable,
    ) {
        Log.e(
            LOG_TAG,
            "DB failure: operation=$operation statement=$statement args=${arguments.formatForLog()} elapsedMs=$elapsedMs message=${throwable.message}",
            throwable,
        )
    }

    private fun Map<String, Any?>.formatForLog(): String {
        if (isEmpty()) return "{}"
        return entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "$key=${value.formatResultForLog()}"
        }
    }

    private fun Any?.formatResultForLog(): String {
        return when (this) {
            null -> "null"
            is String -> take(LOG_PREVIEW_LIMIT)
            is ContactEntity -> "ContactEntity(id=$id, name=$name)"
            is List<*> -> "List(size=$size)"
            else -> toString().take(LOG_PREVIEW_LIMIT)
        }
    }

    private companion object {
        private const val LOG_TAG = "DatabaseExecutor"
        private const val LOG_PREVIEW_LIMIT = 200
    }
}
