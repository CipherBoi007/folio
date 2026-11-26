"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Kbd } from "@/components/common/kbd";
import { useModKeyLabel } from "@/components/layout/command-palette";

function isTyping(target: EventTarget | null) {
  const el = target as HTMLElement | null;
  return !!el && (el.isContentEditable || ["INPUT", "TEXTAREA", "SELECT"].includes(el.tagName) || el.getAttribute("role") === "combobox");
}

/** `/` search · `I` issue · `R` return · `N` new (books/members) · `?` this sheet. */
export function KeyboardShortcuts() {
  const router = useRouter();
  const pathname = usePathname();
  const [open, setOpen] = useState(false);
  const mod = useModKeyLabel();

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.metaKey || event.ctrlKey || event.altKey || isTyping(event.target)) return;
      if (document.querySelector("[role=dialog]") && event.key !== "?") return;
      switch (event.key) {
        case "/": {
          const search = document.querySelector<HTMLInputElement>("[data-page-search]");
          if (search) {
            event.preventDefault();
            search.focus();
          }
          break;
        }
        case "i":
        case "I":
          router.push("/circulation");
          break;
        case "r":
        case "R":
          router.push("/circulation?tab=return");
          break;
        case "n":
        case "N":
          if (pathname.startsWith("/members")) router.push("/members?new=1");
          else router.push("/books?new=1");
          break;
        case "?":
          setOpen((value) => !value);
          break;
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [pathname, router]);

  const shortcuts: [string, string][] = [
    [`${mod} K`, "Search and jump"],
    ["/", "Focus the search on this page"],
    ["I", "Issue a book"],
    ["R", "Record a return"],
    ["N", "New book or member"],
    ["?", "Show this sheet"],
  ];

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Keyboard shortcuts</DialogTitle>
          <DialogDescription>Shortcuts work anywhere outside a text field.</DialogDescription>
        </DialogHeader>
        <dl className="divide-y divide-border">
          {shortcuts.map(([key, label]) => (
            <div key={key} className="flex items-center justify-between py-2.5">
              <dt className="text-base text-ink">{label}</dt>
              <dd>
                <Kbd>{key}</Kbd>
              </dd>
            </div>
          ))}
        </dl>
      </DialogContent>
    </Dialog>
  );
}
