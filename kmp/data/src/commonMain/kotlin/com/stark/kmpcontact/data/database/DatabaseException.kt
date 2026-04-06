package com.stark.kmpcontact.data.database

class DatabaseException(
    val code: Int?,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
