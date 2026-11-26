import type { Query } from "@/lib/api/client";

/** Query keys in one place so mutations can invalidate exactly what they change. */
export const keys = {
  books: {
    all: ["books"] as const,
    list: (query: Query) => ["books", "list", query] as const,
    detail: (id: number) => ["books", "detail", id] as const,
    loans: (id: number, query: Query) => ["books", "detail", id, "loans", query] as const,
    transactions: (id: number) => ["books", "detail", id, "transactions"] as const,
    genres: ["books", "genres"] as const,
    languages: ["books", "languages"] as const,
  },
  members: {
    all: ["members"] as const,
    list: (query: Query) => ["members", "list", query] as const,
    detail: (id: number) => ["members", "detail", id] as const,
    eligibility: (id: number) => ["members", "detail", id, "eligibility"] as const,
    loans: (id: number, query: Query) => ["members", "detail", id, "loans", query] as const,
    fines: (id: number) => ["members", "detail", id, "fines"] as const,
  },
  loans: {
    all: ["loans"] as const,
    list: (query: Query) => ["loans", "list", query] as const,
    detail: (id: number) => ["loans", "detail", id] as const,
  },
  fines: {
    all: ["fines"] as const,
    list: (query: Query) => ["fines", "list", query] as const,
    summary: ["fines", "summary"] as const,
  },
  inventory: {
    all: ["inventory"] as const,
    list: (query: Query) => ["inventory", "list", query] as const,
  },
  activity: {
    all: ["activity"] as const,
    list: (query: Query) => ["activity", "list", query] as const,
  },
  dashboard: { all: ["dashboard"] as const },
  settings: ["settings"] as const,
  staff: ["staff"] as const,
  sessions: ["auth", "sessions"] as const,
  search: (q: string) => ["search", q] as const,
};
