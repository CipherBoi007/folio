import Link from "next/link";
import { BookOpen, IndianRupee, Settings2, Undo2, ArrowRightLeft, User } from "lucide-react";
import { formatDateTime, formatRelative } from "@/lib/format";
import type { ActivityEntry } from "@/types/api";

function href(entry: ActivityEntry): string | null {
  if (!entry.entityId) return null;
  if (entry.entityType === "BOOK") return `/books/${entry.entityId}`;
  if (entry.entityType === "MEMBER") return `/members/${entry.entityId}`;
  const memberId = entry.metadata?.memberId;
  if (entry.entityType === "LOAN" && typeof memberId === "number") return `/members/${memberId}`;
  return null;
}

function Icon({ entry }: { entry: ActivityEntry }) {
  const className = "size-3.5";
  if (entry.entityType === "BOOK") return <BookOpen className={className} />;
  if (entry.entityType === "MEMBER") return <User className={className} />;
  if (entry.entityType === "FINE") return <IndianRupee className={className} />;
  if (entry.entityType === "SETTINGS") return <Settings2 className={className} />;
  return entry.action === "LOAN_RETURNED" ? <Undo2 className={className} /> : <ArrowRightLeft className={className} />;
}

export function ActivityItem({ entry, compact = false }: { entry: ActivityEntry; compact?: boolean }) {
  const link = href(entry);
  return (
    <li className="flex gap-3 py-3">
      <span className="mt-0.5 inline-flex size-6 shrink-0 items-center justify-center rounded-full border border-border text-ink-muted">
        <Icon entry={entry} />
      </span>
      <div className="min-w-0 flex-1">
        <p className="text-base text-ink">
          {link ? (
            <Link href={link} className="hover:underline">
              {entry.summary}
            </Link>
          ) : (
            entry.summary
          )}
        </p>
        <p className="mt-0.5 text-sm text-ink-muted">
          {entry.actor.name}
          {" · "}
          <time dateTime={entry.createdAt} title={formatDateTime(entry.createdAt)}>
            {compact ? formatRelative(entry.createdAt) : formatDateTime(entry.createdAt).split(", ")[1]}
          </time>
        </p>
      </div>
    </li>
  );
}
