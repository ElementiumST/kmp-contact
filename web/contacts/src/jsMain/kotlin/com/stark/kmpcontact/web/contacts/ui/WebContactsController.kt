package com.stark.kmpcontact.web.contacts.ui

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.web.contacts.platform.WebNetworkStatusNotifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLButtonElement

private const val FIRST_PAGE = 1
private const val TOAST_DURATION_MS = 3000

class WebContactsController(
    private val root: HTMLElement,
    private val getContactsUseCase: GetContactsUseCase,
    networkStatusNotifier: WebNetworkStatusNotifier,
) {
    private val scope = MainScope()
    private var toastTimeoutHandle: Int? = null

    private var state = WebContactsState()

    init {
        networkStatusNotifier.addConnectionLostListener {
            showMessage("нет соединения с сервером")
        }
    }

    fun start() {
        render()
        loadInitial()
    }

    private fun loadInitial() {
        loadPage(page = FIRST_PAGE, replace = true)
    }

    private fun loadNextPage() {
        if (!state.hasNext || state.isLoading || state.isLoadingMore) return
        loadPage(page = state.nextPage, replace = false)
    }

    private fun loadPage(page: Int, replace: Boolean) {
        scope.launch {
            state = if (replace) {
                state.copy(isLoading = true, errorMessage = null)
            } else {
                state.copy(isLoadingMore = true, errorMessage = null)
            }
            render()

            try {
                val contactsPage = getContactsUseCase(page = page)
                val updatedContacts = if (replace) {
                    contactsPage.data
                } else {
                    (state.contacts + contactsPage.data).distinctBy { it.stableKey() }
                }

                state = state.copy(
                    contacts = updatedContacts,
                    isLoading = false,
                    isLoadingMore = false,
                    hasNext = contactsPage.hasNext,
                    nextPage = page + 1,
                    errorMessage = null,
                )
            } catch (throwable: Throwable) {
                state = state.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = throwable.message ?: "Не удалось загрузить контакты.",
                )
            }

            render()
        }
    }

    private fun showMessage(message: String) {
        state = state.copy(notification = message)
        render()

        toastTimeoutHandle?.let(window::clearTimeout)
        toastTimeoutHandle = window.setTimeout({
            state = state.copy(notification = null)
            render()
        }, TOAST_DURATION_MS)
    }

    private fun onContactSelected(contact: Contact) {
        state = state.copy(selectedContact = contact)
        render()
    }

    private fun onBackToList() {
        state = state.copy(selectedContact = null)
        render()
    }

    private fun render() {
        root.clearChildren()
        root.appendChild(createHeader("Contacts"))

        state.notification?.let { message ->
            root.appendChild(
                createBlock(
                    text = message,
                    backgroundColor = "#ffe9b3",
                    textColor = "#5c4300",
                ),
            )
        }

        val selectedContact = state.selectedContact
        if (selectedContact != null) {
            root.appendChild(renderContactDetails(selectedContact))
            return
        }

        if (state.isLoading && state.contacts.isEmpty()) {
            root.appendChild(createBlock("Loading contacts..."))
            return
        }

        if (!state.errorMessage.isNullOrBlank() && state.contacts.isEmpty()) {
            root.appendChild(
                createBlock(
                    text = state.errorMessage!!,
                    backgroundColor = "#ffd9d9",
                    textColor = "#7b1f1f",
                ),
            )
            return
        }

        root.appendChild(renderContactsList())
    }

    private fun renderContactsList(): HTMLElement {
        val container = createSection()

        if (state.contacts.isEmpty()) {
            container.appendChild(createBlock("No contacts available."))
            return container
        }

        state.contacts.forEach { contact ->
            container.appendChild(renderContactCard(contact))
        }

        if (state.isLoadingMore) {
            container.appendChild(createBlock("Loading more contacts..."))
        } else if (state.hasNext) {
            val loadMoreButton = document.createElement("button") as HTMLButtonElement
            loadMoreButton.textContent = "Load more"
            applyButtonStyle(loadMoreButton)
            loadMoreButton.addEventListener("click", {
                loadNextPage()
            })
            container.appendChild(loadMoreButton)
        }

        return container
    }

    private fun renderContactCard(contact: Contact): HTMLElement {
        val card = createSection()
        card.style.cursor = "pointer"
        card.style.marginBottom = "12px"
        card.addEventListener("click", {
            onContactSelected(contact)
        })

        card.appendChild(createTitle(contact.name))
        card.appendChild(createText(contact.phone ?: "No phone"))
        card.appendChild(createText(contact.email ?: "No email"))

        return card
    }

    private fun renderContactDetails(contact: Contact): HTMLElement {
        val container = createSection()

        val backButton = document.createElement("button") as HTMLButtonElement
        backButton.textContent = "Back"
        applyButtonStyle(backButton)
        backButton.style.marginBottom = "16px"
        backButton.addEventListener("click", {
            onBackToList()
        })
        container.appendChild(backButton)

        container.appendChild(createTitle(contact.name))
        container.appendChild(createDetailRow("email", contact.email))
        container.appendChild(createDetailRow("phone", contact.phone))
        container.appendChild(createDetailRow("interlocutorType", contact.interlocutorType))
        container.appendChild(createDetailRow("contact.contactId", contact.contact?.contactId))
        container.appendChild(createDetailRow("contact.type", contact.contact?.type))
        container.appendChild(createDetailRow("contact.ownerProfileId", contact.contact?.ownerProfileId))
        container.appendChild(createDetailRow("contact.createdAt", contact.contact?.createdAt?.toString()))
        container.appendChild(createDetailRow("contact.updatedAt", contact.contact?.updatedAt?.toString()))
        container.appendChild(createDetailRow("contact.deleted", contact.contact?.deleted?.toString()))
        container.appendChild(createDetailRow("contact.note", contact.contact?.note))
        container.appendChild(createDetailRow("contact.tags", contact.contact?.tags?.joinToString()))
        container.appendChild(createDetailRow("profile.profileId", contact.profile?.profileId))
        container.appendChild(createDetailRow("profile.userType", contact.profile?.userType))
        container.appendChild(createDetailRow("profile.avatarResourceId", contact.profile?.avatarResourceId))
        container.appendChild(createDetailRow("profile.additionalContact", contact.profile?.additionalContact))
        container.appendChild(createDetailRow("profile.aboutSelf", contact.profile?.aboutSelf))
        container.appendChild(createDetailRow("profile.companyId", contact.profile?.companyId))
        container.appendChild(createDetailRow("profile.isGuest", contact.profile?.isGuest?.toString()))
        container.appendChild(createDetailRow("profile.deleted", contact.profile?.deleted?.toString()))
        container.appendChild(createDetailRow("profile.customStatus.statusText", contact.profile?.customStatus?.statusText))
        container.appendChild(createDetailRow("ldapUser.ldapUserId", contact.ldapUser?.ldapUserId))
        container.appendChild(createDetailRow("ldapUser.targets", contact.ldapUser?.targets?.joinToString()))
        container.appendChild(createDetailRow("externalInfo.externalDomainId", contact.externalInfo?.externalDomainId))
        container.appendChild(createDetailRow("externalInfo.externalDomainName", contact.externalInfo?.externalDomainName))
        container.appendChild(createDetailRow("externalInfo.externalDomainHost", contact.externalInfo?.externalDomainHost))

        return container
    }

    private fun createHeader(text: String): HTMLElement {
        val header = document.createElement("h1") as HTMLElement
        header.textContent = text
        header.style.marginBottom = "24px"
        return header
    }

    private fun createTitle(text: String): HTMLElement {
        val title = document.createElement("h3") as HTMLElement
        title.textContent = text
        title.style.margin = "0 0 8px 0"
        return title
    }

    private fun createText(text: String): HTMLElement {
        val paragraph = document.createElement("p") as HTMLElement
        paragraph.textContent = text
        paragraph.style.margin = "4px 0"
        return paragraph
    }

    private fun createDetailRow(label: String, value: String?): HTMLElement {
        val row = document.createElement("div") as HTMLElement
        row.style.marginBottom = "8px"
        row.appendChild(createText("$label: ${value ?: "null"}"))
        return row
    }

    private fun createBlock(
        text: String,
        backgroundColor: String = "#ffffff",
        textColor: String = "#1a1a1a",
    ): HTMLElement {
        val block = createSection()
        block.style.backgroundColor = backgroundColor
        block.style.color = textColor
        block.appendChild(createText(text))
        return block
    }

    private fun createSection(): HTMLElement {
        val element = document.createElement("div") as HTMLElement
        element.style.backgroundColor = "#ffffff"
        element.style.borderRadius = "12px"
        element.style.padding = "16px"
        element.style.marginBottom = "16px"
        element.style.boxShadow = "0 2px 8px rgba(0,0,0,0.08)"
        return element
    }

    private fun applyButtonStyle(button: HTMLButtonElement) {
        button.style.padding = "10px 16px"
        button.style.border = "none"
        button.style.borderRadius = "8px"
        button.style.backgroundColor = "#2d6cdf"
        button.style.color = "#ffffff"
        button.style.cursor = "pointer"
    }

    private fun HTMLElement.clearChildren() {
        while (firstChild != null) {
            removeChild(firstChild!!)
        }
    }

    private fun Contact.stableKey(): String {
        return contact?.contactId ?: profile?.profileId ?: email ?: phone ?: name
    }
}

private data class WebContactsState(
    val contacts: List<Contact> = emptyList(),
    val selectedContact: Contact? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNext: Boolean = true,
    val nextPage: Int = FIRST_PAGE + 1,
    val notification: String? = null,
    val errorMessage: String? = null,
)
