"use client";

import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ActivityItem } from "@/components/activity/activity-item";
import { BookCover } from "@/components/common/book-cover";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { KpiCard, KpiCardSkeleton } from "@/components/common/kpi-card";
import { PageHeader } from "@/components/common/page-header";
import { SectionCard } from "@/components/common/section-card";
import { LoanStatusBadge } from "@/components/common/status-badges";
import { LoanActions } from "@/components/circulation/loan-actions";
import { LoanTrendChart } from "@/components/dashboard/loan-trend-chart";
import { formatGreetingDate, formatMoneyShort, formatNumber } from "@/lib/format";
import { useActivity, useDashboard, useLoans } from "@/lib/queries";
import type { Loan } from "@/types/api";

export function DashboardView() {
  const { summary, trend, topBooks, genres } = useDashboard();
  const dueToday = useLoans({ dueToday: true, size: 6, sort: "issued,asc" });
  const overdue = useLoans({ overdue: true, size: 6, sort: "due,asc" });
  const activity = useActivity({ size: 8 });
  const s = summary.data;

  return (
    <>
      <PageHeader
        eyebrow={formatGreetingDate()}
        title="Today at the desk"
        description={
          s ? `${s.dueToday} ${s.dueToday === 1 ? "book is" : "books are"} due back today and ${s.overdue} ${s.overdue === 1 ? "is" : "are"} overdue.` : "Loading today's figures…"
        }
        actions={
          <Button asChild>
            <Link href="/circulation">Issue book</Link>
          </Button>
        }
      />

      {summary.isError ? (
        <ErrorState error={summary.error} onRetry={() => summary.refetch()} />
      ) : (
        <section aria-label="Key figures" className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-5">
          {s ? (
            <>
              <KpiCard label="Titles" value={formatNumber(s.titles)} delta={`+${s.titlesAddedThisWeek} this week`} href="/books" />
              <KpiCard label="Copies on shelf" value={formatNumber(s.copiesOnShelf)} delta={`of ${formatNumber(s.copies)} copies`} href="/books?availability=IN_STOCK" />
              <KpiCard label="On loan" value={formatNumber(s.onLoan)} delta={`${s.issuedThisWeek} issued this week`} href="/circulation?tab=return" />
              <KpiCard
                label="Overdue"
                value={formatNumber(s.overdue)}
                tone={s.overdue > 0 ? "danger" : "default"}
                delta={`${s.dueToday} due today`}
                href="/circulation?tab=return"
              />
              <KpiCard label="Unpaid fines" value={formatMoneyShort(s.unpaidFines)} delta={`${s.unpaidFineCount} open ${s.unpaidFineCount === 1 ? "fine" : "fines"}`} href="/fines" />
            </>
          ) : (
            Array.from({ length: 5 }, (_, i) => <KpiCardSkeleton key={i} />)
          )}
        </section>
      )}

      <div className="mt-6 grid gap-6 xl:grid-cols-[2fr_1fr]">
        <SectionCard
          title="Loans, last 30 days"
          actions={
            <div className="flex items-center gap-4 text-sm text-ink-muted">
              <span className="flex items-center gap-1.5">
                <span className="h-0.5 w-3 rounded-full bg-brand" /> Issued
              </span>
              <span className="flex items-center gap-1.5">
                <span className="h-0.5 w-3 rounded-full bg-chart-2" /> Returned
              </span>
            </div>
          }
          bodyClassName="px-3 py-4"
        >
          {trend.data ? <LoanTrendChart data={trend.data} /> : <Skeleton className="mx-2 h-56" />}
        </SectionCard>

        <SectionCard title="Collection by genre" bodyClassName="px-5 py-4">
          {genres.data ? (
            <ul className="space-y-2.5">
              {genres.data.slice(0, 8).map((g) => {
                const max = Math.max(...genres.data.map((x) => x.titles), 1);
                return (
                  <li key={g.id}>
                    <Link href={`/books?genre=${g.id}`} className="group block">
                      <div className="flex items-baseline justify-between text-sm">
                        <span className="text-ink group-hover:underline">{g.name}</span>
                        <span className="font-mono text-ink-muted tabular-nums">{g.titles}</span>
                      </div>
                      <div className="mt-1 h-1.5 overflow-hidden rounded-full bg-surface-muted">
                        <div className="h-full rounded-full bg-brand/80" style={{ width: `${(g.titles / max) * 100}%` }} />
                      </div>
                    </Link>
                  </li>
                );
              })}
            </ul>
          ) : (
            <Skeleton className="h-56" />
          )}
        </SectionCard>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <LoanList title="Due today" query={dueToday} emptyTitle="Nothing due today." emptyText="No returns expected before close." />
        <LoanList title="Overdue" query={overdue} emptyTitle="No overdue loans." emptyText="The shelves thank you." viewAll="/circulation?tab=return" />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_1fr]">
        <SectionCard title="Most borrowed" description="Last 90 days" bodyClassName="px-5 py-2">
          {topBooks.data ? (
            topBooks.data.length ? (
              <ol className="divide-y divide-border">
                {topBooks.data.map((book, index) => (
                  <li key={book.id} className="flex items-center gap-4 py-3">
                    <span className="w-4 font-mono text-sm text-ink-muted">{index + 1}</span>
                    <BookCover isbn={book.isbn13} title={book.title} size="sm" />
                    <div className="min-w-0 flex-1">
                      <Link href={`/books/${book.id}`} className="block truncate font-medium hover:underline">
                        {book.title}
                      </Link>
                      <p className="truncate text-sm text-ink-muted">{book.authors}</p>
                    </div>
                    <span className="font-mono text-sm text-ink-muted tabular-nums">
                      {book.loans} {book.loans === 1 ? "loan" : "loans"}
                    </span>
                  </li>
                ))}
              </ol>
            ) : (
              <EmptyState compact title="No loans yet." description="Popular titles show up here once books circulate." />
            )
          ) : (
            <Skeleton className="my-3 h-64" />
          )}
        </SectionCard>

        <SectionCard
          title="Recent activity"
          actions={
            <Button asChild variant="ghost" size="xs">
              <Link href="/activity">
                View all <ArrowRight />
              </Link>
            </Button>
          }
          bodyClassName="px-5 py-1"
        >
          {activity.data ? (
            <ul className="divide-y divide-border">
              {activity.data.content.map((entry) => (
                <ActivityItem key={entry.id} entry={entry} compact />
              ))}
            </ul>
          ) : (
            <Skeleton className="my-3 h-64" />
          )}
        </SectionCard>
      </div>
    </>
  );
}

