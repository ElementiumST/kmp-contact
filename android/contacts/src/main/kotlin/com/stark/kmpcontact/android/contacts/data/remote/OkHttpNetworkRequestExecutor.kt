package com.stark.kmpcontact.android.contacts.data.remote

import android.util.Log
import com.google.gson.Gson
import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

class OkHttpNetworkRequestExecutor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) : NetworkRequestExecutor {

    override suspend fun <T : Any> execute(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
    ): T = withContext(Dispatchers.IO) {
        logRequestStart(
            method = method,
            url = url,
            headers = headers,
            requestJsonBody = requestJsonBody,
        )

        val requestBuilder = Request.Builder()
            .url(url)

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val requestBody = requestJsonBody?.toRequestBody(JSON_MEDIA_TYPE)

        when (method) {
            HttpMethod.GET -> requestBuilder.get()
            HttpMethod.POST -> requestBuilder.post(requestBody ?: EMPTY_REQUEST_BODY)
            HttpMethod.PUT -> requestBuilder.put(requestBody ?: EMPTY_REQUEST_BODY)
            HttpMethod.PATCH -> requestBuilder.patch(requestBody ?: EMPTY_REQUEST_BODY)
            HttpMethod.DELETE -> {
                if (requestBody == null) {
                    requestBuilder.delete()
                } else {
                    requestBuilder.delete(requestBody)
                }
            }
        }

        var result: T? = null
        var responseCode: Int? = null
        var responsePreview: String? = null
        var failure: Throwable? = null

        val elapsedMs = measureTimeMillis {
            try {
                okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    responseCode = response.code
                    responsePreview = responseBody.take(LOG_PREVIEW_LIMIT)

                    if (!response.isSuccessful) {
                        throw NetworkException(
                            code = response.code,
                            message = responseBody.ifBlank { response.message },
                        )
                    }

                    result = try {
                        gson.fromJson(responseBody, responseClass.java)
                            ?: throw NetworkException(
                                code = response.code,
                                message = "Response body is empty for ${response.request.url}.",
                            )
                    } catch (networkException: NetworkException) {
                        throw networkException
                    } catch (throwable: Throwable) {
                        throw NetworkException(
                            code = response.code,
                            message = "Failed to parse response for ${response.request.url}.",
                            cause = throwable,
                        )
                    }
                }
            } catch (throwable: Throwable) {
                failure = throwable
            }
        }

        if (failure != null) {
            logRequestFailure(
                method = method,
                url = url,
                elapsedMs = elapsedMs,
                responseCode = responseCode,
                throwable = failure!!,
                responsePreview = responsePreview,
            )
            throw failure!!
        }

        logRequestSuccess(
            method = method,
            url = url,
            elapsedMs = elapsedMs,
            responseCode = responseCode,
            responsePreview = responsePreview,
        )

        result ?: throw NetworkException(
            code = responseCode,
            message = "Network response is unexpectedly null for $url.",
        )
    }

    private fun logRequestStart(
        method: HttpMethod,
        url: String,
        headers: Map<String, String>,
        requestJsonBody: String?,
    ) {
        Log.d(
            LOG_TAG,
            "HTTP start: method=$method url=$url headers=${headers.formatForLog()} body=${requestJsonBody.previewForLog()}",
        )
    }

    private fun logRequestSuccess(
        method: HttpMethod,
        url: String,
        elapsedMs: Long,
        responseCode: Int?,
        responsePreview: String?,
    ) {
        Log.d(
            LOG_TAG,
            "HTTP success: method=$method url=$url code=$responseCode elapsedMs=$elapsedMs response=${responsePreview.previewForLog()}",
        )
    }

    private fun logRequestFailure(
        method: HttpMethod,
        url: String,
        elapsedMs: Long,
        responseCode: Int?,
        throwable: Throwable,
        responsePreview: String?,
    ) {
        Log.e(
            LOG_TAG,
            "HTTP failure: method=$method url=$url code=$responseCode elapsedMs=$elapsedMs response=${responsePreview.previewForLog()} message=${throwable.message}",
            throwable,
        )
    }

    private fun String?.previewForLog(): String {
        if (this.isNullOrBlank()) return "<empty>"
        return take(LOG_PREVIEW_LIMIT)
    }

    private fun Map<String, String>.formatForLog(): String {
        if (isEmpty()) return "{}"
        return entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "$key=${value.take(HEADER_VALUE_PREVIEW_LIMIT)}"
        }
    }

    private companion object {
        private const val LOG_TAG = "NetworkExecutor"
        private const val LOG_PREVIEW_LIMIT = 300
        private const val HEADER_VALUE_PREVIEW_LIMIT = 100

        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val EMPTY_REQUEST_BODY = ByteArray(0).toRequestBody(JSON_MEDIA_TYPE)
    }
}
