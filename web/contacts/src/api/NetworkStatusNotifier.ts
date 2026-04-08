type Listener = () => void;

export class NetworkStatusNotifier {
  private listeners = new Set<Listener>();

  addConnectionLostListener(listener: Listener): () => void {
    this.listeners.add(listener);

    return () => {
      this.listeners.delete(listener);
    };
  }

  notifyConnectionLost(): void {
    for (const listener of this.listeners) {
      listener();
    }
  }
}
