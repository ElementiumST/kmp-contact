package com.stark.kmpcontact.data.network

class NetworkException(
    val code: Int?,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
