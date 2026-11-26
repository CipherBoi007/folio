"use client";

import { useCallback, useMemo } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";

type Value = string | number | boolean | null | undefined | readonly (string | number)[];

/**
 * Filters, sort and page live in the URL so views are shareable and survive reloads.
 * Changing any filter resets the page unless `page` itself is being set.
 */
export function useUrlState() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const get = useCallback((key: string) => searchParams.get(key) ?? undefined, [searchParams]);
  const getAll = useCallback((key: string) => searchParams.getAll(key), [searchParams]);
  const getNumber = useCallback(
    (key: string, fallback: number) => {
      const raw = searchParams.get(key);
      const parsed = raw === null ? NaN : Number(raw);
      return Number.isFinite(parsed) ? parsed : fallback;
    },
    [searchParams],
  );

  const set = useCallback(
    (updates: Record<string, Value>) => {
      const next = new URLSearchParams(searchParams.toString());
      for (const [key, value] of Object.entries(updates)) {
        next.delete(key);
        if (Array.isArray(value)) value.forEach((v) => next.append(key, String(v)));
        else if (value !== undefined && value !== null && value !== "" && value !== false) next.set(key, String(value));
      }
      if (!("page" in updates)) next.delete("page");
      const qs = next.toString();
      router.replace(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
    },
    [pathname, router, searchParams],
  );

  return useMemo(() => ({ get, getAll, getNumber, set, params: searchParams }), [get, getAll, getNumber, set, searchParams]);
}
