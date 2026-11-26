"use client";

import { useMemo, useState, type ReactNode } from "react";
import Link from "next/link";
import { CircleAlert, CircleCheck, Printer, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { BookCover } from "@/components/common/book-cover";
import { EligibilityPanel } from "@/components/members/eligibility-panel";
import { BookPicker, MemberPicker } from "@/components/circulation/pickers";
import { useUrlState } from "@/hooks/use-url-state";
import { errorMessage } from "@/lib/api/errors";
import { formatDate, formatDateWithWeekday } from "@/lib/format";
import { useBook, useEligibility, useIssueBook, useMember, useMemberLoans, useSettings } from "@/lib/queries";
import type { BookSummary, Loan, MemberSummary } from "@/types/api";

export function IssueDesk() {
  const url = useUrlState();
  const settings = useSettings();
  const issue = useIssueBook();
  // `undefined` means "not chosen yet", so a member or book passed in the URL is used until the user picks.
  const [memberChoice, setMember] = useState<MemberSummary | null | undefined>(undefined);
  const [bookChoice, setBook] = useState<BookSummary | null | undefined>(undefined);
  const [receipt, setReceipt] = useState<Loan | null>(null);

  const presetMember = useMember(Number(url.get("member") ?? 0) || 0);
  const presetBook = useBook(Number(url.get("book") ?? 0) || 0);
  const member = memberChoice === undefined ? (presetMember.data ?? null) : memberChoice;
  const book = bookChoice === undefined ? (presetBook.data ?? null) : bookChoice;

  const eligibility = useEligibility(member?.id ?? null);
  const memberLoans = useMemberLoans(member?.id ?? 0, { status: "ACTIVE", size: 20 });
  const held = useMemo(() => new Set((member ? memberLoans.data?.content ?? [] : []).map((l) => l.book.id)), [member, memberLoans.data]);

  const blockedByBook = book ? (held.has(book.id) ? "Already on loan to this member." : book.availableQuantity === 0 ? "All copies are on loan." : null) : null;
  const ready = Boolean(member && book && eligibility.data?.canBorrow && !blockedByBook);
  const loanDays = eligibility.data?.loanDays ?? settings.data?.loanDays ?? 14;

  function reset() {
    setMember(null);
    setBook(null);
    setReceipt(null);
    issue.reset();
    url.set({ member: null, book: null });
  }

  if (receipt) return <IssueReceipt loan={receipt} onAnother={reset} />;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_380px]">
      <div className="space-y-6">
        <Step number={1} title="Member">
          <MemberPicker
            value={member}
            maxLoans={settings.data?.maxActiveLoans ?? 3}
            onChange={(m) => {
              setMember(m);
              issue.reset();
            }}
          />
          {member ? <EligibilityPanel className="mt-3" eligibility={eligibility.data} loading={eligibility.isPending} /> : null}
        </Step>
        <Step number={2} title="Book">
          <BookPicker
            value={book}
            heldBookIds={held}
            onChange={(b) => {
              setBook(b);
              issue.reset();
            }}
          />
          {blockedByBook ? (
            <p className="mt-2 flex items-center gap-1.5 text-sm text-danger">
              <CircleAlert className="size-3.5" />
              {blockedByBook}
            </p>
          ) : null}
        </Step>
      </div>

      <aside className="h-fit rounded-lg border border-border bg-surface lg:sticky lg:top-20">
        <div className="border-b border-dashed border-border px-5 py-4">
          <p className="label-caps">Loan summary</p>
        </div>
        <dl className="space-y-3 px-5 py-4 text-base">
          <Row label="Member" value={member ? `${member.fullName}` : "—"} sub={member?.membershipNumber} />
          <Row label="Book" value={book ? book.title : "—"} sub={book ? book.isbn13 : undefined} />
          <Row label="Issued" value={formatDate(new Date())} />
        </dl>
        <div className="border-t border-dashed border-border px-5 py-4">
          <p className="label-caps">Due back</p>
          <p className="mt-1 font-display text-xl font-medium text-ink">
            {eligibility.data ? formatDateWithWeekday(eligibility.data.dueDateIfIssuedToday) : "—"}
          </p>
          <p className="text-sm text-ink-muted">{loanDays} days from today</p>
        </div>
        <div className="space-y-3 border-t border-border px-5 py-4">
          {issue.isError ? (
            <p role="alert" className="text-sm text-danger">
              {errorMessage(issue.error)}
            </p>
          ) : null}
          <Button
            className="w-full"
            size="lg"
            disabled={!ready || issue.isPending}
            onClick={() => member && book && issue.mutate({ memberId: member.id, bookId: book.id }, { onSuccess: setReceipt })}
          >
            {issue.isPending ? "Issuing…" : "Issue book"}
          </Button>
          {member || book ? (
            <Button variant="ghost" size="sm" className="w-full" onClick={reset}>
              Start over
            </Button>
          ) : null}
        </div>
      </aside>
    </div>
  );
}

function Step({ number, title, children }: { number: number; title: string; children: ReactNode }) {
  return (
    <section>
      <h2 className="mb-2.5 flex items-center gap-2 text-base font-medium text-ink">
        <span className="inline-flex size-5 items-center justify-center rounded-full bg-surface-muted font-mono text-xs text-ink-muted">{number}</span>
        {title}
      </h2>
      {children}
    </section>
  );
}

function Row({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <dt className="text-ink-muted">{label}</dt>
      <dd className="min-w-0 text-right">
        <p className="truncate text-ink">{value}</p>
        {sub ? <p className="font-mono text-xs text-ink-muted">{sub}</p> : null}
      </dd>
    </div>
  );
}

function IssueReceipt({ loan, onAnother }: { loan: Loan; onAnother: () => void }) {
  return (
    <div className="mx-auto max-w-lg">
      <div id="issue-slip" className="rounded-lg border border-border bg-surface">
        <div className="flex items-center gap-2 border-b border-border px-6 py-4 text-success">
          <CircleCheck className="size-5" />
          <p className="text-lg font-medium">Book issued</p>
          <span className="ml-auto font-mono text-sm text-ink-muted">Loan #{loan.id}</span>
        </div>
        <div className="flex gap-5 px-6 py-5">
          <BookCover isbn={loan.book.isbn13} title={loan.book.title} size="md" />
          <div className="min-w-0">
            <p className="font-display text-xl font-medium">{loan.book.title}</p>
            <p className="text-ink-muted">{loan.book.authors.join(", ")}</p>
            <p className="mt-4 text-sm text-ink-muted">Issued to</p>
            <Link href={`/members/${loan.member.id}`} className="font-medium hover:underline">
              {loan.member.fullName}
            </Link>
            <p className="font-mono text-sm text-ink-muted">{loan.member.membershipNumber}</p>
          </div>
        </div>
        <div className="border-t border-dashed border-border px-6 py-4">
          <p className="label-caps">Please return by</p>
          <p className="mt-1 font-display text-2xl font-medium">{formatDateWithWeekday(loan.dueDate)}</p>
          <p className="mt-1 text-sm text-ink-muted">
            Issued {formatDate(loan.issuedAt)} by {loan.issuedBy.name}. Late returns are fined per day.
          </p>
        </div>
      </div>
      <div className="mt-4 flex justify-center gap-2 print:hidden">
        <Button variant="secondary" onClick={() => window.print()}>
          <Printer />
          Print slip
        </Button>
        <Button onClick={onAnother}>
          <RotateCcw />
          Issue another
        </Button>
      </div>
    </div>
  );
}
