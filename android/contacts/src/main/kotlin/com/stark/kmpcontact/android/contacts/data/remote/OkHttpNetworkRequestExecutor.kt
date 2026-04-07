package com.stark.kmpcontact.android.contacts.data.remote

import android.util.Log
import com.google.gson.Gson
import com.stark.kmpcontact.android.contacts.data.auth.AuthConfig
import com.stark.kmpcontact.android.contacts.data.auth.AuthSessionStore
import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.remote.auth.LoginRequestDto
import com.stark.kmpcontact.data.remote.auth.LoginResponseDto
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
    private val serverUrlProvider: ServerUrlProvider,
    private val authSessionStore: AuthSessionStore,
    private val authConfig: AuthConfig,
    private val networkStatusNotifier: NetworkStatusNotifier,
) : NetworkRequestExecutor {

    override suspend fun <T : Any> execute(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
    ): T = withContext(Dispatchers.IO) {
        executeInternal(
            url = url,
            method = method,
            responseClass = responseClass,
            headers = headers,
            requestJsonBody = requestJsonBody,
            allowReLogin = true,
        )
    }

    private fun <T : Any> executeInternal(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
        allowReLogin: Boolean,
    ): T {
        val requestHeaders = buildHeaders(headers)

        logRequestStart(
            method = method,
            url = url,
            headers = requestHeaders,
            requestJsonBody = sanitizeBodyForLog(url, requestJsonBody),
        )

        val requestBuilder = Request.Builder()
            .url(url)

        requestHeaders.forEach { (key, value) ->
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
                    responsePreview = sanitizeBodyForLog(url, responseBody.take(LOG_PREVIEW_LIMIT))

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
            } catch (networkException: NetworkException) {
                failure = networkException
            } catch (throwable: Throwable) {
                failure = NetworkException(
                    code = null,
                    message = throwable.message ?: "Network request failed for $url.",
                    cause = throwable,
                )
            }
        }

        if (failure != null) {
            val networkFailure = failure as? NetworkException
            if (allowReLogin && networkFailure?.code == UNAUTHORIZED_CODE && !isLoginRequest(url)) {
                val loginResponse = login()
                authSessionStore.saveSessionId(loginResponse.sessionId)
                Log.d(LOG_TAG, "HTTP auth recovered: obtained new session and retrying $method $url")
                return executeInternal(
                    url = url,
                    method = method,
                    responseClass = responseClass,
                    headers = headers,
                    requestJsonBody = requestJsonBody,
                    allowReLogin = false,
                )
            }

            if (shouldNotifyConnectionLost(networkFailure)) {
                networkStatusNotifier.notifyConnectionLost()
            }

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

        return result ?: throw NetworkException(
            code = responseCode,
            message = "Network response is unexpectedly null for $url.",
        )
    }

    private fun buildHeaders(headers: Map<String, String>): Map<String, String> {
        val resolvedHeaders = headers.toMutableMap()
        val sessionId = authSessionStore.getSessionId()
        if (!sessionId.isNullOrBlank()) {
            resolvedHeaders[SESSION_HEADER] = sessionId
        }
        return resolvedHeaders
    }

    private fun login(): LoginResponseDto {
        authSessionStore.clear()

        val loginRequestJson = gson.toJson(
            LoginRequestDto(
                login = authConfig.login,
                password = authConfig.password,
                rememberMe = authConfig.rememberMe,
            ),
        )

        return executeInternal(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/login",
            method = HttpMethod.POST,
            responseClass = LoginResponseDto::class,
            headers = emptyMap(),
            requestJsonBody = loginRequestJson,
            allowReLogin = false,
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
            if (key.equals(SESSION_HEADER, ignoreCase = true) || key.equals(AUTHORIZATION_HEADER, ignoreCase = true)) {
                "$key=<redacted>"
            } else {
                "$key=${value.take(HEADER_VALUE_PREVIEW_LIMIT)}"
            }
        }
    }

    private fun sanitizeBodyForLog(
        url: String,
        body: String?,
    ): String {
        if (body.isNullOrBlank()) return "<empty>"
        if (isLoginRequest(url)) return "<redacted>"
        return body.take(LOG_PREVIEW_LIMIT)
    }

    private fun isLoginRequest(url: String): Boolean = url.contains("/login")

    private fun shouldNotifyConnectionLost(
        networkFailure: NetworkException?,
    ): Boolean = networkFailure != null && networkFailure.code == null

    private companion object {
        private const val LOG_TAG = "NetworkExecutor"
        private const val LOG_PREVIEW_LIMIT = 300
        private const val HEADER_VALUE_PREVIEW_LIMIT = 100
        private const val UNAUTHORIZED_CODE = 401
        private const val SESSION_HEADER = "Session"
        private const val AUTHORIZATION_HEADER = "Authorization"

        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val EMPTY_REQUEST_BODY = ByteArray(0).toRequestBody(JSON_MEDIA_TYPE)
    }
}
