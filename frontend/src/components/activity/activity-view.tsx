"use client";

import { useMemo } from "react";
import { format, isToday, isYesterday, parseISO } from "date-fns";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ActivityItem } from "@/components/activity/activity-item";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { FilterSelect } from "@/components/common/filters";
import { PageHeader } from "@/components/common/page-header";
import { useUrlState } from "@/hooks/use-url-state";
import { useActivity, useStaff } from "@/lib/queries";
import type { ActivityEntry } from "@/types/api";

function dayLabel(iso: string) {
  const date = parseISO(iso);
  if (isToday(date)) return "Today";
  if (isYesterday(date)) return "Yesterday";
  return format(date, "EEEE, d MMMM yyyy");
}

export function ActivityView() {
  const url = useUrlState();
  const staff = useStaff();
  const actorId = url.get("actor");
  const entityType = url.get("entity");
  const page = url.getNumber("page", 0);
  const activity = useActivity({ actorId, entityType, page, size: 40 });

  const groups = useMemo(() => {
    const map = new Map<string, ActivityEntry[]>();
    for (const entry of activity.data?.content ?? []) {
      const key = dayLabel(entry.createdAt);
      map.set(key, [...(map.get(key) ?? []), entry]);
    }
    return [...map.entries()];
  }, [activity.data]);

  return (
    <>
      <PageHeader title="Activity" description="Who did what, and when. Every change made in Folio is recorded here permanently." />

      <div className="mb-4 flex flex-wrap gap-2">
        <FilterSelect
          ariaLabel="Staff member"
          allLabel="All staff"
          value={actorId}
          onChange={(value) => url.set({ actor: value })}
          options={(staff.data ?? []).map((s) => ({ value: String(s.id), label: s.name }))}
        />
        <FilterSelect
          ariaLabel="Record type"
          allLabel="Everything"
          value={entityType}
          onChange={(value) => url.set({ entity: value })}
          options={[
            { value: "LOAN", label: "Loans" },
            { value: "BOOK", label: "Books and stock" },
            { value: "MEMBER", label: "Members" },
            { value: "FINE", label: "Fines" },
            { value: "SETTINGS", label: "Settings" },
          ]}
        />
      </div>

      {activity.isError ? (
        <ErrorState error={activity.error} onRetry={() => activity.refetch()} />
      ) : !activity.data ? (
        <Skeleton className="h-96" />
      ) : groups.length === 0 ? (
        <div className="rounded-lg border border-border bg-surface">
          <EmptyState compact title="No activity matches." description="Try another staff member or record type." />
        </div>
      ) : (
        <div className="space-y-6">
          {groups.map(([day, entries]) => (
            <section key={day}>
              <h2 className="label-caps mb-2">{day}</h2>
              <ul className="divide-y divide-border rounded-lg border border-border bg-surface px-5">
                {entries.map((entry) => (
                  <ActivityItem key={entry.id} entry={entry} />
                ))}
              </ul>
            </section>
          ))}
          <div className="flex items-center justify-between">
            <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => url.set({ page: page - 1 })}>
              Newer
            </Button>
            <span className="text-sm text-ink-muted">
              Page {page + 1} of {Math.max(1, activity.data.totalPages)}
            </span>
            <Button variant="secondary" size="sm" disabled={page + 1 >= activity.data.totalPages} onClick={() => url.set({ page: page + 1 })}>
              Older
            </Button>
          </div>
        </div>
      )}
    </>
  );
}
