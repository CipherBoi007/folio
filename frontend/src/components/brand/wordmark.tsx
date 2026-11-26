import { cn } from "cn";

/** The "Folio" wordmark, set in Fraunces. `compact` renders the "F" monogram for the collapsed sidebar. */
export function Wordmark({ className, compact = false }: { className?: string; compact?: boolean }) {
  return (
    <span
      className={cn("font-display text-[22px] leading-none font-semibold tracking-[-0.02em] select-none", className)}
      aria-label="Folio"
    >
      {compact ? "F" : "Folio"}
    </span>
  );
}
