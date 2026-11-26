import type { ReactNode } from "react";
import { cn } from "cn";

/**
 * A short Fraunces headline, one plain line of explanation, and (optionally) the action that fixes it.
 * e.g. "No overdue loans." / "The shelves thank you."
 */
export function EmptyState({
  title,
  description,
  action,
  className,
  compact = false,
}: {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
  className?: string;
  compact?: boolean;
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-start justify-center gap-1.5",
        compact ? "px-5 py-8" : "px-8 py-16 sm:px-12",
        className,
      )}
    >
      <p className={cn("font-display font-medium text-ink", compact ? "text-lg" : "text-xl")}>{title}</p>
      {description ? <p className="max-w-prose text-base text-ink-muted">{description}</p> : null}
      {action ? <div className="mt-3">{action}</div> : null}
    </div>
  );
}
