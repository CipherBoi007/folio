import { cn } from "cn";
import type { ComponentProps } from "react";

/** Keyboard hint, e.g. <Kbd>Ctrl K</Kbd>. */
export function Kbd({ className, ...props }: ComponentProps<"kbd">) {
  return (
    <kbd
      className={cn(
        "inline-flex h-5 min-w-5 items-center justify-center rounded-sm border border-border bg-surface px-1 font-mono text-[11px] leading-none font-medium text-ink-muted",
        className,
      )}
      {...props}
    />
  );
}
