import { useEffect, useState } from "react";
import { ContactsPresenter } from "./ContactsPresenter";
import {
  createInitialContactsViewState,
  type ContactsViewState,
} from "./ContactsViewState";

export function useContactsPresenter(presenter: ContactsPresenter): ContactsViewState {
  const [state, setState] = useState<ContactsViewState>(createInitialContactsViewState);

  useEffect(() => presenter.subscribe(setState), [presenter]);

  return state;
}
