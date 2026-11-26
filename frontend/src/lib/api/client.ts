import { ApiError, toApiError } from "@/lib/api/errors";
import { getAuthState } from "@/lib/auth/session";

type QueryValue = string | number | boolean | null | undefined | ReadonlyArray<string | number>;
export type Query = Record<string, QueryValue>;

export interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  query?: Query;
  formData?: FormData;
  signal?: AbortSignal;
  /** Attach the access token and retry once after a silent refresh (default true). */
  auth?: boolean;
}

const API_BASE = "/api/v1";

type Refresher = () => Promise<boolean>;
type SignOutHandler = (error: ApiError) => void;

let refresher: Refresher | null = null;
let onSessionEnded: SignOutHandler | null = null;

/** Wired by auth-client so the transport can refresh tokens without importing the auth module. */
export function configureAuthTransport(refresh: Refresher, sessionEnded: SignOutHandler) {
  refresher = refresh;
  onSessionEnded = sessionEnded;
}

export function buildUrl(path: string, query?: Query): string {
  const params = new URLSearchParams();
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined || value === null || value === "") continue;
      if (Array.isArray(value)) value.forEach((v) => params.append(key, String(v)));
      else params.set(key, String(value));
    }
  }
  const qs = params.toString();
  return `${API_BASE}${path.startsWith("/") ? path : `/${path}`}${qs ? `?${qs}` : ""}`;
}

async function send(path: string, options: RequestOptions, retried = false): Promise<Response> {
  const { method = "GET", body, query, formData, signal, auth = true } = options;
  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";

  const state = getAuthState();
  if (auth && state.status === "authenticated") headers.Authorization = `Bearer ${state.accessToken}`;

  let response: Response;
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers,
      body: formData ?? (body === undefined ? undefined : JSON.stringify(body)),
      signal,
      credentials: "same-origin",
      cache: "no-store",
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw ApiError.network();
  }

  if (response.ok) return response;

  const error = await toApiError(response);
  if (auth && response.status === 401) {
    if (error.code === "TOKEN_EXPIRED" && !retried && refresher && (await refresher())) {
      return send(path, options, true);
    }
    onSessionEnded?.(error);
  }
  throw error;
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await send(path, options);
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

/** Fetches a file endpoint (CSV export) with auth and saves it under the server-provided name. */
export async function download(path: string, query?: Query, fallbackName = "export.csv"): Promise<void> {
  const response = await send(path, { query });
  const disposition = response.headers.get("Content-Disposition") ?? "";
  const match = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i.exec(disposition);
  const filename = match ? decodeURIComponent(match[1] ?? match[2]) : fallbackName;
  const href = URL.createObjectURL(await response.blob());
  const link = document.createElement("a");
  link.href = href;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(href);
}
