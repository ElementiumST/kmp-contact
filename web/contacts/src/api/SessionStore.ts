const KEY_SESSION_ID = "session_id";

export class SessionStore {
  getSessionId(): string | null {
    return window.sessionStorage.getItem(KEY_SESSION_ID);
  }

  saveSessionId(sessionId: string): void {
    window.sessionStorage.setItem(KEY_SESSION_ID, sessionId);
  }

  clear(): void {
    window.sessionStorage.removeItem(KEY_SESSION_ID);
  }
}
