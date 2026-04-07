package com.stark.kmpcontact.web.contacts.platform

import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.remote.dto.ContactDetailsDto
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.data.remote.dto.ContactsResponseDto
import com.stark.kmpcontact.data.remote.dto.CustomStatusDto
import com.stark.kmpcontact.data.remote.dto.ExternalInfoDto
import com.stark.kmpcontact.data.remote.dto.LdapUserDto
import com.stark.kmpcontact.data.remote.dto.ProfileDto
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.js.JSON
import kotlin.js.json
import kotlin.reflect.KClass

class FetchNetworkRequestExecutor(
    private val serverUrlProvider: ServerUrlProvider,
    private val sessionStore: WebSessionStore,
    private val authConfig: WebAuthConfig,
    private val networkStatusNotifier: WebNetworkStatusNotifier,
) : NetworkRequestExecutor {

    override suspend fun <T : Any> execute(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
    ): T {
        return executeInternal(
            url = url,
            method = method,
            responseClass = responseClass,
            headers = headers,
            requestJsonBody = requestJsonBody,
            allowReLogin = true,
        )
    }

    override suspend fun executeWithoutResponse(
        url: String,
        method: HttpMethod,
        headers: Map<String, String>,
        requestJsonBody: String?,
    ) {
        executeWithoutResponseInternal(
            url = url,
            method = method,
            headers = headers,
            requestJsonBody = requestJsonBody,
            allowReLogin = true,
        )
    }

    private suspend fun <T : Any> executeInternal(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
        allowReLogin: Boolean,
    ): T {
        val requestHeaders = buildHeaders(headers)
        logRequestStart(method, url, requestHeaders, sanitizeBodyForLog(url, requestJsonBody))

        val responseText: String
        val statusCode: Int

        try {
            val response = window.fetch(
                input = url,
                init = RequestInit(
                    method = method.name,
                    headers = requestHeaders.toJsHeaders(),
                    body = requestJsonBody,
                ),
            ).await()

            statusCode = response.status.toInt()
            responseText = response.text().await()

            if (!response.ok) {
                throw NetworkException(
                    code = statusCode,
                    message = responseText.ifBlank { response.statusText },
                )
            }
        } catch (networkException: NetworkException) {
            if (allowReLogin && networkException.code == UNAUTHORIZED_CODE && !isLoginRequest(url)) {
                val loginResponse = login()
                sessionStore.saveSessionId(loginResponse.sessionId)
                console.log("HTTP auth recovered: retrying $method $url")
                return executeInternal(
                    url = url,
                    method = method,
                    responseClass = responseClass,
                    headers = headers,
                    requestJsonBody = requestJsonBody,
                    allowReLogin = false,
                )
            }

            if (networkException.code == null) {
                networkStatusNotifier.notifyConnectionLost()
            }

            logRequestFailure(method, url, networkException.code, sanitizeBodyForLog(url, null), networkException)
            throw networkException
        } catch (throwable: Throwable) {
            val wrapped = NetworkException(
                code = null,
                message = throwable.message ?: "Network request failed for $url.",
                cause = throwable,
            )
            networkStatusNotifier.notifyConnectionLost()
            logRequestFailure(method, url, null, "<empty>", wrapped)
            throw wrapped
        }

        return try {
            val parsed = parseResponse(
                responseClass = responseClass,
                responseText = responseText,
            )
            logRequestSuccess(method, url, statusCode, sanitizeBodyForLog(url, responseText))
            parsed
        } catch (networkException: NetworkException) {
            logRequestFailure(method, url, networkException.code, sanitizeBodyForLog(url, responseText), networkException)
            throw networkException
        } catch (throwable: Throwable) {
            val wrapped = NetworkException(
                code = statusCode,
                message = throwable.message ?: "Failed to parse response for $url.",
                cause = throwable,
            )
            logRequestFailure(method, url, statusCode, sanitizeBodyForLog(url, responseText), wrapped)
            throw wrapped
        }
    }

    private suspend fun executeWithoutResponseInternal(
        url: String,
        method: HttpMethod,
        headers: Map<String, String>,
        requestJsonBody: String?,
        allowReLogin: Boolean,
    ) {
        val requestHeaders = buildHeaders(headers)
        logRequestStart(method, url, requestHeaders, sanitizeBodyForLog(url, requestJsonBody))

        val responseText: String
        val statusCode: Int

        try {
            val response = window.fetch(
                input = url,
                init = RequestInit(
                    method = method.name,
                    headers = requestHeaders.toJsHeaders(),
                    body = requestJsonBody,
                ),
            ).await()

            statusCode = response.status.toInt()
            responseText = response.text().await()

            if (!response.ok) {
                throw NetworkException(
                    code = statusCode,
                    message = responseText.ifBlank { response.statusText },
                )
            }
        } catch (networkException: NetworkException) {
            if (allowReLogin && networkException.code == UNAUTHORIZED_CODE && !isLoginRequest(url)) {
                val loginResponse = login()
                sessionStore.saveSessionId(loginResponse.sessionId)
                console.log("HTTP auth recovered: retrying $method $url")
                return executeWithoutResponseInternal(
                    url = url,
                    method = method,
                    headers = headers,
                    requestJsonBody = requestJsonBody,
                    allowReLogin = false,
                )
            }

            if (networkException.code == null) {
                networkStatusNotifier.notifyConnectionLost()
            }

            logRequestFailure(method, url, networkException.code, sanitizeBodyForLog(url, null), networkException)
            throw networkException
        } catch (throwable: Throwable) {
            val wrapped = NetworkException(
                code = null,
                message = throwable.message ?: "Network request failed for $url.",
                cause = throwable,
            )
            networkStatusNotifier.notifyConnectionLost()
            logRequestFailure(method, url, null, "<empty>", wrapped)
            throw wrapped
        }

        logRequestSuccess(method, url, statusCode, sanitizeBodyForLog(url, responseText))
    }

    private fun buildHeaders(headers: Map<String, String>): Map<String, String> {
        val resolved = headers.toMutableMap()
        val sessionId = sessionStore.getSessionId()
        if (!sessionId.isNullOrBlank()) {
            resolved[SESSION_HEADER] = sessionId
        }
        if (resolved[CONTENT_TYPE_HEADER] == null) {
            resolved[CONTENT_TYPE_HEADER] = APPLICATION_JSON
        }
        return resolved
    }

    private suspend fun login(): LoginResponseDto {
        sessionStore.clear()

        return executeInternal(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/login",
            method = HttpMethod.POST,
            responseClass = LoginResponseDto::class,
            headers = emptyMap(),
            requestJsonBody = JSON.stringify(
                json(
                    "login" to authConfig.login,
                    "password" to authConfig.password,
                    "rememberMe" to authConfig.rememberMe,
                ),
            ),
            allowReLogin = false,
        )
    }

    private fun <T : Any> parseResponse(
        responseClass: KClass<T>,
        responseText: String,
    ): T {
        val payload = JSON.parse<dynamic>(responseText)

        @Suppress("UNCHECKED_CAST")
        return when (responseClass) {
            ContactsResponseDto::class -> parseContactsResponse(payload) as T
            LoginResponseDto::class -> LoginResponseDto(
                sessionId = safeString(payload.sessionId),
            ) as T
            else -> throw NetworkException(
                code = null,
                message = "Unsupported response class for web executor: ${responseClass.simpleName}",
            )
        }
    }

    private fun parseContactsResponse(payload: dynamic): ContactsResponseDto {
        val contacts = arrayFromDynamic(payload.data).map { parseContactDto(it) }

        return ContactsResponseDto(
            data = contacts,
            hasNext = safeBoolean(payload.hasNext),
            totalCount = safeInt(payload.totalCount),
        )
    }

    private fun parseContactDto(payload: dynamic): ContactDto {
        val contactPayload = safeNested(payload.contact)
        val profilePayload = safeNested(payload.profile)
        val ldapPayload = safeNested(payload.ldapUser)
        val externalPayload = safeNested(payload.externalInfo)
        val customStatusPayload = if (profilePayload != null) safeNested(profilePayload.customStatus) else null

        return ContactDto(
            name = safeString(payload.name),
            email = safeStringOrNull(payload.email),
            phone = safeStringOrNull(payload.phone),
            interlocutorType = safeString(payload.interlocutorType),
            contact = if (contactPayload == null) {
                null
            } else {
                ContactDetailsDto(
                    contactId = safeString(contactPayload.contactId),
                    type = safeString(contactPayload.type),
                    ownerProfileId = safeStringOrNull(contactPayload.ownerProfileId),
                    createdAt = safeLongOrNull(contactPayload.createdAt),
                    updatedAt = safeLongOrNull(contactPayload.updatedAt),
                    deleted = safeBoolean(contactPayload.deleted),
                    note = safeStringOrNull(contactPayload.note),
                    tags = arrayFromDynamic(contactPayload.tags).map { safeString(it) },
                )
            },
            profile = if (profilePayload == null) {
                null
            } else {
                ProfileDto(
                    profileId = safeString(profilePayload.profileId),
                    userType = safeString(profilePayload.userType),
                    avatarResourceId = safeStringOrNull(profilePayload.avatarResourceId),
                    additionalContact = safeStringOrNull(profilePayload.additionalContact),
                    aboutSelf = safeStringOrNull(profilePayload.aboutSelf),
                    companyId = safeStringOrNull(profilePayload.companyId),
                    isGuest = safeBoolean(profilePayload.isGuest),
                    deleted = safeBoolean(profilePayload.deleted),
                    customStatus = if (customStatusPayload == null) {
                        null
                    } else {
                        CustomStatusDto(
                            statusText = safeStringOrNull(customStatusPayload.statusText),
                        )
                    },
                )
            },
            ldapUser = if (ldapPayload == null) {
                null
            } else {
                LdapUserDto(
                    ldapUserId = safeString(ldapPayload.ldapUserId),
                    targets = arrayFromDynamic(ldapPayload.targets).map { safeString(it) },
                )
            },
            externalInfo = if (externalPayload == null) {
                null
            } else {
                ExternalInfoDto(
                    externalDomainId = safeStringOrNull(externalPayload.externalDomainId),
                    externalDomainName = safeStringOrNull(externalPayload.externalDomainName),
                    externalDomainHost = safeStringOrNull(externalPayload.externalDomainHost),
                )
            },
        )
    }

    private fun Map<String, String>.toJsHeaders(): Headers {
        val jsHeaders = Headers()
        entries.forEach { (key, value) ->
            jsHeaders.append(key, value)
        }
        return jsHeaders
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

    private fun logRequestStart(
        method: HttpMethod,
        url: String,
        headers: Map<String, String>,
        requestJsonBody: String,
    ) {
        console.log("HTTP start: method=$method url=$url headers=${headers.formatForLog()} body=$requestJsonBody")
    }

    private fun logRequestSuccess(
        method: HttpMethod,
        url: String,
        statusCode: Int,
        responsePreview: String,
    ) {
        console.log("HTTP success: method=$method url=$url code=$statusCode response=$responsePreview")
    }

    private fun logRequestFailure(
        method: HttpMethod,
        url: String,
        statusCode: Int?,
        responsePreview: String,
        throwable: Throwable,
    ) {
        console.error("HTTP failure: method=$method url=$url code=$statusCode response=$responsePreview message=${throwable.message}")
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

    private fun arrayFromDynamic(value: dynamic): List<dynamic> {
        val jsArray = js("Array.from(value || [])")
        return (jsArray as Array<dynamic>).toList()
    }

    private fun safeNested(value: dynamic): dynamic? {
        return if (isNullOrUndefined(value)) null else value
    }

    private fun safeString(value: dynamic): String = if (isNullOrUndefined(value)) "" else value.toString()

    private fun safeStringOrNull(value: dynamic): String? = if (isNullOrUndefined(value)) null else value.toString()

    private fun safeBoolean(value: dynamic): Boolean = if (isNullOrUndefined(value)) false else value as Boolean

    private fun safeInt(value: dynamic): Int = if (isNullOrUndefined(value)) 0 else (value as Number).toInt()

    private fun safeLongOrNull(value: dynamic): Long? = if (isNullOrUndefined(value)) null else (value as Number).toLong()

    private fun isNullOrUndefined(value: dynamic): Boolean = js("value === null || value === undefined") as Boolean

    private companion object {
        private const val LOG_PREVIEW_LIMIT = 300
        private const val HEADER_VALUE_PREVIEW_LIMIT = 100
        private const val UNAUTHORIZED_CODE = 401
        private const val SESSION_HEADER = "Session"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val APPLICATION_JSON = "application/json"
    }
}
