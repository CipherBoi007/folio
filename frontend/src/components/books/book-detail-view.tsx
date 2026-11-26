"use client";

import { useState, type ReactNode } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeftRight, Minus, Pencil, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AdminOnly } from "@/components/common/admin-only";
import { BookCover } from "@/components/common/book-cover";
import { ConfirmDialog } from "@/components/common/confirm-dialog";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { AvailabilityBadge, LoanStatusBadge } from "@/components/common/status-badges";
import { StockMeter } from "@/components/common/stock-meter";
import { Pagination } from "@/components/data-table/pagination";
import { BookSheet } from "@/components/books/book-sheet";
import { StockDialog } from "@/components/books/stock-dialog";
import { WaitlistPanel } from "@/components/books/waitlist-panel";
import { LoanActions } from "@/components/circulation/loan-actions";
import { LedgerTimeline } from "@/components/inventory/ledger-timeline";
import { useBreadcrumbLabel } from "@/components/layout/breadcrumbs";
import { formatDate } from "@/lib/format";
import { formatIsbn } from "@/lib/isbn";
import { useBook, useBookLoans, useBookTransactions, useDeleteBook } from "@/lib/queries";
import type { BookDetail, LoanStatus } from "@/types/api";

export function BookDetailView({ id }: { id: number }) {
  const book = useBook(id);
  const router = useRouter();
  const remove = useDeleteBook();
  const [editing, setEditing] = useState(false);
  const [stock, setStock] = useState<"in" | "out" | null>(null);
  const [deleting, setDeleting] = useState(false);
  useBreadcrumbLabel(book.data?.title);

  if (book.isError) {
    return (
      <div className="rounded-lg border border-border bg-surface">
        <ErrorState error={book.error} title="This book couldn't be loaded." onRetry={() => book.refetch()} />
      </div>
    );
  }
  if (!book.data) return <DetailSkeleton />;
  const b = book.data;

  return (
    <>
      <div className="flex flex-col gap-8 md:flex-row">
        <div className="shrink-0">
          <BookCover isbn={b.isbn13} title={b.title} author={b.authors[0]} size="xl" priority />
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="min-w-0">
              <p className="label-caps">{b.genre.name}</p>
              <h1 className="mt-1 font-display text-3xl font-medium text-ink">{b.title}</h1>
              {b.subtitle ? <p className="mt-1 text-lg text-ink-muted">{b.subtitle}</p> : null}
              <p className="mt-2 text-lg text-ink">{b.authors.join(", ")}</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => setEditing(true)}>
                <Pencil />
                Edit
              </Button>
              <AdminOnly action="delete books">
                <Button variant="danger-ghost" onClick={() => setDeleting(true)}>
                  <Trash2 />
                  Delete
                </Button>
              </AdminOnly>
            </div>
          </div>

          <dl className="mt-6 grid grid-cols-2 gap-x-8 gap-y-4 sm:grid-cols-3">
            <Meta label="ISBN" value={<span className="font-mono">{formatIsbn(b.isbn13)}</span>} />
            <Meta label="Publisher" value={b.publisher} />
            <Meta label="Published" value={b.publishedYear ? <span className="font-mono">{b.publishedYear}</span> : null} />
            <Meta label="Pages" value={b.pages ? <span className="font-mono">{b.pages}</span> : null} />
            <Meta label="Language" value={b.language.name} />
            <Meta label="Shelf" value={b.shelfLocation ? <span className="font-mono">{b.shelfLocation}</span> : null} />
          </dl>

          {b.description ? <p className="mt-6 max-w-prose text-base leading-relaxed text-ink-muted">{b.description}</p> : null}

          <section className="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-lg border border-border bg-surface p-5">
            <div>
              <div className="flex items-center gap-3">
                <span className="font-display text-3xl font-medium tabular-nums">{b.availableQuantity}</span>
                <span className="text-base text-ink-muted">
                  of {b.totalQuantity} {b.totalQuantity === 1 ? "copy" : "copies"} on the shelf
                </span>
                <AvailabilityBadge available={b.availableQuantity} total={b.totalQuantity} />
              </div>
              <StockMeter available={b.availableQuantity} total={b.totalQuantity} showLabel={false} className="mt-3" barClassName="w-64" />
              <p className="mt-2 text-sm text-ink-muted">{b.activeLoans} on loan</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="secondary" onClick={() => setStock("in")}>
                <Plus />
                Add copies
              </Button>
              <AdminOnly action="remove copies">
                <Button variant="secondary" onClick={() => setStock("out")} disabled={b.availableQuantity === 0}>
                  <Minus />
                  Remove copies
                </Button>
              </AdminOnly>
              {b.availableQuantity > 0 ? (
                <Button asChild>
                  <Link href={`/circulation?book=${b.id}`}>
                    <ArrowLeftRight />
                    Issue
                  </Link>
                </Button>
              ) : (
                <Button disabled>
                  <ArrowLeftRight />
                  Issue
                </Button>
              )}
            </div>
          </section>
        </div>
      </div>

      <Tabs defaultValue="current" className="mt-10">
        <TabsList>
          <TabsTrigger value="current">Current loans</TabsTrigger>
          <TabsTrigger value="history">Loan history</TabsTrigger>
          <TabsTrigger value="inventory">Inventory history</TabsTrigger>
          <TabsTrigger value="waitlist">Waitlist</TabsTrigger>
        </TabsList>
        <TabsContent value="current">
          <BookLoans book={b} status="ACTIVE" />
        </TabsContent>
        <TabsContent value="history">
          <BookLoans book={b} status="RETURNED" />
        </TabsContent>
        <TabsContent value="inventory">
          <InventoryHistory bookId={b.id} />
        </TabsContent>
        <TabsContent value="waitlist">
          <WaitlistPanel book={b} />
        </TabsContent>
      </Tabs>

      <BookSheet open={editing} onOpenChange={setEditing} book={b} />
      <StockDialog book={b} direction={stock} onOpenChange={(open) => !open && setStock(null)} />
      <ConfirmDialog
        open={deleting}
        onOpenChange={setDeleting}
        destructive
        title={`Delete "${b.title}"?`}
        description="The record leaves the catalogue but its loan and inventory history is kept, and the ISBN stays reserved."
        confirmLabel="Delete book"
        pending={remove.isPending}
        onConfirm={() => remove.mutate(b.id, { onSuccess: () => router.push("/books") })}
      />
    </>
  );
}

