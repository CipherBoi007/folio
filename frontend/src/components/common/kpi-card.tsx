import Link from "next/link";
import type { ReactNode } from "react";
import { cn } from "cn";
import { Skeleton } from "@/components/ui/skeleton";

/** KPI: 12px muted label, big Fraunces number, small muted delta line. White surface, hairline, no icons. */
export function KpiCard({
  label,
  value,
  delta,
  href,
  tone = "default",
  className,
}: {
  label: string;
  value: ReactNode;
  delta?: ReactNode;
  href?: string;
  tone?: "default" | "danger" | "warning";
  className?: string;
}) {
  const body = (
    <>
      <div className="label-caps">{label}</div>
      <div
        className={cn(
          "mt-3 font-display text-3xl font-medium tabular-nums",
          tone === "danger" && "text-danger",
          tone === "warning" && "text-warning",
        )}
      >
        {value}
      </div>
      {delta ? <div className="mt-1.5 text-sm text-ink-muted">{delta}</div> : <div className="mt-1.5 h-[18px]" />}
    </>
  );

  const classes = cn("rounded-lg border border-border bg-surface px-5 py-4", className);

  return href ? (
    <Link href={href} className={cn(classes, "transition-colors duration-150 hover:border-input hover:bg-surface-muted/40")}>
      {body}
    </Link>
  ) : (
    <div className={classes}>{body}</div>
  );
}

export function KpiCardSkeleton() {
  return (
    <div className="rounded-lg border border-border bg-surface px-5 py-4">
      <Skeleton className="h-3 w-20" />
      <Skeleton className="mt-4 h-9 w-24" />
      <Skeleton className="mt-2.5 h-3 w-28" />
    </div>
  );
}
