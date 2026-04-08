import { useEffect, useMemo } from "react";
import { ContactsApi } from "./api/ContactsApi";
import { ContactsCache } from "./api/ContactsCache";
import { ContactsRepository } from "./api/ContactsRepository";
import { NetworkStatusNotifier } from "./api/NetworkStatusNotifier";
import { SessionStore } from "./api/SessionStore";
import { ContactDetails } from "./components/ContactDetails";
import { ContactsList } from "./components/ContactsList";
import { MessageBlock } from "./components/MessageBlock";
import { Toast } from "./components/Toast";
import { ContactsPresenter } from "./presentation/ContactsPresenter";
import { useContactsPresenter } from "./presentation/useContactsPresenter";

export default function App() {
  const presenter = useMemo(() => {
    const networkStatusNotifier = new NetworkStatusNotifier();
    const sessionStore = new SessionStore();
    const api = new ContactsApi(sessionStore, networkStatusNotifier);
    const cache = new ContactsCache();
    const repository = new ContactsRepository(api, cache);

    return new ContactsPresenter(repository, networkStatusNotifier);
  }, []);

  const state = useContactsPresenter(presenter);

  useEffect(() => () => presenter.dispose(), [presenter]);

  const selectedContact = state.selectedContact;
  const pagingState = state.pagingState;

  return (
    <main className="app-shell">
      <h1 className="app-title">Contacts</h1>

      <Toast message={state.notification} />

      {selectedContact ? (
        <ContactDetails contact={selectedContact} onBackToList={presenter.onBackToList} />
      ) : null}

      {!selectedContact && pagingState.isLoading && pagingState.items.length === 0 ? (
        <MessageBlock text="Loading contacts..." />
      ) : null}

      {!selectedContact && !pagingState.isLoading && pagingState.error && pagingState.items.length === 0 ? (
        <MessageBlock
          text={pagingState.error.message}
          tone="error"
          buttonLabel="Retry"
          onButtonClick={presenter.onRetryLoading}
        />
      ) : null}

      {!selectedContact && (!pagingState.isLoading || pagingState.items.length > 0) && !(
        pagingState.error && pagingState.items.length === 0
      ) ? (
        <ContactsList
          pagingState={pagingState}
          onRetryLoading={presenter.onRetryLoading}
          onLoadNextPage={presenter.onLoadNextPage}
          onContactSelected={presenter.onContactSelected}
        />
      ) : null}
    </main>
  );
}