function Meta({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div>
      <dt className="label-caps">{label}</dt>
      <dd className="mt-1 text-base text-ink">{value ?? <span className="text-ink-muted">—</span>}</dd>
    </div>
  );
}

function BookLoans({ book, status }: { book: BookDetail; status: LoanStatus }) {
  const [page, setPage] = useState(0);
  const loans = useBookLoans(book.id, { status, page, size: 10 });

  if (loans.isError) return <ErrorState error={loans.error} onRetry={() => loans.refetch()} />;
  const rows = loans.data?.content;

  return (
    <div className="overflow-hidden rounded-lg border border-border bg-surface">
      {rows && rows.length === 0 ? (
        <EmptyState
          compact
          title={status === "ACTIVE" ? "Every copy is home." : "Never borrowed yet."}
          description={status === "ACTIVE" ? "No copies of this book are on loan right now." : "Loans will appear here once the book circulates."}
        />
      ) : (
        <Table>
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead>Member</TableHead>
              <TableHead>Issued</TableHead>
              <TableHead>{status === "ACTIVE" ? "Due" : "Returned"}</TableHead>
              <TableHead>Status</TableHead>
              {status === "ACTIVE" ? <TableHead className="text-right">Actions</TableHead> : null}
            </TableRow>
          </TableHeader>
          <TableBody>
            {(rows ?? []).map((loan) => (
              <TableRow key={loan.id}>
                <TableCell>
                  <Link href={`/members/${loan.member.id}`} className="font-medium hover:underline">
                    {loan.member.fullName}
                  </Link>
                  <span className="ml-2 font-mono text-xs text-ink-muted">{loan.member.membershipNumber}</span>
                </TableCell>
                <TableCell className="font-mono text-sm">{formatDate(loan.issuedAt)}</TableCell>
                <TableCell className="font-mono text-sm">{formatDate(status === "ACTIVE" ? loan.dueDate : loan.returnedAt)}</TableCell>
                <TableCell>
                  <LoanStatusBadge loan={loan} />
                </TableCell>
                {status === "ACTIVE" ? (
                  <TableCell>
                    <LoanActions loan={loan} />
                  </TableCell>
                ) : null}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {loans.data && loans.data.totalElements > 10 ? (
        <Pagination page={page} size={10} total={loans.data.totalElements} onPageChange={setPage} />
      ) : null}
    </div>
  );
}

function InventoryHistory({ bookId }: { bookId: number }) {
  const ledger = useBookTransactions(bookId);
  if (ledger.isError) return <ErrorState error={ledger.error} onRetry={() => ledger.refetch()} />;
  return (
    <div className="rounded-lg border border-border bg-surface p-6">
      {ledger.data ? <LedgerTimeline entries={ledger.data.content} /> : <Skeleton className="h-40 w-full" />}
    </div>
  );
}

function DetailSkeleton() {
  return (
    <div className="flex flex-col gap-8 md:flex-row">
      <Skeleton className="aspect-[2/3] w-60" />
      <div className="flex-1 space-y-3">
        <Skeleton className="h-3 w-20" />
        <Skeleton className="h-10 w-2/3" />
        <Skeleton className="h-5 w-1/3" />
        <div className="grid grid-cols-3 gap-4 pt-6">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} className="h-10" />
          ))}
        </div>
        <Skeleton className="mt-6 h-28 w-full" />
      </div>
    </div>
  );
}
