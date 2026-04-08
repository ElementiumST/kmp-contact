export class NetworkError extends Error {
  readonly code: number | null;
  readonly networkLost: boolean;

  constructor(message: string, options?: { code?: number | null; networkLost?: boolean }) {
    super(message);
    this.name = "NetworkError";
    this.code = options?.code ?? null;
    this.networkLost = options?.networkLost ?? false;
  }
}

export class CacheError extends Error {
  constructor(message: string, cause?: unknown) {
    super(message);
    this.name = "CacheError";
    this.cause = cause;
  }
}
