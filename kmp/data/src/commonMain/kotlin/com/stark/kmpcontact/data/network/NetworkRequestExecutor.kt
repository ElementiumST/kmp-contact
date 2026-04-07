package com.stark.kmpcontact.data.network

import kotlin.reflect.KClass

interface NetworkRequestExecutor {
    suspend fun <T : Any> execute(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String> = emptyMap(),
        requestJsonBody: String? = null,
    ): T

    suspend fun executeWithoutResponse(
        url: String,
        method: HttpMethod,
        headers: Map<String, String> = emptyMap(),
        requestJsonBody: String? = null,
    )
}
