export interface Contact {
  stableKey: string;
  name: string;
  email?: string | null;
  phone?: string | null;
  interlocutorType: string;
  contact?: ContactDetails | null;
  profile?: Profile | null;
  ldapUser?: LdapUser | null;
  externalInfo?: ExternalInfo | null;
}

export interface ContactDetails {
  contactId: string;
  type: string;
  ownerProfileId?: string | null;
  createdAt?: number | null;
  updatedAt?: number | null;
  deleted: boolean;
  note?: string | null;
  tags: string[];
}

export interface Profile {
  profileId: string;
  userType: string;
  avatarResourceId?: string | null;
  additionalContact?: string | null;
  aboutSelf?: string | null;
  companyId?: string | null;
  isGuest: boolean;
  deleted: boolean;
  customStatus?: CustomStatus | null;
}

export interface CustomStatus {
  statusText?: string | null;
}

export interface LdapUser {
  ldapUserId: string;
  targets: string[];
}

export interface ExternalInfo {
  externalDomainId?: string | null;
  externalDomainName?: string | null;
  externalDomainHost?: string | null;
}

export interface ContactsResponse {
  data: Contact[];
  hasNext: boolean;
  totalCount: number;
}

export interface ContactsPage {
  data: Contact[];
  hasNext: boolean;
  totalCount: number;
}

export interface PagingError {
  message: string;
}

export interface PagingState<T> {
  items: T[];
  isLoading: boolean;
  isLoadingMore: boolean;
  hasNext: boolean;
  nextPage: number;
  error: PagingError | null;
}

export interface ContactsViewState {
  selectedContact: Contact | null;
  pagingState: PagingState<Contact>;
  notification: string | null;
}

export function createInitialPagingState<T>(): PagingState<T> {
  return {
    items: [],
    isLoading: false,
    isLoadingMore: false,
    hasNext: true,
    nextPage: 1,
    error: null,
  };
}

export function createInitialContactsViewState(): ContactsViewState {
  return {
    selectedContact: null,
    pagingState: createInitialPagingState<Contact>(),
    notification: null,
  };
}

export function getContactStableKey(contact: Contact): string {
  return (
    contact.contact?.contactId ??
    contact.profile?.profileId ??
    contact.email ??
    contact.phone ??
    contact.name
  );
}
