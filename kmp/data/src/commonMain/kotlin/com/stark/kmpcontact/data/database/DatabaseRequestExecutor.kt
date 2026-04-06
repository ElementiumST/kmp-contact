package com.stark.kmpcontact.data.database

interface DatabaseRequestExecutor {
    suspend fun execute(
        statement: String,
        operation: DatabaseOperation,
        arguments: Map<String, Any?> = emptyMap(),
    ): Any?
}
