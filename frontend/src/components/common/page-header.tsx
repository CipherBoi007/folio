import type { ReactNode } from "react";
import { cn } from "cn";

/** Page title in Fraunces 28px, a one-line muted description, and actions on the right. */
export function PageHeader({
  title,
  description,
  actions,
  eyebrow,
  className,
}: {
  title: ReactNode;
  description?: ReactNode;
  actions?: ReactNode;
  eyebrow?: ReactNode;
  className?: string;
}) {
  return (
    <header className={cn("flex flex-col gap-4 pb-6 sm:flex-row sm:items-end sm:justify-between", className)}>
      <div className="min-w-0 space-y-1">
        {eyebrow ? <div className="label-caps">{eyebrow}</div> : null}
        <h1 className="font-display text-2xl font-medium text-ink">{title}</h1>
        {description ? <p className="text-base text-ink-muted">{description}</p> : null}
      </div>
      {actions ? <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div> : null}
    </header>
  );
}
