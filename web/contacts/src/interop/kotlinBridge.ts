import type { ContactsViewState } from "../types/contacts";

type StateListener = (state: ContactsViewState) => void;
type Unsubscribe = () => void;

export interface KotlinContactsBridge {
  subscribe(listener: StateListener): Unsubscribe;
  retryLoading(): void;
  loadNextPage(): void;
  selectContact(stableKey: string): void;
  backToList(): void;
  dispose(): void;
}

declare global {
  interface Window {
    kmpContactsBridge?: KotlinContactsBridge;
  }
}

export function getKotlinContactsBridge(): KotlinContactsBridge {
  const bridge = window.kmpContactsBridge;

  if (!bridge) {
    throw new Error("Kotlin contacts bridge is not available on window.");
  }

  return bridge;
}
