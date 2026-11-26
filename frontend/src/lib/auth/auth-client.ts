"use client";

import { api, configureAuthTransport } from "@/lib/api/client";
import { ApiError, isApiError } from "@/lib/api/errors";
import { getAuthState, setAuthState } from "@/lib/auth/session";
import type { TokenResponse } from "@/types/api";

/**
 * Session lifecycle in the browser:
 *  - the access token (15 min) is kept in memory only;
 *  - the refresh token is an HttpOnly, SameSite=Strict cookie the script never sees;
 *  - tokens are refreshed shortly before expiry, and on demand when the API reports TOKEN_EXPIRED;
 *  - sign-in and sign-out are broadcast so every open tab follows.
 */

const REFRESH_LEEWAY_MS = 60_000;
const channel = typeof window !== "undefined" && "BroadcastChannel" in window ? new BroadcastChannel("folio-auth") : null;

let inflight: Promise<boolean> | null = null;
let refreshTimer: ReturnType<typeof setTimeout> | null = null;

function apply(tokens: TokenResponse) {
  setAuthState({ status: "authenticated", user: tokens.user, accessToken: tokens.accessToken, expiresAt: tokens.expiresAt });
  scheduleRefresh(tokens.expiresAt);
}

function clear(reason: "expired" | "signed-out") {
  if (refreshTimer) clearTimeout(refreshTimer);
  refreshTimer = null;
  setAuthState({ status: "unauthenticated", reason });
}

function scheduleRefresh(expiresAt: string) {
  if (refreshTimer) clearTimeout(refreshTimer);
  const delay = Math.max(5_000, new Date(expiresAt).getTime() - Date.now() - REFRESH_LEEWAY_MS);
  refreshTimer = setTimeout(() => void refreshSession(), delay);
}

/** Exchanges the refresh cookie for a new access token. Concurrent callers share one request. */
export function refreshSession(): Promise<boolean> {
  inflight ??= (async () => {
    try {
      apply(await api<TokenResponse>("/auth/refresh", { method: "POST", auth: false }));
      return true;
    } catch (error) {
      if (isApiError(error) && error.status === 401) {
        clear(getAuthState().status === "authenticated" ? "expired" : "signed-out");
      } else if (getAuthState().status === "loading") {
        clear("signed-out");
      }
      return false;
    } finally {
      inflight = null;
    }
  })();
  return inflight;
}

export async function signIn(email: string, password: string): Promise<void> {
  apply(await api<TokenResponse>("/auth/login", { method: "POST", body: { email, password }, auth: false }));
  channel?.postMessage("signed-in");
}

export async function signOut(): Promise<void> {
  try {
    await api<void>("/auth/logout", { method: "POST", auth: true });
  } catch {
    // The local session ends regardless; the server session expires on its own.
  }
  clear("signed-out");
  channel?.postMessage("signed-out");
}

let started = false;

/** Called once when the app boots: restores the session from the refresh cookie and listens to other tabs. */
export function startAuth() {
  if (started) return;
  started = true;
  configureAuthTransport(refreshSession, (error: ApiError) => {
    if (error.code !== "TOKEN_EXPIRED") clear("expired");
  });
  channel?.addEventListener("message", (event: MessageEvent<string>) => {
    if (event.data === "signed-out") clear("signed-out");
    if (event.data === "signed-in" && getAuthState().status !== "authenticated") void refreshSession();
  });
  void refreshSession();
}
