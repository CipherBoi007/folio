"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { ChevronRight } from "lucide-react";
import { findNavItem } from "@/lib/nav";

/**
 * Breadcrumbs derive from the URL; detail pages can name the current record (e.g. a book title)
 * with `useBreadcrumbLabel("The Guide")` instead of showing an id.
 */
const BreadcrumbContext = createContext<{ label: string | null; setLabel: (label: string | null) => void } | null>(null);

export function BreadcrumbProvider({ children }: { children: ReactNode }) {
  const [label, setLabel] = useState<string | null>(null);
  return <BreadcrumbContext.Provider value={{ label, setLabel }}>{children}</BreadcrumbContext.Provider>;
}

export function useBreadcrumbLabel(label: string | null | undefined) {
  const context = useContext(BreadcrumbContext);
  const setLabel = context?.setLabel;
  useEffect(() => {
    if (!setLabel) return;
    setLabel(label ?? null);
    return () => setLabel(null);
  }, [label, setLabel]);
}

export function Breadcrumbs() {
  const pathname = usePathname();
  const context = useContext(BreadcrumbContext);
  const section = findNavItem(pathname);
  const isDetail = section !== undefined && pathname !== section.href;

  if (!section) return null;

  return (
    <nav aria-label="Breadcrumb" className="flex min-w-0 items-center gap-1.5 text-base">
      {isDetail ? (
        <>
          <Link href={section.href} className="shrink-0 text-ink-muted transition-colors hover:text-ink">
            {section.label}
          </Link>
          <ChevronRight aria-hidden className="size-3.5 shrink-0 text-ink-muted/70" />
          <span aria-current="page" className="truncate font-medium text-ink">
            {context?.label ?? "…"}
          </span>
        </>
      ) : (
        <span aria-current="page" className="font-medium text-ink">
          {section.label}
        </span>
      )}
    </nav>
  );
}
