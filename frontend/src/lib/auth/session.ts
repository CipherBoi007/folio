"use client";

import { useSyncExternalStore } from "react";
import type { StaffUser } from "@/types/api";

/**
 * The signed-in session lives in memory only. The access token is never written to storage; on reload the app
 * exchanges the HttpOnly refresh cookie for a new one (see auth-client).
 */
export type AuthState =
  | { status: "loading" }
  | { status: "unauthenticated"; reason?: "expired" | "signed-out" }
  | { status: "authenticated"; user: StaffUser; accessToken: string; expiresAt: string };

const LOADING: AuthState = { status: "loading" };

let state: AuthState = LOADING;
const listeners = new Set<() => void>();

export function getAuthState(): AuthState {
  return state;
}

export function setAuthState(next: AuthState) {
  state = next;
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useAuth(): AuthState {
  return useSyncExternalStore(subscribe, getAuthState, () => LOADING);
}

export function useCurrentUser(): StaffUser | null {
  const auth = useAuth();
  return auth.status === "authenticated" ? auth.user : null;
}

export function useIsAdmin(): boolean {
  return useCurrentUser()?.role === "ADMIN";
}
