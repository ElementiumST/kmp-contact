import type { Contact, PagingState } from "../types/contacts";
import { createInitialPagingState } from "../types/contacts";

export interface ContactsViewState {
  selectedContact: Contact | null;
  pagingState: PagingState<Contact>;
  notification: string | null;
}

export function createInitialContactsViewState(): ContactsViewState {
  return {
    selectedContact: null,
    pagingState: createInitialPagingState<Contact>(),
    notification: null,
  };
}
