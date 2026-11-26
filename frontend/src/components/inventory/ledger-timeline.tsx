import { SignedQuantity, TransactionTypeBadge } from "@/components/common/status-badges";
import { formatDateTime, humanize } from "@/lib/format";
import type { LedgerEntry } from "@/types/api";

/** Vertical inventory timeline: type, signed quantity, before → after, reason, who and when. */
export function LedgerTimeline({ entries }: { entries: LedgerEntry[] }) {
  return (
    <ol className="relative ml-2 border-l border-border">
      {entries.map((e) => (
        <li key={e.id} className="relative pb-6 pl-6 last:pb-0">
          <span aria-hidden className="absolute top-1.5 -left-[4.5px] size-2 rounded-full border border-surface bg-ink-muted/60 ring-2 ring-surface" />
          <div className="flex flex-wrap items-center gap-2">
            <TransactionTypeBadge type={e.type} />
            <SignedQuantity value={e.quantityChange} />
            <span className="font-mono text-sm text-ink-muted tabular-nums">
              {e.availableBefore}/{e.totalBefore} → <span className="text-ink">{e.availableAfter}/{e.totalAfter}</span>
            </span>
          </div>
          <p className="mt-1 text-sm text-ink-muted">
            {[e.reason ? humanize(e.reason) : null, e.note, e.loanId ? `Loan #${e.loanId}` : null].filter(Boolean).join(" · ") || "—"}
          </p>
          <p className="mt-0.5 text-xs text-ink-muted">
            {e.performedBy.name} · {formatDateTime(e.performedAt)}
          </p>
        </li>
      ))}
    </ol>
  );
}
