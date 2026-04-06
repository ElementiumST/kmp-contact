package com.stark.kmpcontact.data.network

interface NetworkRequestExecutor {
    suspend fun execute(
        url: String,
        method: HttpMethod,
        headers: Map<String, String> = emptyMap(),
        requestJsonBody: String? = null,
    ): Any?
}
