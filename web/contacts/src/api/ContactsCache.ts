import { CacheError } from "./errors";
import type { Contact, ContactsPage } from "../types/contacts";
import { contactsConfig } from "./contactsConfig";
import { getContactStableKey } from "../types/contacts";

const DATABASE_NAME = "kmp-contact-web-db";
const DATABASE_VERSION = 1;
const STORE_NAME = "contacts";

interface CachedContactRecord {
  id: string;
  name: string;
  sortName: string;
  contact: Contact;
}

export class ContactsCache {
  async upsertContacts(contacts: Contact[]): Promise<void> {
    const database = await this.openDatabase();

    try {
      const transaction = database.transaction(STORE_NAME, "readwrite");
      const store = transaction.objectStore(STORE_NAME);

      for (const contact of contacts) {
        const record: CachedContactRecord = {
          id: getContactStableKey(contact),
          name: contact.name,
          sortName: contact.name.toLocaleLowerCase(),
          contact,
        };

        await this.awaitRequest(store.put(record));
      }
    } finally {
      database.close();
    }
  }

  async getContactsPage(page: number): Promise<ContactsPage> {
    const database = await this.openDatabase();

    try {
      const transaction = database.transaction(STORE_NAME, "readonly");
      const store = transaction.objectStore(STORE_NAME);
      const result = await this.awaitRequest<CachedContactRecord[]>(store.getAll());
      const items = Array.from(result ?? [])
        .map((record) => record.contact)
        .sort((left, right) => left.name.localeCompare(right.name));

      const offset = Math.max(page - 1, 0) * contactsConfig.pageSize;
      const pageItems = items.slice(offset, offset + contactsConfig.pageSize);

      return {
        data: pageItems,
        hasNext: offset + pageItems.length < items.length,
        totalCount: items.length,
      };
    } finally {
      database.close();
    }
  }

  private async openDatabase(): Promise<IDBDatabase> {
    const indexedDb = window.indexedDB;

    if (!indexedDb) {
      throw new CacheError("IndexedDB is not available in this browser.");
    }

    return new Promise((resolve, reject) => {
      const request = indexedDb.open(DATABASE_NAME, DATABASE_VERSION);

      request.onupgradeneeded = () => {
        const database = request.result;

        if (!database.objectStoreNames.contains(STORE_NAME)) {
          database.createObjectStore(STORE_NAME, { keyPath: "id" });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(new CacheError("Failed to open IndexedDB database."));
    });
  }

  private async awaitRequest<T>(request: IDBRequest<T>): Promise<T> {
    return new Promise((resolve, reject) => {
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(new CacheError("IndexedDB request execution failed."));
    });
  }
}
