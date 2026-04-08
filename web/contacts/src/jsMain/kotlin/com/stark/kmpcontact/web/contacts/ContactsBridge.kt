package com.stark.kmpcontact.web.contacts

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.support.paging.ContactsPaginator
import com.stark.kmpcontact.support.paging.PagingState
import com.stark.kmpcontact.web.contacts.platform.WebNetworkStatusNotifier
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TOAST_DURATION_MS = 3000

class ContactsBridge(
    getContactsUseCase: GetContactsUseCase,
    networkStatusNotifier: WebNetworkStatusNotifier,
) {
    private val scope = MainScope()
    private val contactsPaginator = ContactsPaginator(
        scope = scope,
        getContactsUseCase = getContactsUseCase,
    )
    private val listeners = mutableSetOf<(dynamic) -> Unit>()

    private var state = ContactsBridgeState()
    private var toastTimeoutHandle: Int? = null
    private var started = false

    init {
        networkStatusNotifier.addConnectionLostListener {
            showMessage("нет соединения с сервером")
        }

        contactsPaginator.state
            .onEach { pagingState ->
                state = state.copy(pagingState = pagingState)
                publish()
            }
            .launchIn(scope)
    }

    fun subscribe(listener: (dynamic) -> Unit): () -> Unit {
        listeners += listener
        listener(state.toJsState())

        if (!started) {
            started = true
            contactsPaginator.refresh()
        }

        return {
            listeners -= listener
        }
    }

    fun retryLoading() {
        contactsPaginator.retry()
    }

    fun loadNextPage() {
        contactsPaginator.loadNextPage()
    }

    fun selectContact(stableKey: String) {
        val contact = state.pagingState.items.firstOrNull { it.stableKey() == stableKey } ?: return
        state = state.copy(selectedContact = contact)
        publish()
    }

    fun backToList() {
        state = state.copy(selectedContact = null)
        publish()
    }

    fun dispose() {
        listeners.clear()
        toastTimeoutHandle?.let(window::clearTimeout)
        toastTimeoutHandle = null
        scope.cancel()
    }

    private fun showMessage(message: String) {
        state = state.copy(notification = message)
        publish()

        toastTimeoutHandle?.let(window::clearTimeout)
        toastTimeoutHandle = window.setTimeout({
            state = state.copy(notification = null)
            publish()
        }, TOAST_DURATION_MS)
    }

    private fun publish() {
        val jsState = state.toJsState()
        listeners.toList().forEach { listener ->
            listener(jsState)
        }
    }
}

private data class ContactsBridgeState(
    val selectedContact: Contact? = null,
    val pagingState: PagingState<Contact> = PagingState(),
    val notification: String? = null,
)

private fun ContactsBridgeState.toJsState(): dynamic {
    val result = jsObject()
    result.selectedContact = selectedContact?.toJsContact()
    result.pagingState = pagingState.toJsPagingState()
    result.notification = notification
    return result
}

private fun PagingState<Contact>.toJsPagingState(): dynamic {
    val result = jsObject()
    result.items = items.map { it.toJsContact() }.toTypedArray()
    result.isLoading = isLoading
    result.isLoadingMore = isLoadingMore
    result.hasNext = hasNext
    result.nextPage = nextPage
    result.error = error?.let {
        val errorObject = jsObject()
        errorObject.message = it.message
        errorObject
    }
    return result
}

private fun Contact.toJsContact(): dynamic {
    val result = jsObject()
    result.stableKey = stableKey()
    result.name = name
    result.email = email
    result.phone = phone
    result.interlocutorType = interlocutorType
    result.contact = contact?.let { details ->
        val detailsObject = jsObject()
        detailsObject.contactId = details.contactId
        detailsObject.type = details.type
        detailsObject.ownerProfileId = details.ownerProfileId
        detailsObject.createdAt = details.createdAt?.toDouble()
        detailsObject.updatedAt = details.updatedAt?.toDouble()
        detailsObject.deleted = details.deleted
        detailsObject.note = details.note
        detailsObject.tags = details.tags.toTypedArray()
        detailsObject
    }
    result.profile = profile?.let { profile ->
        val profileObject = jsObject()
        profileObject.profileId = profile.profileId
        profileObject.userType = profile.userType
        profileObject.avatarResourceId = profile.avatarResourceId
        profileObject.additionalContact = profile.additionalContact
        profileObject.aboutSelf = profile.aboutSelf
        profileObject.companyId = profile.companyId
        profileObject.isGuest = profile.isGuest
        profileObject.deleted = profile.deleted
        profileObject.customStatus = profile.customStatus?.let { status ->
            val statusObject = jsObject()
            statusObject.statusText = status.statusText
            statusObject
        }
        profileObject
    }
    result.ldapUser = ldapUser?.let { ldapUser ->
        val ldapObject = jsObject()
        ldapObject.ldapUserId = ldapUser.ldapUserId
        ldapObject.targets = ldapUser.targets.toTypedArray()
        ldapObject
    }
    result.externalInfo = externalInfo?.let { externalInfo ->
        val externalObject = jsObject()
        externalObject.externalDomainId = externalInfo.externalDomainId
        externalObject.externalDomainName = externalInfo.externalDomainName
        externalObject.externalDomainHost = externalInfo.externalDomainHost
        externalObject
    }
    return result
}

private fun Contact.stableKey(): String {
    return contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name
}

private fun jsObject(): dynamic = js("({})")
