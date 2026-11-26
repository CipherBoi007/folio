"use client";

import { useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/common/empty-state";
import { MemberAvatar } from "@/components/common/member-avatar";
import { MemberPicker } from "@/components/circulation/pickers";
import { formatDate } from "@/lib/format";
import { useBookWaitlist, useCancelWaitlist, useJoinWaitlist, useSettings } from "@/lib/queries";
import type { BookDetail, MemberSummary } from "@/types/api";

export function WaitlistPanel({ book }: { book: BookDetail }) {
  const queue = useBookWaitlist(book.id);
  const join = useJoinWaitlist(book.id);
  const cancel = useCancelWaitlist();
  const settings = useSettings();
  const [member, setMember] = useState<MemberSummary | null>(null);
  const outOfStock = book.availableQuantity === 0;

  return (
    <div className="rounded-lg border border-border bg-surface">
      {outOfStock ? (
        <div className="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <div className="flex-1">
            <MemberPicker value={member} onChange={setMember} maxLoans={settings.data?.maxActiveLoans ?? 3} />
          </div>
          <Button disabled={!member || join.isPending} onClick={() => member && join.mutate(member.id, { onSuccess: () => setMember(null) })}>
            Add to waitlist
          </Button>
        </div>
      ) : (
        <p className="border-b border-border px-5 py-3 text-sm text-ink-muted">Copies are on the shelf. Members can join the waitlist when every copy is out.</p>
      )}
      {queue.data && queue.data.length === 0 ? (
        <EmptyState compact title="Nobody is waiting." description="Members in the queue are shown at the desk when a copy comes back." />
      ) : (
        <ol className="divide-y divide-border">
          {(queue.data ?? []).map((entry) => (
            <li key={entry.id} className="flex items-center gap-3 px-5 py-3">
              <span className="w-6 font-mono text-sm text-ink-muted">#{entry.position}</span>
              <MemberAvatar name={entry.member.fullName} size="sm" />
              <div className="min-w-0 flex-1">
                <Link href={`/members/${entry.member.id}`} className="font-medium hover:underline">
                  {entry.member.fullName}
                </Link>
                <p className="text-xs text-ink-muted">Waiting since {formatDate(entry.createdAt)}</p>
              </div>
              <Button variant="ghost" size="xs" disabled={cancel.isPending} onClick={() => cancel.mutate(entry.id)}>
                Remove
              </Button>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}
