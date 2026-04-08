import { ContactsApi, deduplicateContacts } from "./ContactsApi";
import { ContactsCache } from "./ContactsCache";
import { contactsConfig } from "./contactsConfig";
import { NetworkError } from "./errors";
import type { ContactsPage } from "../types/contacts";

export class ContactsRepository {
  constructor(
    private readonly api: ContactsApi,
    private readonly cache: ContactsCache,
  ) {}

  async getContacts(page: number): Promise<ContactsPage> {
    const offset = Math.max(page - 1, 0) * contactsConfig.pageSize;

    try {
      const response = await this.api.getContacts(offset, contactsConfig.pageSize);
      const uniqueContacts = deduplicateContacts(response.data);

      try {
        await this.cache.upsertContacts(uniqueContacts);
      } catch (error) {
        console.warn("Failed to update contacts cache.", error);
      }

      return {
        data: uniqueContacts,
        hasNext: response.hasNext,
        totalCount: uniqueContacts.length,
      };
    } catch (error) {
      if (!(error instanceof NetworkError)) {
        throw error;
      }

      return this.cache.getContactsPage(page);
    }
  }
}
