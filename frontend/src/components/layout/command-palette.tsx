"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  useSyncExternalStore,
  type ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { ArrowLeftRight, BookPlus, Moon, Sun, Undo2, UserPlus } from "lucide-react";
import { useTheme } from "next-themes";
import {
  Command,
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
  CommandShortcut,
} from "@/components/ui/command";
import { BookCover } from "@/components/common/book-cover";
import { MemberAvatar } from "@/components/common/member-avatar";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { navGroupsFor } from "@/lib/nav";
import { useSearch } from "@/lib/queries";
import type { StaffUser } from "@/types/api";

interface CommandPaletteContextValue {
  open: boolean;
  setOpen: (open: boolean) => void;
}

const CommandPaletteContext = createContext<CommandPaletteContextValue | null>(null);

export function useCommandPalette() {
  const context = useContext(CommandPaletteContext);
  if (!context) throw new Error("useCommandPalette must be used inside CommandPaletteProvider");
  return context;
}

export function useModKeyLabel(): string {
  return useSyncExternalStore(
    () => () => {},
    () => (/Mac|iPhone|iPad/.test(navigator.userAgent) ? "⌘" : "Ctrl"),
    () => "Ctrl",
  );
}

export function CommandPaletteProvider({ user, children }: { user: StaffUser; children: ReactNode }) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key.toLowerCase() === "k" && (event.metaKey || event.ctrlKey)) {
        event.preventDefault();
        setOpen((value) => !value);
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const value = useMemo(() => ({ open, setOpen }), [open]);

  return (
    <CommandPaletteContext.Provider value={value}>
      {children}
      <CommandDialog open={open} onOpenChange={setOpen} title="Command palette" description="Search or jump anywhere" className="sm:max-w-xl">
        {open ? <PaletteBody user={user} close={() => setOpen(false)} /> : null}
      </CommandDialog>
    </CommandPaletteContext.Provider>
  );
}

function PaletteBody({ user, close }: { user: StaffUser; close: () => void }) {
  const router = useRouter();
  const { resolvedTheme, setTheme } = useTheme();
  const [query, setQuery] = useState("");
  const debounced = useDebouncedValue(query.trim(), 200);
  const search = useSearch(debounced);
  const groups = useMemo(() => navGroupsFor(user.role), [user.role]);

  const go = useCallback(
    (href: string) => {
      close();
      router.push(href);
    },
    [close, router],
  );

  const books = debounced ? (search.data?.books ?? []) : [];
  const members = debounced ? (search.data?.members ?? []) : [];

  return (
    <Command loop shouldFilter={false} className="rounded-none! p-0">
      <CommandInput value={query} onValueChange={setQuery} placeholder="Search books, members, or jump to a page…" />
      <CommandList className="p-1.5">
        <CommandEmpty>{search.isFetching ? "Searching…" : "Nothing matches that."}</CommandEmpty>

        {books.length > 0 ? (
          <CommandGroup heading="Books">
            {books.map((book) => (
              <CommandItem key={`b${book.id}`} value={`book-${book.id}`} onSelect={() => go(`/books/${book.id}`)}>
                <BookCover isbn={book.isbn13} title={book.title} size="xs" />
                <span className="min-w-0 flex-1 truncate">
                  {book.title}
                  <span className="text-ink-muted"> · {book.authors.join(", ")}</span>
                </span>
                <CommandShortcut>
                  {book.availableQuantity}/{book.totalQuantity}
                </CommandShortcut>
              </CommandItem>
            ))}
          </CommandGroup>
        ) : null}

        {members.length > 0 ? (
          <CommandGroup heading="Members">
            {members.map((member) => (
              <CommandItem key={`m${member.id}`} value={`member-${member.id}`} onSelect={() => go(`/members/${member.id}`)}>
                <MemberAvatar name={member.fullName} size="sm" />
                <span className="min-w-0 flex-1 truncate">{member.fullName}</span>
                <CommandShortcut>{member.membershipNumber}</CommandShortcut>
              </CommandItem>
            ))}
          </CommandGroup>
        ) : null}

        {!debounced ? (
          <>
            <CommandGroup heading="Actions">
              <CommandItem value="issue" onSelect={() => go("/circulation")}>
                <ArrowLeftRight />
                Issue book
              </CommandItem>
              <CommandItem value="return" onSelect={() => go("/circulation?tab=return")}>
                <Undo2 />
                Record return
              </CommandItem>
              <CommandItem value="add book" onSelect={() => go("/books?new=1")}>
                <BookPlus />
                Add book
              </CommandItem>
              <CommandItem value="add member" onSelect={() => go("/members?new=1")}>
                <UserPlus />
                Add member
              </CommandItem>
              <CommandItem
                value="theme"
                onSelect={() => {
                  close();
                  setTheme(resolvedTheme === "dark" ? "light" : "dark");
                }}
              >
                {resolvedTheme === "dark" ? <Sun /> : <Moon />}
                {resolvedTheme === "dark" ? "Switch to Paper" : "Switch to Reading room"}
              </CommandItem>
            </CommandGroup>
            <CommandSeparator className="my-1" />
            <CommandGroup heading="Navigation">
              {groups.flatMap((group) =>
                group.items.map((item) => (
                  <CommandItem key={item.href} value={item.href} onSelect={() => go(item.href)}>
                    <item.icon />
                    {item.label}
                    <CommandShortcut>{group.label}</CommandShortcut>
                  </CommandItem>
                )),
              )}
            </CommandGroup>
          </>
        ) : null}
      </CommandList>
    </Command>
  );
}
