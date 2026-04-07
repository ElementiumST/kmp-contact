import SwiftUI

struct ContactDetailView: View {
    let contact: ContactPresentationModel

    var body: some View {
        List {
            DetailRow(label: "name", value: contact.name)
            DetailRow(label: "email", value: contact.email)
            DetailRow(label: "phone", value: contact.phone)
            DetailRow(label: "interlocutorType", value: contact.interlocutorType)

            DetailRow(label: "contact.contactId", value: contact.contactId)
            DetailRow(label: "contact.type", value: contact.contactType)
            DetailRow(label: "contact.ownerProfileId", value: contact.ownerProfileId)
            DetailRow(label: "contact.createdAt", value: contact.createdAt.map(String.init))
            DetailRow(label: "contact.updatedAt", value: contact.updatedAt.map(String.init))
            DetailRow(label: "contact.deleted", value: contact.contactDeleted.map(String.init))
            DetailRow(label: "contact.note", value: contact.contactNote)
            DetailRow(label: "contact.tags", value: contact.contactTagsText)

            DetailRow(label: "profile.profileId", value: contact.profileId)
            DetailRow(label: "profile.userType", value: contact.profileUserType)
            DetailRow(label: "profile.avatarResourceId", value: contact.avatarResourceId)
            DetailRow(label: "profile.additionalContact", value: contact.additionalContact)
            DetailRow(label: "profile.aboutSelf", value: contact.aboutSelf)
            DetailRow(label: "profile.companyId", value: contact.companyId)
            DetailRow(label: "profile.isGuest", value: contact.isGuest.map(String.init))
            DetailRow(label: "profile.deleted", value: contact.profileDeleted.map(String.init))
            DetailRow(label: "profile.customStatus.statusText", value: contact.customStatusText)

            DetailRow(label: "ldapUser.ldapUserId", value: contact.ldapUserId)
            DetailRow(label: "ldapUser.targets", value: contact.ldapTargetsText)

            DetailRow(label: "externalInfo.externalDomainId", value: contact.externalDomainId)
            DetailRow(label: "externalInfo.externalDomainName", value: contact.externalDomainName)
            DetailRow(label: "externalInfo.externalDomainHost", value: contact.externalDomainHost)
        }
        .navigationTitle(contact.name)
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct DetailRow: View {
    let label: String
    let value: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value ?? "null")
                .font(.body)
        }
        .padding(.vertical, 4)
    }
}
