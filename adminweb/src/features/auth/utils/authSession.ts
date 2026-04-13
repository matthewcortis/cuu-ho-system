import type { DangNhapData } from "../api/authApi";

const AUTH_SESSION_KEY = "adminweb.auth-session";

export type AuthSession = DangNhapData;

function isObjectRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isAuthSession(value: unknown): value is AuthSession {
  if (!isObjectRecord(value)) {
    return false;
  }

  return (
    typeof value.tokenType === "string" &&
    typeof value.accessToken === "string" &&
    typeof value.expiresAt === "string" &&
    typeof value.taiKhoanId === "number" &&
    typeof value.tenDangNhap === "string" &&
    typeof value.vaiTro === "string"
  );
}

function isExpired(expiresAt: string): boolean {
  const timestamp = Date.parse(expiresAt);
  if (Number.isNaN(timestamp)) {
    return true;
  }
  return timestamp <= Date.now();
}

function parseSession(raw: string | null): AuthSession | null {
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as unknown;
    if (!isAuthSession(parsed)) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

function readSession(storage: Storage): AuthSession | null {
  const session = parseSession(storage.getItem(AUTH_SESSION_KEY));
  if (!session) {
    return null;
  }

  if (isExpired(session.expiresAt)) {
    storage.removeItem(AUTH_SESSION_KEY);
    return null;
  }

  return session;
}

export function saveAuthSession(session: AuthSession, persist: boolean): void {
  const serialized = JSON.stringify(session);
  const activeStorage = persist ? localStorage : sessionStorage;
  const inactiveStorage = persist ? sessionStorage : localStorage;

  activeStorage.setItem(AUTH_SESSION_KEY, serialized);
  inactiveStorage.removeItem(AUTH_SESSION_KEY);
}

export function getAuthSession(): AuthSession | null {
  const localSession = readSession(localStorage);
  if (localSession) {
    return localSession;
  }

  return readSession(sessionStorage);
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_SESSION_KEY);
  sessionStorage.removeItem(AUTH_SESSION_KEY);
}

export function isAuthenticated(): boolean {
  return getAuthSession() !== null;
}

export function getAuthorizationHeader(): string | null {
  const session = getAuthSession();
  if (!session) {
    return null;
  }

  return `${session.tokenType} ${session.accessToken}`;
}
