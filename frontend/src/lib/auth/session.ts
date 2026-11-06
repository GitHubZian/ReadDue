import { useSyncExternalStore } from "react";
import type { LoginResult, SessionUser } from "@/lib/api/models";

const STORAGE_KEY = "readdue.admin.session";

interface StoredSession {
  token: string;
  expiresAt: number;
  user: SessionUser;
}

function readStorage(): StoredSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as StoredSession;
    if (!parsed.token || !parsed.user) return null;
    if (parsed.expiresAt && parsed.expiresAt < Date.now()) {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

let current = readStorage();
const listeners = new Set<() => void>();

function emit() {
  listeners.forEach((fn) => fn());
}

export function getSession(): SessionUser | null {
  return current?.user ?? null;
}

export function getToken(): string | null {
  return current?.token ?? null;
}

export function saveSession(result: LoginResult) {
  current = {
    token: result.token,
    user: result.user,
    expiresAt: Date.now() + (Number(result.expiresIn) || 8 * 3600) * 1000,
  };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(current));
  emit();
}

export function updateUser(patch: Partial<SessionUser>) {
  if (!current) return;
  current = { ...current, user: { ...current.user, ...patch } };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(current));
  emit();
}

export function clearSession() {
  current = null;
  localStorage.removeItem(STORAGE_KEY);
  emit();
}

// 多标签页同步：其他标签退出或换账号时，这里的会话立即跟上
if (typeof window !== "undefined") {
  window.addEventListener("storage", (event) => {
    if (event.key !== STORAGE_KEY) return;
    current = readStorage();
    emit();
  });
}

function subscribe(fn: () => void) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function useSession() {
  const session = useSyncExternalStore(subscribe, getSession, () => null);
  return { user: session, isAuthenticated: Boolean(session) };
}
