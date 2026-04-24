export interface StompFrame {
  command: string;
  headers: Record<string, string>;
  body: string;
}

type ConnectionState = "connecting" | "connected" | "disconnected";

type SubscriptionHandler = (frame: StompFrame) => void;

interface Subscription {
  id: string;
  handler: SubscriptionHandler;
}

interface StompClientOptions {
  url: string;
  connectHeaders: Record<string, string>;
  reconnectDelayMs?: number;
  onConnectionStateChange?: (state: ConnectionState) => void;
  onError?: (message: string) => void;
}

function parseFrame(rawFrame: string): StompFrame | null {
  const normalized = rawFrame.replace(/^\n+/, "").replace(/\n+$/, "");
  if (!normalized) {
    return null;
  }

  const lines = normalized.split("\n");
  const command = lines[0]?.trim();
  if (!command) {
    return null;
  }

  const headers: Record<string, string> = {};
  let bodyIndex = 1;

  for (let index = 1; index < lines.length; index += 1) {
    const line = lines[index] ?? "";
    if (line === "") {
      bodyIndex = index + 1;
      break;
    }

    const delimiterIndex = line.indexOf(":");
    if (delimiterIndex <= 0) {
      continue;
    }

    const key = line.slice(0, delimiterIndex).trim();
    const value = line.slice(delimiterIndex + 1).trim();
    if (key) {
      headers[key] = value;
    }
  }

  return {
    command,
    headers,
    body: lines.slice(bodyIndex).join("\n"),
  };
}

export class StompClient {
  private readonly reconnectDelayMs: number;
  private readonly onConnectionStateChange?: (state: ConnectionState) => void;
  private readonly onError?: (message: string) => void;
  private socket: WebSocket | null = null;
  private reconnectTimer: number | null = null;
  private subscriptions = new Map<string, Subscription>();
  private subscriptionIdCounter = 0;
  private manuallyDisconnected = false;
  private buffer = "";
  private stompConnected = false;

  constructor(private readonly options: StompClientOptions) {
    this.reconnectDelayMs = options.reconnectDelayMs ?? 3000;
    this.onConnectionStateChange = options.onConnectionStateChange;
    this.onError = options.onError;
  }

  connect() {
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return;
    }

    this.manuallyDisconnected = false;
    this.onConnectionStateChange?.("connecting");

    this.socket = new WebSocket(this.options.url);
    this.socket.onopen = () => {
      this.stompConnected = false;
      const connectHeaders = {
        "accept-version": "1.2",
        "heart-beat": "10000,10000",
        ...this.options.connectHeaders,
      };
      this.sendFrame("CONNECT", connectHeaders);
    };

    this.socket.onmessage = (event) => {
      if (typeof event.data !== "string") {
        return;
      }

      this.buffer += event.data;
      let frameTerminatorIndex = this.buffer.indexOf("\0");

      while (frameTerminatorIndex >= 0) {
        const rawFrame = this.buffer.slice(0, frameTerminatorIndex);
        this.buffer = this.buffer.slice(frameTerminatorIndex + 1);

        const parsed = parseFrame(rawFrame);
        if (parsed) {
          this.handleFrame(parsed);
        }

        frameTerminatorIndex = this.buffer.indexOf("\0");
      }
    };

    this.socket.onerror = () => {
      this.onError?.("Khong the ket noi WebSocket.");
    };

    this.socket.onclose = () => {
      this.stompConnected = false;
      this.onConnectionStateChange?.("disconnected");
      this.socket = null;
      this.buffer = "";

      if (!this.manuallyDisconnected) {
        this.scheduleReconnect();
      }
    };
  }

  disconnect() {
    this.manuallyDisconnected = true;

    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (!this.socket) {
      return;
    }

    if (this.socket.readyState === WebSocket.OPEN) {
      this.sendFrame("DISCONNECT");
    }

    this.socket.close();
    this.socket = null;
    this.buffer = "";
    this.stompConnected = false;
  }

  subscribe(destination: string, handler: SubscriptionHandler): () => void {
    const existing = this.subscriptions.get(destination);
    if (existing) {
      this.subscriptions.set(destination, {
        ...existing,
        handler,
      });
      return () => this.unsubscribe(destination);
    }

    const subscription: Subscription = {
      id: this.nextSubscriptionId(),
      handler,
    };

    this.subscriptions.set(destination, subscription);

    if (this.isConnected()) {
      this.sendFrame("SUBSCRIBE", {
        id: subscription.id,
        destination,
      });
    }

    return () => this.unsubscribe(destination);
  }

  send(destination: string, body: string, headers?: Record<string, string>) {
    if (!this.isConnected()) {
      return;
    }

    this.sendFrame(
      "SEND",
      {
        destination,
        "content-type": "application/json",
        ...(headers ?? {}),
      },
      body
    );
  }

  private unsubscribe(destination: string) {
    const current = this.subscriptions.get(destination);
    if (!current) {
      return;
    }

    if (this.isConnected()) {
      this.sendFrame("UNSUBSCRIBE", {
        id: current.id,
      });
    }

    this.subscriptions.delete(destination);
  }

  private handleFrame(frame: StompFrame) {
    switch (frame.command) {
      case "CONNECTED": {
        this.stompConnected = true;
        this.onConnectionStateChange?.("connected");
        this.resubscribeAll();
        return;
      }
      case "MESSAGE": {
        const destination = frame.headers.destination;
        if (!destination) {
          return;
        }

        const subscription = this.subscriptions.get(destination);
        subscription?.handler(frame);
        return;
      }
      case "ERROR": {
        this.onError?.(frame.body || "Ket noi STOMP bi tu choi.");
        return;
      }
      default:
        return;
    }
  }

  private resubscribeAll() {
    const currentEntries = Array.from(this.subscriptions.entries());
    this.subscriptions = new Map(
      currentEntries.map(([destination, subscription]) => [
        destination,
        {
          id: this.nextSubscriptionId(),
          handler: subscription.handler,
        },
      ])
    );

    this.subscriptions.forEach((subscription, destination) => {
      this.sendFrame("SUBSCRIBE", {
        id: subscription.id,
        destination,
      });
    });
  }

  private sendFrame(command: string, headers?: Record<string, string>, body = "") {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }

    const headerLines = headers
      ? Object.entries(headers)
          .filter(([key]) => key.trim().length > 0)
          .map(([key, value]) => `${key}:${value}`)
          .join("\n")
      : "";
    const headersBlock = headerLines.length > 0 ? `${headerLines}\n` : "";
    const frame = `${command}\n${headersBlock}\n${body}\0`;
    this.socket.send(frame);
  }

  private scheduleReconnect() {
    if (this.reconnectTimer !== null) {
      return;
    }

    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      this.connect();
    }, this.reconnectDelayMs);
  }

  private isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN && this.stompConnected;
  }

  private nextSubscriptionId(): string {
    this.subscriptionIdCounter += 1;
    return `sub-${this.subscriptionIdCounter}`;
  }
}
