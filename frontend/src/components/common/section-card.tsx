import type { ReactNode } from "react";
import { cn } from "cn";

export function SectionCard({
  title,
  description,
  actions,
  children,
  className,
  bodyClassName,
}: {
  title: ReactNode;
  description?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
  bodyClassName?: string;
}) {
  return (
    <section className={cn("rounded-lg border border-border bg-surface", className)}>
      <header className="flex items-start justify-between gap-4 border-b border-border px-5 py-3.5">
        <div className="min-w-0">
          <h2 className="text-lg font-medium text-ink">{title}</h2>
          {description ? <p className="mt-0.5 text-sm text-ink-muted">{description}</p> : null}
        </div>
        {actions ? <div className="flex shrink-0 items-center gap-2">{actions}</div> : null}
      </header>
      <div className={bodyClassName}>{children}</div>
    </section>
  );
}
