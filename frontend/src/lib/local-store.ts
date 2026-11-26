"use client";

import { useCallback, useSyncExternalStore } from "react";

/**
 * A tiny persisted boolean preference (e.g. sidebar collapsed), read through useSyncExternalStore so there is
 * no hydration mismatch: the server snapshot is the default, the browser snapshot is localStorage.
 */
export function usePersistentFlag(key: string, defaultValue = false): [boolean, (next: boolean) => void] {
  const subscribe = useCallback(
    (listener: () => void) => {
      const onStorage = (event: StorageEvent) => {
        if (event.key === key) listener();
      };
      const onLocal = (event: Event) => {
        if ((event as CustomEvent<string>).detail === key) listener();
      };
      window.addEventListener("storage", onStorage);
      window.addEventListener("folio:local-store", onLocal);
      return () => {
        window.removeEventListener("storage", onStorage);
        window.removeEventListener("folio:local-store", onLocal);
      };
    },
    [key],
  );

  const value = useSyncExternalStore(
    subscribe,
    () => {
      const raw = window.localStorage.getItem(key);
      return raw === null ? defaultValue : raw === "1";
    },
    () => defaultValue,
  );

  const setValue = useCallback(
    (next: boolean) => {
      window.localStorage.setItem(key, next ? "1" : "0");
      window.dispatchEvent(new CustomEvent("folio:local-store", { detail: key }));
    },
    [key],
  );

  return [value, setValue];
}
