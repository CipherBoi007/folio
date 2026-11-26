"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { Download } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { FilterSelect } from "@/components/common/filters";
import { PageHeader } from "@/components/common/page-header";
import { SignedQuantity, TRANSACTION_LABELS, TransactionTypeBadge } from "@/components/common/status-badges";
import { DataTable, type Column } from "@/components/data-table/data-table";
import { Pagination } from "@/components/data-table/pagination";
import { useUrlState } from "@/hooks/use-url-state";
import { download } from "@/lib/api/client";
import { errorMessage } from "@/lib/api/errors";
import { formatDateTime, humanize } from "@/lib/format";
import { useLedger, useStaff } from "@/lib/queries";
import type { LedgerEntry, TransactionType } from "@/types/api";

const columns: Column<LedgerEntry>[] = [
  {
    id: "when",
    header: "When",
    meta: { label: "When" },
    cell: ({ row }) => <span className="font-mono text-sm text-ink-muted">{formatDateTime(row.original.performedAt)}</span>,
  },
  { id: "type", header: "Type", meta: { label: "Type" }, cell: ({ row }) => <TransactionTypeBadge type={row.original.type} /> },
  {
    id: "book",
    header: "Book",
    meta: { label: "Book", className: "w-[30%]" },
    cell: ({ row }) => (
      <Link href={`/books/${row.original.book.id}`} className="block max-w-xs truncate font-medium hover:underline">
        {row.original.book.title}
      </Link>
    ),
  },
  { id: "change", header: "Change", meta: { label: "Change", align: "right" }, cell: ({ row }) => <SignedQuantity value={row.original.quantityChange} /> },
  {
    id: "available",
    header: "On shelf",
    meta: { label: "On shelf", align: "right" },
    cell: ({ row }) => (
      <span className="font-mono text-sm text-ink-muted tabular-nums">
        {row.original.availableBefore} → <span className="text-ink">{row.original.availableAfter}</span>
      </span>
    ),
  },
  {
    id: "total",
    header: "Total",
    meta: { label: "Total", align: "right", hideable: true },
    cell: ({ row }) => (
      <span className="font-mono text-sm text-ink-muted tabular-nums">
        {row.original.totalBefore} → <span className="text-ink">{row.original.totalAfter}</span>
      </span>
    ),
  },
  {
    id: "reason",
    header: "Reason",
    meta: { label: "Reason", hideable: true },
    cell: ({ row }) => {
      const e = row.original;
      const text = [e.reason ? humanize(e.reason) : null, e.note, e.loanId ? `Loan #${e.loanId}` : null].filter(Boolean).join(" · ");
      return <span className="block max-w-xs truncate text-sm text-ink-muted" title={text}>{text || "—"}</span>;
    },
  },
  { id: "by", header: "By", meta: { label: "By", hideable: true }, cell: ({ row }) => <span className="text-sm">{row.original.performedBy.name}</span> },
];

function toInstant(date: string | undefined, endOfDay = false) {
  if (!date) return undefined;
  const d = new Date(`${date}T00:00:00`);
  if (endOfDay) d.setDate(d.getDate() + 1);
  return d.toISOString();
}

export function InventoryView() {
  const url = useUrlState();
  const staff = useStaff();
  const [exporting, setExporting] = useState(false);
  const type = url.get("type") as TransactionType | undefined;
  const actorId = url.get("actor");
  const from = url.get("from");
  const to = url.get("to");
  const bookId = url.get("book");
  const page = url.getNumber("page", 0);

  const filters = useMemo(
    () => ({ type, actorId, bookId, from: toInstant(from), to: toInstant(to, true) }),
    [type, actorId, bookId, from, to],
  );
  const ledger = useLedger({ ...filters, page, size: 25 });

  async function exportCsv() {
    setExporting(true);
    try {
      await download("/inventory/transactions/export.csv", filters, "folio-inventory-transactions.csv");
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setExporting(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Inventory"
        description="The ledger of every stock change. Entries are permanent and add up to each book's current stock."
        actions={
          <Button variant="secondary" onClick={exportCsv} disabled={exporting}>
            <Download />
            {exporting ? "Exporting…" : "Export CSV"}
          </Button>
        }
      />
      {ledger.isError ? (
        <ErrorState error={ledger.error} onRetry={() => ledger.refetch()} />
      ) : (
        <DataTable
          columns={columns}
          data={ledger.data?.content}
          loading={ledger.isFetching}
          getRowId={(e) => String(e.id)}
          toolbar={
            <>
              <FilterSelect
                ariaLabel="Type"
                allLabel="All types"
                value={type}
                onChange={(value) => url.set({ type: value })}
                options={(Object.keys(TRANSACTION_LABELS) as TransactionType[]).map((t) => ({ value: t, label: TRANSACTION_LABELS[t] }))}
              />
              <FilterSelect
                ariaLabel="Staff member"
                allLabel="Anyone"
                value={actorId}
                onChange={(value) => url.set({ actor: value })}
                options={(staff.data ?? []).map((s) => ({ value: String(s.id), label: s.name }))}
              />
              <label className="flex items-center gap-2 text-sm text-ink-muted">
                From
                <Input type="date" className="w-40 font-mono text-sm" value={from ?? ""} onChange={(e) => url.set({ from: e.target.value })} />
              </label>
              <label className="flex items-center gap-2 text-sm text-ink-muted">
                To
                <Input type="date" className="w-40 font-mono text-sm" value={to ?? ""} onChange={(e) => url.set({ to: e.target.value })} />
              </label>
              {bookId ? (
                <Button variant="ghost" size="sm" onClick={() => url.set({ book: null })}>
                  Clear book filter
                </Button>
              ) : null}
            </>
          }
          footer={
            ledger.data ? (
              <Pagination page={ledger.data.page} size={ledger.data.size} total={ledger.data.totalElements} onPageChange={(p) => url.set({ page: p })} />
            ) : null
          }
          empty={<EmptyState compact title="No stock changes match." description="Widen the date range or clear a filter." />}
        />
      )}
    </>
  );
}
