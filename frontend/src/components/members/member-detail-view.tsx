"use client";

import { useState, type ReactNode } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeftRight, ChevronDown, CreditCard, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AdminOnly } from "@/components/common/admin-only";
import { BookCover } from "@/components/common/book-cover";
import { ConfirmDialog } from "@/components/common/confirm-dialog";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { MemberAvatar } from "@/components/common/member-avatar";
import { FineStatusBadge, LoanStatusBadge, MemberStatusBadge } from "@/components/common/status-badges";
import { Pagination } from "@/components/data-table/pagination";
import { LoanActions } from "@/components/circulation/loan-actions";
import { FineActions } from "@/components/fines/fine-actions";
import { useBreadcrumbLabel } from "@/components/layout/breadcrumbs";
import { EligibilityPanel } from "@/components/members/eligibility-panel";
import { MemberCardDialog } from "@/components/members/member-card";
import { MemberSheet } from "@/components/members/member-sheet";
import { formatDate, formatMoney } from "@/lib/format";
import { useDeleteMember, useEligibility, useMember, useMemberFines, useMemberLoans, useMemberStatus } from "@/lib/queries";
import type { LoanStatus, MemberStatus } from "@/types/api";

const STATUS_ACTIONS: { status: MemberStatus; label: string; hint: string }[] = [
  { status: "ACTIVE", label: "Active", hint: "Can borrow" },
  { status: "SUSPENDED", label: "Suspended", hint: "Can return, can't borrow" },
  { status: "INACTIVE", label: "Inactive", hint: "Membership closed" },
];

export function MemberDetailView({ id }: { id: number }) {
  const member = useMember(id);
  const eligibility = useEligibility(id);
  const status = useMemberStatus(id);
  const remove = useDeleteMember();
  const router = useRouter();
  const [editing, setEditing] = useState(false);
  const [card, setCard] = useState(false);
  const [deleting, setDeleting] = useState(false);
  useBreadcrumbLabel(member.data?.fullName);

  if (member.isError) {
    return (
      <div className="rounded-lg border border-border bg-surface">
        <ErrorState error={member.error} title="This member couldn't be loaded." onRetry={() => member.refetch()} />
      </div>
    );
  }
  if (!member.data) return <Skeleton className="h-48 w-full" />;
  const m = member.data;

  return (
    <>
      <header className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div className="flex items-start gap-4">
          <MemberAvatar name={m.fullName} size="lg" />
          <div className="min-w-0">
            <h1 className="font-display text-2xl font-medium text-ink">{m.fullName}</h1>
            <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-base text-ink-muted">
              <span className="font-mono text-ink">{m.membershipNumber}</span>
              <span>{m.email}</span>
              <span className="font-mono">{m.phone}</span>
            </p>
            <p className="mt-1 text-sm text-ink-muted">
              {[m.addressLine, m.city, m.postalCode].filter(Boolean).join(", ") || "No address on file"} · Joined {formatDate(m.joinedOn)}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="secondary" className="gap-2">
                <MemberStatusBadge status={m.status} />
                <ChevronDown className="text-ink-muted" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel className="label-caps">Membership status</DropdownMenuLabel>
              {STATUS_ACTIONS.map((option) => (
                <DropdownMenuItem key={option.status} disabled={option.status === m.status || status.isPending} onSelect={() => status.mutate(option.status)}>
                  <span className="flex flex-col">
                    <span>{option.label}</span>
                    <span className="text-xs text-ink-muted">{option.hint}</span>
                  </span>
                </DropdownMenuItem>
              ))}
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={() => setCard(true)}>
                <CreditCard />
                Membership card
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button variant="secondary" onClick={() => setEditing(true)}>
            <Pencil />
            Edit
          </Button>
          <AdminOnly action="delete members">
            <Button variant="danger-ghost" onClick={() => setDeleting(true)}>
              <Trash2 />
              Delete
            </Button>
          </AdminOnly>
          {eligibility.data?.canBorrow ? (
            <Button asChild>
              <Link href={`/circulation?member=${m.id}`}>
                <ArrowLeftRight />
                Issue book
              </Link>
            </Button>
          ) : null}
        </div>
      </header>

      <div className="mt-6 grid gap-4 md:grid-cols-[2fr_1fr_1fr_1fr]">
        <EligibilityPanel eligibility={eligibility.data} loading={eligibility.isPending} />
        <Stat label="On loan" value={m.activeLoans} />
        <Stat label="Overdue" value={m.overdueLoans} tone={m.overdueLoans > 0 ? "danger" : undefined} />
        <Stat label="Unpaid fines" value={formatMoney(m.unpaidFines)} tone={Number(m.unpaidFines) > 0 ? "danger" : undefined} />
      </div>

      <Tabs defaultValue="active" className="mt-8">
        <TabsList>
          <TabsTrigger value="active">Active loans</TabsTrigger>
          <TabsTrigger value="history">Loan history · {m.totalLoans - m.activeLoans}</TabsTrigger>
          <TabsTrigger value="fines">Fines</TabsTrigger>
        </TabsList>
        <TabsContent value="active">
          <MemberLoans memberId={m.id} status="ACTIVE" />
        </TabsContent>
        <TabsContent value="history">
          <MemberLoans memberId={m.id} status="RETURNED" />
        </TabsContent>
        <TabsContent value="fines">
          <MemberFines memberId={m.id} />
        </TabsContent>
      </Tabs>

      <MemberSheet open={editing} onOpenChange={setEditing} member={m} />
      <MemberCardDialog member={m} open={card} onOpenChange={setCard} />
      <ConfirmDialog
        open={deleting}
        onOpenChange={setDeleting}
        destructive
        title={`Remove ${m.fullName}?`}
        description="The member leaves the active register. Their loan and fine history is kept for the records."
        confirmLabel="Remove member"
        pending={remove.isPending}
        onConfirm={() => remove.mutate(m.id, { onSuccess: () => router.push("/members") })}
      />
    </>
  );
}

