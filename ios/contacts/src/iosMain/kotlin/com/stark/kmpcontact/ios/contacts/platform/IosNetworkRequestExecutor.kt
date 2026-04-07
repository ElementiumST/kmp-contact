package com.stark.kmpcontact.ios.contacts.platform

import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.remote.auth.LoginResponseDto
import com.stark.kmpcontact.data.remote.dto.ContactDetailsDto
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.data.remote.dto.ContactsResponseDto
import com.stark.kmpcontact.data.remote.dto.CustomStatusDto
import com.stark.kmpcontact.data.remote.dto.ExternalInfoDto
import com.stark.kmpcontact.data.remote.dto.LdapUserDto
import com.stark.kmpcontact.data.remote.dto.ProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod as KtorHttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.reflect.KClass

class IosNetworkRequestExecutor(
    private val serverUrlProvider: ServerUrlProvider,
    private val authSessionStore: IosAuthSessionStore,
    private val authConfig: IosAuthConfig,
) : NetworkRequestExecutor {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val httpClient = HttpClient(Darwin) {
        expectSuccess = false
    }

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

    private suspend fun <T : Any> executeInternal(
        url: String,
        method: HttpMethod,
        responseClass: KClass<T>,
        headers: Map<String, String>,
        requestJsonBody: String?,
        allowReLogin: Boolean,
    ): T {
        val requestHeaders = buildHeaders(headers)

        val responseBody = try {
            val response = httpClient.request {
                url(url)
                this.method = method.toKtorHttpMethod()
                requestHeaders.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
                if (requestJsonBody != null) {
                    contentType(ContentType.Application.Json)
                    setBody(requestJsonBody)
                }
            }

            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw NetworkException(
                    code = response.status.value,
                    message = body.ifBlank { response.status.description },
                )
            }

            body
        } catch (networkException: NetworkException) {
            if (allowReLogin && networkException.code == UNAUTHORIZED_CODE && !isLoginRequest(url)) {
                val loginResponse = login()
                authSessionStore.saveSessionId(loginResponse.sessionId)
                return executeInternal(
                    url = url,
                    method = method,
                    responseClass = responseClass,
                    headers = headers,
                    requestJsonBody = requestJsonBody,
                    allowReLogin = false,
                )
            }
            throw networkException
        } catch (throwable: Throwable) {
            throw NetworkException(
                code = null,
                message = throwable.message ?: "Network request failed for $url.",
                cause = throwable,
            )
        }

        return decodeResponse(
            responseClass = responseClass,
            responseBody = responseBody,
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

    private suspend fun login(): LoginResponseDto {
        authSessionStore.clear()
        val requestBody = buildJsonObject {
            put("login", authConfig.login)
            put("password", authConfig.password)
            put("rememberMe", authConfig.rememberMe)
        }.toString()

        return executeInternal(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/login",
            method = HttpMethod.POST,
            responseClass = LoginResponseDto::class,
            headers = mapOf(HttpHeaders.ContentType to ContentType.Application.Json.toString()),
            requestJsonBody = requestBody,
            allowReLogin = false,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> decodeResponse(
        responseClass: KClass<T>,
        responseBody: String,
    ): T {
        val decoded: Any = try {
            when (responseClass) {
                ContactsResponseDto::class -> parseContactsResponse(responseBody)
                LoginResponseDto::class -> parseLoginResponse(responseBody)
                else -> throw NetworkException(
                    code = null,
                    message = "Unsupported response class: ${responseClass.qualifiedName}",
                )
            }
        } catch (networkException: NetworkException) {
            throw networkException
        } catch (throwable: Throwable) {
            throw NetworkException(
                code = null,
                message = "Failed to parse response for ${responseClass.qualifiedName}.",
                cause = throwable,
            )
        }

        return decoded as T
    }

    private fun parseContactsResponse(responseBody: String): ContactsResponseDto {
        val root = responseBody.parseJsonObject()
        return ContactsResponseDto(
            data = root.requiredArray("data").map { element -> element.asContactDto() },
            hasNext = root.requiredBoolean("hasNext"),
            totalCount = root.requiredInt("totalCount"),
        )
    }

    private fun parseLoginResponse(responseBody: String): LoginResponseDto {
        val root = responseBody.parseJsonObject()
        return LoginResponseDto(
            sessionId = root.requiredString("sessionId"),
        )
    }

    private fun JsonElement.asContactDto(): ContactDto {
        val jsonObject = jsonObject
        return ContactDto(
            name = jsonObject.requiredString("name"),
            email = jsonObject.optionalString("email"),
            phone = jsonObject.optionalString("phone"),
            interlocutorType = jsonObject.requiredString("interlocutorType"),
            contact = jsonObject.optionalObject("contact")?.asContactDetailsDto(),
            profile = jsonObject.optionalObject("profile")?.asProfileDto(),
            ldapUser = jsonObject.optionalObject("ldapUser")?.asLdapUserDto(),
            externalInfo = jsonObject.optionalObject("externalInfo")?.asExternalInfoDto(),
        )
    }

    private fun JsonObject.asContactDetailsDto(): ContactDetailsDto {
        return ContactDetailsDto(
            contactId = requiredString("contactId"),
            type = requiredString("type"),
            ownerProfileId = optionalString("ownerProfileId"),
            createdAt = optionalLong("createdAt"),
            updatedAt = optionalLong("updatedAt"),
            deleted = optionalBoolean("deleted") ?: false,
            note = optionalString("note"),
            tags = optionalArray("tags")?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
        )
    }

    private fun JsonObject.asProfileDto(): ProfileDto {
        return ProfileDto(
            profileId = requiredString("profileId"),
            userType = requiredString("userType"),
            avatarResourceId = optionalString("avatarResourceId"),
            additionalContact = optionalString("additionalContact"),
            aboutSelf = optionalString("aboutSelf"),
            companyId = optionalString("companyId"),
            isGuest = optionalBoolean("isGuest") ?: false,
            deleted = optionalBoolean("deleted") ?: false,
            customStatus = optionalObject("customStatus")?.asCustomStatusDto(),
        )
    }

    private fun JsonObject.asCustomStatusDto(): CustomStatusDto {
        return CustomStatusDto(
            statusText = optionalString("statusText"),
        )
    }

    private fun JsonObject.asLdapUserDto(): LdapUserDto {
        return LdapUserDto(
            ldapUserId = requiredString("ldapUserId"),
            targets = optionalArray("targets")?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
        )
    }

    private fun JsonObject.asExternalInfoDto(): ExternalInfoDto {
        return ExternalInfoDto(
            externalDomainId = optionalString("externalDomainId"),
            externalDomainName = optionalString("externalDomainName"),
            externalDomainHost = optionalString("externalDomainHost"),
        )
    }

    private fun String.parseJsonObject(): JsonObject {
        val root = json.parseToJsonElement(this)
        return root as? JsonObject ?: throw JsonConvertException("Expected JSON object.")
    }

    private fun JsonObject.requiredString(key: String): String {
        return this[key]?.jsonPrimitive?.contentOrNull
            ?: throw JsonConvertException("Missing required string field: $key")
    }

    private fun JsonObject.requiredBoolean(key: String): Boolean {
        return this[key]?.jsonPrimitive?.booleanOrNull
            ?: throw JsonConvertException("Missing required boolean field: $key")
    }

    private fun JsonObject.requiredInt(key: String): Int {
        return this[key]?.jsonPrimitive?.intOrNull
            ?: throw JsonConvertException("Missing required int field: $key")
    }

    private fun JsonObject.requiredArray(key: String): JsonArray {
        return this[key]?.jsonArray
            ?: throw JsonConvertException("Missing required array field: $key")
    }

    private fun JsonObject.optionalString(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.optionalLong(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull

    private fun JsonObject.optionalBoolean(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

    private fun JsonObject.optionalArray(key: String): JsonArray? = this[key]?.jsonArray

    private fun JsonObject.optionalObject(key: String): JsonObject? = (this[key] as? JsonObject)

    private fun HttpMethod.toKtorHttpMethod(): KtorHttpMethod = when (this) {
        HttpMethod.GET -> KtorHttpMethod.Get
        HttpMethod.POST -> KtorHttpMethod.Post
        HttpMethod.PUT -> KtorHttpMethod.Put
        HttpMethod.PATCH -> KtorHttpMethod.Patch
        HttpMethod.DELETE -> KtorHttpMethod.Delete
    }

    private fun isLoginRequest(url: String): Boolean = url.contains("/login")

    private companion object {
        private const val SESSION_HEADER = "Session"
        private const val UNAUTHORIZED_CODE = 401
    }
}
