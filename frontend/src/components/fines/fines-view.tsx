"use client";

import Link from "next/link";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { KpiCard, KpiCardSkeleton } from "@/components/common/kpi-card";
import { PageHeader } from "@/components/common/page-header";
import { SearchInput } from "@/components/common/search-input";
import { FineStatusBadge } from "@/components/common/status-badges";
import { DataTable, formatSort, parseSort, type Column } from "@/components/data-table/data-table";
import { Pagination } from "@/components/data-table/pagination";
import { FineActions } from "@/components/fines/fine-actions";
import { useUrlState } from "@/hooks/use-url-state";
import { formatDate, formatMoney } from "@/lib/format";
import { useFineSummary, useFines } from "@/lib/queries";
import type { Fine, FineStatus } from "@/types/api";

const DEFAULT_SORT = { field: "created", direction: "desc" } as const;

const columns: Column<Fine>[] = [
  {
    id: "member",
    header: "Member",
    meta: { label: "Member" },
    cell: ({ row }) => (
      <div className="min-w-0">
        <Link href={`/members/${row.original.member.id}`} className="block truncate font-medium hover:underline" onClick={(e) => e.stopPropagation()}>
          {row.original.member.fullName}
        </Link>
        <p className="font-mono text-xs text-ink-muted">{row.original.member.membershipNumber}</p>
      </div>
    ),
  },
  {
    id: "book",
    header: "Book",
    meta: { label: "Book" },
    cell: ({ row }) => <span className="block max-w-xs truncate">{row.original.book.title}</span>,
  },
  {
    id: "days",
    header: "Days late",
    meta: { label: "Days late", align: "right" },
    cell: ({ row }) => (
      <span className="font-mono tabular-nums" title={`${formatMoney(row.original.dailyRate)} a day`}>
        {row.original.overdueDays}
      </span>
    ),
  },
  {
    id: "amount",
    header: "Amount",
    meta: { label: "Amount", align: "right", sortKey: "amount" },
    cell: ({ row }) => <span className="font-mono tabular-nums">{formatMoney(row.original.amount)}</span>,
  },
  {
    id: "created",
    header: "Charged",
    meta: { label: "Charged", sortKey: "created", align: "right" },
    cell: ({ row }) => <span className="font-mono text-sm text-ink-muted">{formatDate(row.original.createdAt)}</span>,
  },
  {
    id: "status",
    header: "Status",
    meta: { label: "Status" },
    cell: ({ row }) => (
      <div>
        <FineStatusBadge status={row.original.status} />
        {row.original.resolvedBy ? (
          <p className="mt-0.5 max-w-[220px] truncate text-xs text-ink-muted" title={row.original.resolutionNote ?? undefined}>
            {row.original.resolvedBy.name} · {formatDate(row.original.resolvedAt)}
            {row.original.resolutionNote ? ` · ${row.original.resolutionNote}` : ""}
          </p>
        ) : null}
      </div>
    ),
  },
  { id: "actions", header: "", meta: { label: "Actions", align: "right" }, cell: ({ row }) => <FineActions fine={row.original} /> },
];

export function FinesView() {
  const url = useUrlState();
  const summary = useFineSummary();
  const status = (url.get("status") ?? "UNPAID") as FineStatus;
  const q = url.get("q") ?? "";
  const page = url.getNumber("page", 0);
  const sort = parseSort(url.get("sort"), DEFAULT_SORT);
  const fines = useFines({ status, q, page, size: 20, sort: formatSort(sort) });
  const s = summary.data;

  return (
    <>
      <PageHeader title="Fines" description="Charged automatically when a book comes back late. Payments and waivers are logged." />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        {s ? (
          <>
            <KpiCard label="Outstanding" value={formatMoney(s.unpaid.amount)} tone={Number(s.unpaid.amount) > 0 ? "danger" : "default"} delta={`${s.unpaid.count} unpaid`} />
            <KpiCard label="Collected" value={formatMoney(s.paid.amount)} delta={`${s.paid.count} paid`} />
            <KpiCard label="Waived" value={formatMoney(s.waived.amount)} delta={`${s.waived.count} waived`} />
          </>
        ) : (
          Array.from({ length: 3 }, (_, i) => <KpiCardSkeleton key={i} />)
        )}
      </div>

      <Tabs value={status} onValueChange={(value) => url.set({ status: value === "UNPAID" ? null : value })} className="mb-4">
        <TabsList>
          <TabsTrigger value="UNPAID">Unpaid</TabsTrigger>
          <TabsTrigger value="PAID">Paid</TabsTrigger>
          <TabsTrigger value="WAIVED">Waived</TabsTrigger>
        </TabsList>
      </Tabs>

      {fines.isError ? (
        <ErrorState error={fines.error} onRetry={() => fines.refetch()} />
      ) : (
        <DataTable
          columns={columns}
          data={fines.data?.content}
          loading={fines.isFetching}
          sort={sort}
          onSortChange={(next) => url.set({ sort: formatSort(next) })}
          getRowId={(f) => String(f.id)}
          toolbar={<SearchInput value={q} onChange={(value) => url.set({ q: value })} placeholder="Search member or book" className="w-full sm:w-72" />}
          footer={
            fines.data ? (
              <Pagination page={fines.data.page} size={fines.data.size} total={fines.data.totalElements} onPageChange={(p) => url.set({ page: p })} />
            ) : null
          }
          empty={
            status === "UNPAID" ? (
              <EmptyState compact title="Nothing outstanding." description="Every fine has been paid or waived." />
            ) : (
              <EmptyState compact title={status === "PAID" ? "No payments yet." : "No waivers yet."} description="Settled fines will be listed here." />
            )
          }
        />
      )}
    </>
  );
}
