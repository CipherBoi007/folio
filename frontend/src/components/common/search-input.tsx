"use client";

import { useEffect, useRef, useState } from "react";
import { Search, X } from "lucide-react";
import { cn } from "cn";
import { Input } from "@/components/ui/input";
import { Kbd } from "@/components/common/kbd";
import { useDebouncedValue } from "@/hooks/use-debounced-value";

/** Debounced search box (300ms). `/` focuses it from anywhere on the page. */
export function SearchInput({
  value,
  onChange,
  placeholder,
  className,
}: {
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
  className?: string;
}) {
  const [draft, setDraft] = useState(value);
  const debounced = useDebouncedValue(draft, 300);
  const onChangeRef = useRef(onChange);
  const valueRef = useRef(value);

  useEffect(() => {
    onChangeRef.current = onChange;
    valueRef.current = value;
  }, [onChange, value]);

  useEffect(() => {
    if (debounced !== valueRef.current) onChangeRef.current(debounced);
  }, [debounced]);

  return (
    <div className={cn("relative", className)}>
      <Search aria-hidden className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-ink-muted" />
      <Input
        data-page-search
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        placeholder={placeholder}
        aria-label={placeholder}
        className="pr-9 pl-9"
      />
      {draft ? (
        <button
          type="button"
          onClick={() => setDraft("")}
          className="absolute top-1/2 right-2 -translate-y-1/2 rounded-sm p-1 text-ink-muted hover:text-ink"
          aria-label="Clear search"
        >
          <X className="size-3.5" />
        </button>
      ) : (
        <Kbd className="pointer-events-none absolute top-1/2 right-2.5 -translate-y-1/2">/</Kbd>
      )}
    </div>
  );
}
