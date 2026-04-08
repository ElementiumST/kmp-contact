import type { Contact } from "../types/contacts";

interface ContactDetailsProps {
  contact: Contact;
  onBackToList: () => void;
}

interface DetailItem {
  label: string;
  value: string | null | undefined;
}

export function ContactDetails({ contact, onBackToList }: ContactDetailsProps) {
  const details: DetailItem[] = [
    { label: "email", value: contact.email },
    { label: "phone", value: contact.phone },
    { label: "interlocutorType", value: contact.interlocutorType },
    { label: "contact.contactId", value: contact.contact?.contactId },
    { label: "contact.type", value: contact.contact?.type },
    { label: "contact.ownerProfileId", value: contact.contact?.ownerProfileId },
    { label: "contact.createdAt", value: contact.contact?.createdAt?.toString() },
    { label: "contact.updatedAt", value: contact.contact?.updatedAt?.toString() },
    { label: "contact.deleted", value: contact.contact?.deleted?.toString() },
    { label: "contact.note", value: contact.contact?.note },
    { label: "contact.tags", value: contact.contact?.tags?.join(", ") },
    { label: "profile.profileId", value: contact.profile?.profileId },
    { label: "profile.userType", value: contact.profile?.userType },
    { label: "profile.avatarResourceId", value: contact.profile?.avatarResourceId },
    { label: "profile.additionalContact", value: contact.profile?.additionalContact },
    { label: "profile.aboutSelf", value: contact.profile?.aboutSelf },
    { label: "profile.companyId", value: contact.profile?.companyId },
    { label: "profile.isGuest", value: contact.profile?.isGuest?.toString() },
    { label: "profile.deleted", value: contact.profile?.deleted?.toString() },
    {
      label: "profile.customStatus.statusText",
      value: contact.profile?.customStatus?.statusText,
    },
    { label: "ldapUser.ldapUserId", value: contact.ldapUser?.ldapUserId },
    { label: "ldapUser.targets", value: contact.ldapUser?.targets?.join(", ") },
    { label: "externalInfo.externalDomainId", value: contact.externalInfo?.externalDomainId },
    {
      label: "externalInfo.externalDomainName",
      value: contact.externalInfo?.externalDomainName,
    },
    {
      label: "externalInfo.externalDomainHost",
      value: contact.externalInfo?.externalDomainHost,
    },
  ];

  return (
    <section className="section">
      <button className="primary-button back-button" type="button" onClick={onBackToList}>
        Back
      </button>

      <h2 className="details-title">{contact.name}</h2>

      {details.map((detail) => (
        <div key={detail.label} className="detail-row">
          <span className="detail-label">{detail.label}:</span>
          <span className="detail-value">{detail.value ?? "null"}</span>
        </div>
      ))}
    </section>
  );
}