function LoanList({
  title,
  query,
  emptyTitle,
  emptyText,
  viewAll,
}: {
  title: string;
  query: ReturnType<typeof useLoans>;
  emptyTitle: string;
  emptyText: string;
  viewAll?: string;
}) {
  const loans: Loan[] | undefined = query.data?.content;
  return (
    <SectionCard
      title={
        <span className="flex items-center gap-2">
          {title}
          {query.data ? <span className="font-mono text-sm text-ink-muted">{query.data.totalElements}</span> : null}
        </span>
      }
      actions={
        viewAll && query.data && query.data.totalElements > 0 ? (
          <Button asChild variant="ghost" size="xs">
            <Link href={viewAll}>
              View all <ArrowRight />
            </Link>
          </Button>
        ) : null
      }
      bodyClassName="px-5 py-1"
    >
      {query.isError ? (
        <ErrorState error={query.error} onRetry={() => query.refetch()} />
      ) : !loans ? (
        <Skeleton className="my-3 h-40" />
      ) : loans.length === 0 ? (
        <EmptyState compact title={emptyTitle} description={emptyText} className="px-0" />
      ) : (
        <ul className="divide-y divide-border">
          {loans.map((loan) => (
            <li key={loan.id} className="flex items-center gap-3 py-3">
              <BookCover isbn={loan.book.isbn13} title={loan.book.title} size="xs" />
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{loan.book.title}</p>
                <Link href={`/members/${loan.member.id}`} className="truncate text-sm text-ink-muted hover:underline">
                  {loan.member.fullName}
                </Link>
              </div>
              <LoanStatusBadge loan={loan} />
              <LoanActions loan={loan} />
            </li>
          ))}
        </ul>
      )}
    </SectionCard>
  );
}
