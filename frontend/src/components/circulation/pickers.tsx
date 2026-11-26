"use client";

import { useId, useState, type ReactNode } from "react";
import { ChevronsUpDown } from "lucide-react";
import { cn } from "cn";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { BookCover } from "@/components/common/book-cover";
import { MemberAvatar } from "@/components/common/member-avatar";
import { AvailabilityBadge, LoanSlots, MemberStatusBadge } from "@/components/common/status-badges";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useBooks, useMembers } from "@/lib/queries";
import type { BookSummary, MemberSummary } from "@/types/api";

function PickerShell({
  label,
  open,
  onOpenChange,
  trigger,
  children,
}: {
  label: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  trigger: ReactNode;
  children: ReactNode;
}) {
  const listId = useId();
  return (
    <Popover open={open} onOpenChange={onOpenChange}>
      <PopoverTrigger asChild>
        <button
          type="button"
          role="combobox"
          aria-expanded={open}
          aria-controls={listId}
          aria-label={label}
          className={cn(
            "flex min-h-14 w-full items-center gap-3 rounded-md border border-input bg-surface px-3 py-2 text-left transition-colors hover:border-ink-muted/40",
            open && "border-brand ring-[3px] ring-brand/15",
          )}
        >
          <div className="min-w-0 flex-1">{trigger}</div>
          <ChevronsUpDown className="size-4 shrink-0 text-ink-muted" />
        </button>
      </PopoverTrigger>
      <PopoverContent id={listId} align="start" className="w-(--radix-popover-trigger-width) min-w-[320px] p-0">
        {children}
      </PopoverContent>
    </Popover>
  );
}

export function MemberPicker({
  value,
  onChange,
  maxLoans,
}: {
  value: MemberSummary | null;
  onChange: (member: MemberSummary) => void;
  maxLoans: number;
}) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const q = useDebouncedValue(query.trim(), 250);
  const members = useMembers({ q, size: 8, sort: "name,asc" }, open);

  return (
    <PickerShell
      label="Choose a member"
      open={open}
      onOpenChange={setOpen}
      trigger={
        value ? (
          <div className="flex items-center gap-3">
            <MemberAvatar name={value.fullName} />
            <div className="min-w-0">
              <p className="truncate font-medium text-ink">{value.fullName}</p>
              <p className="font-mono text-sm text-ink-muted">
                {value.membershipNumber} · {value.phone}
              </p>
            </div>
          </div>
        ) : (
          <span className="text-ink-muted">Search by name, phone or membership number</span>
        )
      }
    >
      <Command shouldFilter={false}>
        <CommandInput value={query} onValueChange={setQuery} placeholder="Name, phone or FOL-…" autoFocus />
        <CommandList className="max-h-80 p-1">
          <CommandEmpty>{members.isFetching ? "Searching…" : "No member matches."}</CommandEmpty>
          <CommandGroup>
            {(members.data?.content ?? []).map((m) => (
              <CommandItem
                key={m.id}
                value={String(m.id)}
                onSelect={() => {
                  onChange(m);
                  setOpen(false);
                }}
              >
                <MemberAvatar name={m.fullName} size="sm" />
                <div className="min-w-0 flex-1">
                  <p className="truncate">{m.fullName}</p>
                  <p className="font-mono text-xs text-ink-muted">{m.membershipNumber}</p>
                </div>
                {m.status !== "ACTIVE" ? <MemberStatusBadge status={m.status} /> : <LoanSlots used={m.activeLoans} max={maxLoans} />}
              </CommandItem>
            ))}
          </CommandGroup>
        </CommandList>
      </Command>
    </PickerShell>
  );
}

export function BookPicker({
  value,
  onChange,
  heldBookIds,
}: {
  value: BookSummary | null;
  onChange: (book: BookSummary) => void;
  heldBookIds: Set<number>;
}) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const q = useDebouncedValue(query.trim(), 250);
  const books = useBooks({ q, size: 8, sort: "title,asc" });

  return (
    <PickerShell
      label="Choose a book"
      open={open}
      onOpenChange={setOpen}
      trigger={
        value ? (
          <div className="flex items-center gap-3">
            <BookCover isbn={value.isbn13} title={value.title} size="xs" />
            <div className="min-w-0">
              <p className="truncate font-medium text-ink">{value.title}</p>
              <p className="truncate text-sm text-ink-muted">{value.authors.join(", ")}</p>
            </div>
          </div>
        ) : (
          <span className="text-ink-muted">Search by title, author or ISBN</span>
        )
      }
    >
      <Command shouldFilter={false}>
        <CommandInput value={query} onValueChange={setQuery} placeholder="Title, author or ISBN" autoFocus />
        <CommandList className="max-h-96 p-1">
          <CommandEmpty>{books.isFetching ? "Searching…" : "No book matches."}</CommandEmpty>
          <CommandGroup>
            {(books.data?.content ?? []).map((b) => {
              const held = heldBookIds.has(b.id);
              const out = b.availableQuantity === 0;
              const reason = held ? "Already on loan to this member" : out ? "All copies are on loan" : null;
              return (
                <CommandItem
                  key={b.id}
                  value={String(b.id)}
                  disabled={Boolean(reason)}
                  onSelect={() => {
                    onChange(b);
                    setOpen(false);
                  }}
                >
                  <BookCover isbn={b.isbn13} title={b.title} size="xs" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate">{b.title}</p>
                    <p className="truncate text-xs text-ink-muted">{reason ?? b.authors.join(", ")}</p>
                  </div>
                  <AvailabilityBadge available={b.availableQuantity} total={b.totalQuantity} />
                </CommandItem>
              );
            })}
          </CommandGroup>
        </CommandList>
      </Command>
    </PickerShell>
  );
}
