import { useEffect, useState } from "react";
import { type KotlinContactsBridge } from "./kotlinBridge";
import {
  createInitialContactsViewState,
  type ContactsViewState,
} from "../types/contacts";

export function useKotlinContactsState(bridge: KotlinContactsBridge): ContactsViewState {
  const [state, setState] = useState<ContactsViewState>(createInitialContactsViewState);

  useEffect(() => bridge.subscribe(setState), [bridge]);

  return state;
}
