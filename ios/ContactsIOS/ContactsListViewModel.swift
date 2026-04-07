import Foundation
import ContactsShared

@MainActor
final class ContactsListViewModel: ObservableObject {
    @Published private(set) var contacts: [ContactPresentationModel] = []
    @Published private(set) var isInitialLoading = false
    @Published private(set) var isLoadingNextPage = false
    @Published private(set) var initialError: String?
    @Published private(set) var appendError: String?

    private let module: IosContactsModule
    private var currentPage = 0
    private var hasNextPage = true

    init(module: IosContactsModule) {
        self.module = module
    }

    func loadIfNeeded() {
        guard contacts.isEmpty, !isInitialLoading else { return }
        Task {
            await refresh()
        }
    }

    func refresh() async {
        isInitialLoading = true
        initialError = nil
        appendError = nil
        currentPage = 0
        hasNextPage = true
        contacts = []

        await loadPage(1, mode: .initial)
    }

    func loadNextPageIfNeeded(currentItem: ContactPresentationModel) {
        guard let lastItem = contacts.last, lastItem.id == currentItem.id else { return }
        guard hasNextPage, !isInitialLoading, !isLoadingNextPage else { return }

        Task {
            await loadPage(currentPage + 1, mode: .append)
        }
    }

    private func loadPage(_ page: Int, mode: LoadingMode) async {
        switch mode {
        case .initial:
            isInitialLoading = true
            initialError = nil
        case .append:
            isLoadingNextPage = true
            appendError = nil
        }

        defer {
            isInitialLoading = false
            isLoadingNextPage = false
        }

        do {
            let pageResult = try await module.loadContactsPageAsync(page: page)
            let sharedItems = pageResult.contacts.compactMap { $0 as? IosContactItem }
            let mappedContacts = sharedItems.map(ContactPresentationModel.init(sharedItem:))

            if page == 1 {
                contacts = mappedContacts
            } else {
                contacts.append(contentsOf: mappedContacts)
            }

            currentPage = page
            hasNextPage = pageResult.hasNext
        } catch {
            let message = (error as NSError).localizedDescription
            switch mode {
            case .initial:
                initialError = message
            case .append:
                appendError = message
            }
        }
    }
}

private enum LoadingMode {
    case initial
    case append
}

struct ContactPresentationModel: Identifiable, Hashable {
    let id: String
    let name: String
    let email: String?
    let phone: String?
    let interlocutorType: String
    let contactId: String?
    let contactType: String?
    let ownerProfileId: String?
    let createdAt: Int64?
    let updatedAt: Int64?
    let contactDeleted: Bool?
    let contactNote: String?
    let contactTagsText: String?
    let profileId: String?
    let profileUserType: String?
    let avatarResourceId: String?
    let additionalContact: String?
    let aboutSelf: String?
    let companyId: String?
    let isGuest: Bool?
    let profileDeleted: Bool?
    let customStatusText: String?
    let ldapUserId: String?
    let ldapTargetsText: String?
    let externalDomainId: String?
    let externalDomainName: String?
    let externalDomainHost: String?

    init(sharedItem: IosContactItem) {
        id = sharedItem.id
        name = sharedItem.name
        email = sharedItem.email
        phone = sharedItem.phone
        interlocutorType = sharedItem.interlocutorType
        contactId = sharedItem.contactId
        contactType = sharedItem.contactType
        ownerProfileId = sharedItem.ownerProfileId
        createdAt = unwrapKotlinLong(sharedItem.createdAt)
        updatedAt = unwrapKotlinLong(sharedItem.updatedAt)
        contactDeleted = unwrapKotlinBool(sharedItem.contactDeleted)
        contactNote = sharedItem.contactNote
        contactTagsText = sharedItem.contactTagsText
        profileId = sharedItem.profileId
        profileUserType = sharedItem.profileUserType
        avatarResourceId = sharedItem.avatarResourceId
        additionalContact = sharedItem.additionalContact
        aboutSelf = sharedItem.aboutSelf
        companyId = sharedItem.companyId
        isGuest = unwrapKotlinBool(sharedItem.isGuest)
        profileDeleted = unwrapKotlinBool(sharedItem.profileDeleted)
        customStatusText = sharedItem.customStatusText
        ldapUserId = sharedItem.ldapUserId
        ldapTargetsText = sharedItem.ldapTargetsText
        externalDomainId = sharedItem.externalDomainId
        externalDomainName = sharedItem.externalDomainName
        externalDomainHost = sharedItem.externalDomainHost
    }
}
