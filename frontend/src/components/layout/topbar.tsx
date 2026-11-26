"use client";

import Link from "next/link";
import { Menu, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Kbd } from "@/components/common/kbd";
import { Breadcrumbs } from "@/components/layout/breadcrumbs";
import { useCommandPalette, useModKeyLabel } from "@/components/layout/command-palette";

/** Breadcrumb on the left; the ⌘K search trigger and the desk's primary action on the right. */
export function Topbar({ onOpenMobileNav }: { onOpenMobileNav: () => void }) {
  const { setOpen } = useCommandPalette();
  const mod = useModKeyLabel();

  return (
    <header className="sticky top-0 z-30 flex h-14 shrink-0 items-center gap-3 border-b border-border bg-background px-4 sm:px-6 lg:px-8">
      <Button variant="ghost" size="icon-sm" className="lg:hidden" onClick={onOpenMobileNav} aria-label="Open navigation">
        <Menu />
      </Button>

      <div className="min-w-0 flex-1">
        <Breadcrumbs />
      </div>

      <button
        type="button"
        onClick={() => setOpen(true)}
        className="hidden h-8 w-64 items-center gap-2 rounded-md border border-input bg-surface px-2.5 text-left text-base text-ink-muted transition-colors hover:border-ink-muted/40 sm:flex xl:w-80"
        aria-label="Search (opens command palette)"
      >
        <Search aria-hidden className="size-4 shrink-0" />
        <span className="flex-1 truncate">Search books, members…</span>
        <Kbd>{mod} K</Kbd>
      </button>
      <Button variant="ghost" size="icon-sm" className="sm:hidden" onClick={() => setOpen(true)} aria-label="Search">
        <Search />
      </Button>

      <Button asChild size="sm">
        <Link href="/circulation">Issue book</Link>
      </Button>
    </header>
  );
}
