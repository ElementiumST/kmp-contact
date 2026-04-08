import { contactsConfig } from "./contactsConfig";
import { NetworkError } from "./errors";
import { NetworkStatusNotifier } from "./NetworkStatusNotifier";
import { SessionStore } from "./SessionStore";
import type { Contact, ContactsResponse } from "../types/contacts";

interface LoginResponse {
  sessionId: string;
}

interface RequestOptions extends RequestInit {
  allowReLogin?: boolean;
}

const SESSION_HEADER = "Session";
const CONTENT_TYPE_HEADER = "Content-Type";
const APPLICATION_JSON = "application/json";

export class ContactsApi {
  constructor(
    private readonly sessionStore: SessionStore,
    private readonly networkStatusNotifier: NetworkStatusNotifier,
  ) {}

  async getContacts(offset: number, limit: number): Promise<ContactsResponse> {
    return this.requestJson<ContactsResponse>(`/contacts?offset=${offset}&limit=${limit}`, {
      method: "GET",
    });
  }

  private async requestJson<T>(path: string, options: RequestOptions): Promise<T> {
    const responseText = await this.requestText(path, options);

    try {
      return JSON.parse(responseText) as T;
    } catch (error) {
      throw new NetworkError(`Failed to parse response for ${path}.`, {
        code: 200,
      });
    }
  }

  private async requestText(path: string, options: RequestOptions): Promise<string> {
    const url = `${contactsConfig.serverUrl.replace(/\/$/, "")}${path}`;
    const allowReLogin = options.allowReLogin ?? true;
    const headers = new Headers(options.headers);
    const sessionId = this.sessionStore.getSessionId();

    if (sessionId) {
      headers.set(SESSION_HEADER, sessionId);
    }

    if (!headers.has(CONTENT_TYPE_HEADER)) {
      headers.set(CONTENT_TYPE_HEADER, APPLICATION_JSON);
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });
      const responseText = await response.text();

      if (!response.ok) {
        if (allowReLogin && response.status === 401 && !path.includes("/login")) {
          const loginResponse = await this.login();
          this.sessionStore.saveSessionId(loginResponse.sessionId);

          return this.requestText(path, {
            ...options,
            allowReLogin: false,
          });
        }

        throw new NetworkError(responseText || response.statusText, {
          code: response.status,
        });
      }

      return responseText;
    } catch (error) {
      if (error instanceof NetworkError) {
        throw error;
      }

      this.networkStatusNotifier.notifyConnectionLost();
      throw new NetworkError(
        error instanceof Error ? error.message : `Network request failed for ${url}.`,
        {
          networkLost: true,
        },
      );
    }
  }

  private async login(): Promise<LoginResponse> {
    this.sessionStore.clear();

    return this.requestJson<LoginResponse>("/login", {
      method: "POST",
      body: JSON.stringify({
        login: contactsConfig.auth.login,
        password: contactsConfig.auth.password,
        rememberMe: contactsConfig.auth.rememberMe,
      }),
      allowReLogin: false,
    });
  }
}

export function deduplicateContacts(contacts: Contact[]): Contact[] {
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
