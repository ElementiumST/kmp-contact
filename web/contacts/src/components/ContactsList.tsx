import type { Contact, PagingState } from "../types/contacts";
import { MessageBlock } from "./MessageBlock";

interface ContactsListProps {
  pagingState: PagingState<Contact>;
  onRetryLoading: () => void;
  onLoadNextPage: () => void;
  onContactSelected: (contact: Contact) => void;
}

export function ContactsList({
  pagingState,
  onRetryLoading,
  onLoadNextPage,
  onContactSelected,
}: ContactsListProps) {
  if (pagingState.items.length === 0) {
    return <MessageBlock text="No contacts available." />;
  }

  return (
    <>
      {pagingState.items.map((contact) => {
        const key =
          contact.contact?.contactId ??
          contact.profile?.profileId ??
          contact.email ??
          contact.phone ??
          contact.name;

        return (
          <section
            key={key}
            className="section contact-card"
            onClick={() => onContactSelected(contact)}
          >
            <h2 className="contact-card__title">{contact.name}</h2>
            <p className="contact-card__text">{contact.phone ?? "No phone"}</p>
            <p className="contact-card__text">{contact.email ?? "No email"}</p>
          </section>
        );
      })}

      {pagingState.isLoadingMore ? <MessageBlock text="Loading more contacts..." /> : null}

      {!pagingState.isLoadingMore && pagingState.error?.message ? (
        <MessageBlock
          text={pagingState.error.message}
          tone="error"
          buttonLabel="Retry"
          onButtonClick={onRetryLoading}
        />
      ) : null}

      {!pagingState.isLoadingMore && !pagingState.error && pagingState.hasNext ? (
        <div className="actions-row">
          <button className="primary-button" type="button" onClick={onLoadNextPage}>
            Load more
          </button>
        </div>
      ) : null}
    </>
  );
}
