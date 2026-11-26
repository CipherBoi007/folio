"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Download, Plus } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { FilterSelect } from "@/components/common/filters";
import { MemberAvatar } from "@/components/common/member-avatar";
import { PageHeader } from "@/components/common/page-header";
import { SearchInput } from "@/components/common/search-input";
import { FineStatusBadge, LoanSlots, MemberStatusBadge } from "@/components/common/status-badges";
import { DataTable, formatSort, parseSort, type Column } from "@/components/data-table/data-table";
import { Pagination } from "@/components/data-table/pagination";
import { MemberSheet } from "@/components/members/member-sheet";
import { useUrlState } from "@/hooks/use-url-state";
import { download } from "@/lib/api/client";
import { errorMessage } from "@/lib/api/errors";
import { formatDate } from "@/lib/format";
import { useMembers, useSettings } from "@/lib/queries";
import type { MemberStatus, MemberSummary } from "@/types/api";

const DEFAULT_SORT = { field: "name", direction: "asc" } as const;

function columns(maxLoans: number): Column<MemberSummary>[] {
  return [
    {
      id: "name",
      header: "Member",
      meta: { sortKey: "name", label: "Member", className: "w-[30%]" },
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <MemberAvatar name={row.original.fullName} />
          <div className="min-w-0">
            <Link href={`/members/${row.original.id}`} className="block truncate font-medium hover:underline" onClick={(e) => e.stopPropagation()}>
              {row.original.fullName}
            </Link>
            <p className="truncate text-sm text-ink-muted">{row.original.city ?? "—"}</p>
          </div>
        </div>
      ),
    },
    {
      id: "number",
      header: "Membership no.",
      meta: { sortKey: "number", label: "Membership no." },
      cell: ({ row }) => <span className="font-mono text-sm">{row.original.membershipNumber}</span>,
    },
    {
      id: "contact",
      header: "Contact",
      meta: { label: "Contact", hideable: true },
      cell: ({ row }) => (
        <div className="text-sm">
          <p className="truncate">{row.original.email}</p>
          <p className="font-mono text-ink-muted">{row.original.phone}</p>
        </div>
      ),
    },
    { id: "status", header: "Status", meta: { label: "Status" }, cell: ({ row }) => <MemberStatusBadge status={row.original.status} /> },
    {
      id: "loans",
      header: "Loans",
      meta: { label: "Loans" },
      cell: ({ row }) => (
        <span className="inline-flex items-center gap-2">
          <LoanSlots used={row.original.activeLoans} max={maxLoans} />
          {row.original.overdueLoans > 0 ? <span className="text-xs text-danger">{row.original.overdueLoans} overdue</span> : null}
        </span>
      ),
    },
    {
      id: "fines",
      header: "Fines",
      meta: { label: "Fines", align: "right" },
      cell: ({ row }) =>
        Number(row.original.unpaidFines) > 0 ? <FineStatusBadge status="UNPAID" amount={Number(row.original.unpaidFines)} /> : <span className="text-ink-muted">—</span>,
    },
    {
      id: "joined",
      header: "Joined",
      meta: { sortKey: "joined", label: "Joined", hideable: true, align: "right" },
      cell: ({ row }) => <span className="font-mono text-sm text-ink-muted">{formatDate(row.original.joinedOn)}</span>,
    },
  ];
}

export function MembersView() {
  const url = useUrlState();
  const router = useRouter();
  const settings = useSettings();
  const [exporting, setExporting] = useState(false);

  const q = url.get("q") ?? "";
  const status = url.get("status") as MemberStatus | undefined;
  const page = url.getNumber("page", 0);
  const sort = parseSort(url.get("sort"), DEFAULT_SORT);
  const sortParam = formatSort(sort);
  const query = useMemo(() => ({ q, status, page, size: 20, sort: sortParam }), [q, status, page, sortParam]);
  const members = useMembers(query);
  const cols = useMemo(() => columns(settings.data?.maxActiveLoans ?? 3), [settings.data?.maxActiveLoans]);

  async function exportCsv() {
    setExporting(true);
    try {
      await download("/members/export.csv", { q, status }, "folio-members.csv");
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setExporting(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Members"
        description="Everyone who borrows from the library, with their loans and standing."
        actions={
          <>
            <Button variant="secondary" onClick={exportCsv} disabled={exporting}>
              <Download />
              {exporting ? "Exporting…" : "Export CSV"}
            </Button>
            <Button onClick={() => url.set({ new: 1 })}>
              <Plus />
              Add member
            </Button>
          </>
        }
      />
      {members.isError ? (
        <div className="rounded-lg border border-border bg-surface">
          <ErrorState error={members.error} title="Members didn't load." onRetry={() => members.refetch()} />
        </div>
      ) : (
        <DataTable
          columns={cols}
          data={members.data?.content}
          loading={members.isFetching}
          sort={sort}
          onSortChange={(next) => url.set({ sort: formatSort(next) })}
          getRowId={(m) => String(m.id)}
          onRowClick={(m) => router.push(`/members/${m.id}`)}
          toolbar={
            <>
              <SearchInput value={q} onChange={(value) => url.set({ q: value })} placeholder="Search name, phone, email or number" className="w-full sm:w-80" />
              <FilterSelect
                ariaLabel="Status"
                allLabel="All statuses"
                value={status}
                onChange={(value) => url.set({ status: value })}
                options={[
                  { value: "ACTIVE", label: "Active" },
                  { value: "SUSPENDED", label: "Suspended" },
                  { value: "INACTIVE", label: "Inactive" },
                ]}
              />
            </>
          }
          footer={
            members.data ? (
              <Pagination page={members.data.page} size={members.data.size} total={members.data.totalElements} onPageChange={(p) => url.set({ page: p })} />
            ) : null
          }
          empty={
            q || status ? (
              <EmptyState compact title="No members match." description="Check the spelling, or search by phone or membership number." />
            ) : (
              <EmptyState compact title="No members yet." description="Register the first member to start lending." action={<Button onClick={() => url.set({ new: 1 })}>Add member</Button>} />
            )
          }
        />
      )}
      <MemberSheet open={url.get("new") === "1"} onOpenChange={(open) => url.set({ new: open ? 1 : null, page })} onSaved={(m) => router.push(`/members/${m.id}`)} />
    </>
  );
}