function Stat({ label, value, tone }: { label: string; value: ReactNode; tone?: "danger" }) {
  return (
    <div className="rounded-lg border border-border bg-surface p-4">
      <p className="label-caps">{label}</p>
      <p className={`mt-2 font-display text-2xl font-medium tabular-nums ${tone === "danger" ? "text-danger" : "text-ink"}`}>{value}</p>
    </div>
  );
}

function MemberLoans({ memberId, status }: { memberId: number; status: LoanStatus }) {
  const [page, setPage] = useState(0);
  const loans = useMemberLoans(memberId, { status, page, size: 10 });
  if (loans.isError) return <ErrorState error={loans.error} onRetry={() => loans.refetch()} />;
  const rows = loans.data?.content;

  return (
    <div className="overflow-hidden rounded-lg border border-border bg-surface">
      {rows && rows.length === 0 ? (
        <EmptyState
          compact
          title={status === "ACTIVE" ? "Nothing on loan." : "No past loans."}
          description={status === "ACTIVE" ? "Books issued to this member will appear here." : "Returned books will be listed here."}
        />
      ) : (
        <Table>
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead>Book</TableHead>
              <TableHead>Issued</TableHead>
              <TableHead>{status === "ACTIVE" ? "Due" : "Returned"}</TableHead>
              <TableHead>Status</TableHead>
              {status === "ACTIVE" ? <TableHead className="text-right">Actions</TableHead> : <TableHead className="text-right">Fine</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {(rows ?? []).map((loan) => (
              <TableRow key={loan.id}>
                <TableCell>
                  <div className="flex items-center gap-3">
                    <BookCover isbn={loan.book.isbn13} title={loan.book.title} size="xs" />
                    <Link href={`/books/${loan.book.id}`} className="truncate font-medium hover:underline">
                      {loan.book.title}
                    </Link>
                  </div>
                </TableCell>
                <TableCell className="font-mono text-sm">{formatDate(loan.issuedAt)}</TableCell>
                <TableCell className="font-mono text-sm">{formatDate(status === "ACTIVE" ? loan.dueDate : loan.returnedAt)}</TableCell>
                <TableCell>
                  <LoanStatusBadge loan={loan} />
                </TableCell>
                <TableCell className="text-right">
                  {status === "ACTIVE" ? (
                    <LoanActions loan={loan} />
                  ) : loan.fine ? (
                    <FineStatusBadge status={loan.fine.status} amount={loan.fine.amount} />
                  ) : (
                    <span className="text-ink-muted">—</span>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {loans.data && loans.data.totalElements > 10 ? <Pagination page={page} size={10} total={loans.data.totalElements} onPageChange={setPage} /> : null}
    </div>
  );
}

function MemberFines({ memberId }: { memberId: number }) {
  const fines = useMemberFines(memberId);
  if (fines.isError) return <ErrorState error={fines.error} onRetry={() => fines.refetch()} />;
  const rows = fines.data?.content;
  return (
    <div className="overflow-hidden rounded-lg border border-border bg-surface">
      {rows && rows.length === 0 ? (
        <EmptyState compact title="No fines." description="Every book came back on time." />
      ) : (
        <Table>
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead>Book</TableHead>
              <TableHead className="text-right">Days late</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(rows ?? []).map((fine) => (
              <TableRow key={fine.id}>
                <TableCell className="font-medium">{fine.book.title}</TableCell>
                <TableCell className="text-right font-mono">{fine.overdueDays}</TableCell>
                <TableCell className="text-right font-mono">{formatMoney(fine.amount)}</TableCell>
                <TableCell>
                  <FineStatusBadge status={fine.status} />
                  {fine.resolutionNote ? <p className="mt-1 max-w-xs truncate text-xs text-ink-muted">{fine.resolutionNote}</p> : null}
                </TableCell>
                <TableCell>
                  <FineActions fine={fine} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
