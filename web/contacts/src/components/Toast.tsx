import { MessageBlock } from "./MessageBlock";

interface ToastProps {
  message: string | null;
}

export function Toast({ message }: ToastProps) {
  if (!message) {
    return null;
  }

  return <MessageBlock text={message} tone="warning" />;
}
