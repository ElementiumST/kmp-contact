import { ContactsRepository } from "../api/ContactsRepository";
import { NetworkStatusNotifier } from "../api/NetworkStatusNotifier";
import type { Contact } from "../types/contacts";
import {
  createInitialContactsViewState,
  type ContactsViewState,
} from "./ContactsViewState";

const TOAST_DURATION_MS = 3000;

type Listener = (state: ContactsViewState) => void;

export class ContactsPresenter {
  private readonly listeners = new Set<Listener>();
  private readonly unsubscribeNetworkListener: () => void;
  private state = createInitialContactsViewState();
  private toastTimeoutId: number | null = null;
  private started = false;
  private pendingRequest: Promise<void> | null = null;
  private lastFailedPage: number | null = null;

  constructor(
    private readonly repository: ContactsRepository,
    networkStatusNotifier: NetworkStatusNotifier,
  ) {
    this.unsubscribeNetworkListener = networkStatusNotifier.addConnectionLostListener(() => {
      this.showMessage("нет соединения с сервером");
    });
  }

  subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    listener(this.state);

    if (!this.started) {
      this.started = true;
      void this.refresh();
    }

    return () => {
      this.listeners.delete(listener);
    };
  }

  onRetryLoading = (): void => {
    if (this.pendingRequest) {
      return;
    }

    const failedPage = this.lastFailedPage ?? 1;
    void this.loadPage(failedPage, failedPage === 1);
  };

  onLoadNextPage = (): void => {
    const { pagingState } = this.state;

    if (this.pendingRequest || !pagingState.hasNext) {
      return;
    }

    void this.loadPage(pagingState.nextPage, false);
  };

  onContactSelected = (contact: Contact): void => {
    this.state = {
      ...this.state,
      selectedContact: contact,
    };
    this.publish();
  };

  onBackToList = (): void => {
    this.state = {
      ...this.state,
      selectedContact: null,
    };
    this.publish();
  };

  dispose(): void {
    this.unsubscribeNetworkListener();

    if (this.toastTimeoutId !== null) {
      window.clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }

    this.listeners.clear();
  }

  private async refresh(): Promise<void> {
    await this.loadPage(1, true);
  }

  private async loadPage(page: number, replace: boolean): Promise<void> {
    const previousPagingState = this.state.pagingState;
    this.state = {
      ...this.state,
      pagingState: replace
        ? {
            ...previousPagingState,
            isLoading: true,
            isLoadingMore: false,
            error: null,
            hasNext: true,
            nextPage: 1,
          }
        : {
            ...previousPagingState,
            isLoading: false,
            isLoadingMore: true,
            error: null,
          },
    };
    this.publish();

    const request = this.repository
      .getContacts(page)
      .then((pageResult) => {
        const mergedItems = replace
          ? pageResult.data
          : deduplicateByStableKey([...previousPagingState.items, ...pageResult.data]);

        this.state = {
          ...this.state,
          pagingState: {
            items: mergedItems,
            isLoading: false,
            isLoadingMore: false,
            hasNext: pageResult.hasNext,
            nextPage: pageResult.hasNext ? page + 1 : page,
            error: null,
          },
        };
        this.lastFailedPage = null;
      })
      .catch((error: unknown) => {
        const message =
          error instanceof Error && error.message
            ? error.message
            : replace
              ? "Failed to load items."
              : "Failed to load next page.";

        this.state = {
          ...this.state,
          pagingState: {
            ...previousPagingState,
            isLoading: false,
            isLoadingMore: false,
            error: {
              message,
            },
          },
        };
        this.lastFailedPage = page;
      })
      .finally(() => {
        if (this.pendingRequest === request) {
          this.pendingRequest = null;
        }

        this.publish();
      });

    this.pendingRequest = request;
    await request;
  }

  private showMessage(message: string): void {
    this.state = {
      ...this.state,
      notification: message,
    };
    this.publish();

    if (this.toastTimeoutId !== null) {
      window.clearTimeout(this.toastTimeoutId);
    }

    this.toastTimeoutId = window.setTimeout(() => {
      this.state = {
        ...this.state,
        notification: null,
      };
      this.publish();
      this.toastTimeoutId = null;
    }, TOAST_DURATION_MS);
  }

  private publish(): void {
    for (const listener of this.listeners) {
      listener(this.state);
    }
  }
}

function deduplicateByStableKey(contacts: Contact[]): Contact[] {
  const seen = new Set<string>();

  return contacts.filter((contact) => {
    const key =
      contact.contact?.contactId ??
      contact.profile?.profileId ??
      contact.email ??
      contact.phone ??
      contact.name;

    if (seen.has(key)) {
      return false;
    }

    seen.add(key);
    return true;
  });
}
