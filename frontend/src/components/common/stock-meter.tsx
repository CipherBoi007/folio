import { cn } from "cn";

export type StockLevel = "available" | "low" | "out";

/** Low stock = at most 1 copy left, or 20% or less of the total. */
export function stockLevel(available: number, total: number): StockLevel {
  if (available <= 0) return "out";
  if (available === 1 || available / Math.max(total, 1) <= 0.2) return "low";
  return "available";
}

const fillByLevel: Record<StockLevel, string> = {
  available: "bg-brand",
  low: "bg-warning",
  out: "bg-danger",
};

/** A thin 4px bar of available/total plus "3 / 5" in mono. Used in tables and on the book page. */
export function StockMeter({
  available,
  total,
  className,
  barClassName,
  showLabel = true,
}: {
  available: number;
  total: number;
  className?: string;
  barClassName?: string;
  showLabel?: boolean;
}) {
  const level = stockLevel(available, total);
  const percent = total > 0 ? Math.min(100, Math.round((available / total) * 100)) : 0;

  return (
    <div className={cn("inline-flex items-center gap-2.5", className)}>
      <div
        role="meter"
        aria-valuemin={0}
        aria-valuemax={total}
        aria-valuenow={available}
        aria-label={`${available} of ${total} copies available`}
        className={cn("h-1 w-16 overflow-hidden rounded-full bg-surface-muted", barClassName)}
      >
        <div className={cn("h-full rounded-full transition-[width] duration-200", fillByLevel[level])} style={{ width: `${percent}%` }} />
      </div>
      {showLabel ? (
        <span className="font-mono text-sm text-ink tabular-nums">
          {available}
          <span className="text-ink-muted"> / {total}</span>
        </span>
      ) : null}
    </div>
  );
}
