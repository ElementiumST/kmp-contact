import { useMemo } from "react";
import { ContactDetails } from "./components/ContactDetails";
import { ContactsList } from "./components/ContactsList";
import { MessageBlock } from "./components/MessageBlock";
import { Toast } from "./components/Toast";
import { getKotlinContactsBridge } from "./interop/kotlinBridge";
import { useKotlinContactsState } from "./interop/useKotlinContactsState";

export default function App() {
  const bridge = useMemo(getKotlinContactsBridge, []);
  const state = useKotlinContactsState(bridge);

  const selectedContact = state.selectedContact;
  const pagingState = state.pagingState;

  return (
    <main className="app-shell">
      <h1 className="app-title">Contacts</h1>

      <Toast message={state.notification} />

      {selectedContact ? (
        <ContactDetails contact={selectedContact} onBackToList={bridge.backToList} />
      ) : null}

      {!selectedContact && pagingState.isLoading && pagingState.items.length === 0 ? (
        <MessageBlock text="Loading contacts..." />
      ) : null}

      {!selectedContact && !pagingState.isLoading && pagingState.error && pagingState.items.length === 0 ? (
        <MessageBlock
          text={pagingState.error.message}
          tone="error"
          buttonLabel="Retry"
          onButtonClick={bridge.retryLoading}
        />
      ) : null}

      {!selectedContact && (!pagingState.isLoading || pagingState.items.length > 0) && !(
        pagingState.error && pagingState.items.length === 0
      ) ? (
        <ContactsList
          pagingState={pagingState}
          onRetryLoading={bridge.retryLoading}
          onLoadNextPage={bridge.loadNextPage}
          onContactSelected={(contact) => bridge.selectContact(contact.stableKey)}
        />
      ) : null}
    </main>
  );
}
