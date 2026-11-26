"use client";

import { useState } from "react";
import Link from "next/link";
import { CircleCheck, Undo2 } from "lucide-react";
import { cn } from "cn";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { BookCover } from "@/components/common/book-cover";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { SearchInput } from "@/components/common/search-input";
import { LoanStatusBadge } from "@/components/common/status-badges";
import { useUrlState } from "@/hooks/use-url-state";
import { formatDate, formatMoney } from "@/lib/format";
import { useLoans, useRenewLoan, useReturnLoan } from "@/lib/queries";
import type { Loan, ReturnResult } from "@/types/api";

export function ReturnDesk() {
  const url = useUrlState();
  const q = url.get("q") ?? "";
  const loans = useLoans({ status: "ACTIVE", q, size: 12, sort: "due,asc" });
  const ret = useReturnLoan();
  const renew = useRenewLoan();
  const [confirming, setConfirming] = useState<number | null>(null);
  const [result, setResult] = useState<ReturnResult | null>(null);

  return (
    <div className="space-y-5">
      {result ? <ReturnResultBanner result={result} onDismiss={() => setResult(null)} /> : null}

      <SearchInput value={q} onChange={(value) => url.set({ q: value })} placeholder="Find a loan by member, book, ISBN or loan number" className="max-w-xl" />

      {loans.isError ? (
        <ErrorState error={loans.error} onRetry={() => loans.refetch()} />
      ) : !loans.data ? (
        <div className="grid gap-3 md:grid-cols-2">
          {Array.from({ length: 4 }, (_, i) => (
            <Skeleton key={i} className="h-28" />
          ))}
        </div>
      ) : loans.data.content.length === 0 ? (
        <div className="rounded-lg border border-border bg-surface">
          {q ? (
            <EmptyState compact title="No active loan matches." description="It may already be returned. Check the member's history." />
          ) : (
            <EmptyState compact title="Nothing on loan." description="Every book is on the shelf." />
          )}
        </div>
      ) : (
        <>
          <p className="text-sm text-ink-muted">{q ? `${loans.data.totalElements} matching loans` : "Oldest due first"}</p>
          <ul className="grid gap-3 md:grid-cols-2">
            {loans.data.content.map((loan) => (
              <LoanCard
                key={loan.id}
                loan={loan}
                confirming={confirming === loan.id}
                pending={ret.isPending && ret.variables === loan.id}
                onStart={() => setConfirming(loan.id)}
                onCancel={() => setConfirming(null)}
                onRenew={() => renew.mutate(loan.id)}
                renewing={renew.isPending && renew.variables === loan.id}
                onConfirm={() =>
                  ret.mutate(loan.id, {
                    onSuccess: (r) => {
                      setConfirming(null);
                      setResult(r);
                    },
                  })
                }
              />
            ))}
          </ul>
        </>
      )}
    </div>
  );
}

function LoanCard({
  loan,
  confirming,
  pending,
  renewing,
  onStart,
  onCancel,
  onConfirm,
  onRenew,
}: {
  loan: Loan;
  confirming: boolean;
  pending: boolean;
  renewing: boolean;
  onStart: () => void;
  onCancel: () => void;
  onConfirm: () => void;
  onRenew: () => void;
}) {
  return (
    <li className={cn("rounded-lg border bg-surface p-4 transition-colors", confirming ? "border-brand" : "border-border")}>
      <div className="flex gap-4">
        <BookCover isbn={loan.book.isbn13} title={loan.book.title} size="sm" />
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="truncate font-medium text-ink">{loan.book.title}</p>
              <p className="truncate text-sm text-ink-muted">
                {loan.member.fullName} · <span className="font-mono">{loan.member.membershipNumber}</span>
              </p>
            </div>
            <LoanStatusBadge loan={loan} />
          </div>
          <p className="mt-2 font-mono text-sm text-ink-muted">
            Issued {formatDate(loan.issuedAt)} · Due {formatDate(loan.dueDate)}
          </p>
        </div>
      </div>

      {confirming ? (
        <div className="mt-4 rounded-md bg-surface-muted p-3">
          {loan.projectedFine ? (
            <p className="text-sm text-danger">
              {loan.overdueDays} {loan.overdueDays === 1 ? "day" : "days"} late. A fine of <strong>{formatMoney(loan.projectedFine)}</strong> will be
              recorded.
            </p>
          ) : (
            <p className="text-sm text-ink">On time. No fine.</p>
          )}
          <div className="mt-3 flex justify-end gap-2">
            <Button variant="ghost" size="sm" onClick={onCancel} disabled={pending}>
              Cancel
            </Button>
            <Button size="sm" onClick={onConfirm} disabled={pending}>
              {pending ? "Recording…" : "Confirm return"}
            </Button>
          </div>
        </div>
      ) : (
        <div className="mt-3 flex justify-end gap-2">
          {loan.renewable ? (
            <Button variant="ghost" size="sm" onClick={onRenew} disabled={renewing}>
              {renewing ? "Renewing…" : "Renew"}
            </Button>
          ) : null}
          <Button variant="secondary" size="sm" onClick={onStart}>
            <Undo2 />
            Record return
          </Button>
        </div>
      )}
    </li>
  );
}

function ReturnResultBanner({ result, onDismiss }: { result: ReturnResult; onDismiss: () => void }) {
  const { loan, fine } = result;
  return (
    <div className={cn("flex flex-wrap items-center gap-3 rounded-lg border px-4 py-3", fine ? "border-warning/40 bg-warning-soft/60" : "border-success/30 bg-success-soft/60")}>
      <CircleCheck className={cn("size-5", fine ? "text-warning" : "text-success")} />
      <p className="min-w-0 flex-1 text-base text-ink">
        &ldquo;{loan.book.title}&rdquo; is back from {loan.member.fullName}.
        {fine ? (
          <>
            {" "}
            Fine of <strong>{formatMoney(fine.amount)}</strong> recorded for {fine.overdueDays} {fine.overdueDays === 1 ? "day" : "days"} late.
          </>
        ) : (
          " Returned on time."
        )}
        {result.nextInQueue ? (
          <>
            {" "}
            Next in queue:{" "}
            <Link href={`/members/${result.nextInQueue.id}`} className="font-medium underline underline-offset-2">
              {result.nextInQueue.fullName}
            </Link>
            .
          </>
        ) : null}
      </p>
      {fine ? (
        <Button asChild variant="secondary" size="sm">
          <Link href={`/members/${loan.member.id}`}>Collect payment</Link>
        </Button>
      ) : null}
      <Button variant="ghost" size="sm" onClick={onDismiss}>
        Dismiss
      </Button>
    </div>
  );
}
