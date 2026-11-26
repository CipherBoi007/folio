import * as React from "react";
import { cn } from "cn";

/** 36px input, 6px radius, hairline border; focus = accent border + faint accent ring. */
const inputClassName = [
  "h-9 w-full min-w-0 rounded-md border border-input bg-surface px-3 text-base text-ink",
  "transition-[border-color,box-shadow] duration-150 ease-out outline-none",
  "placeholder:text-ink-muted/80",
  "focus-visible:border-brand focus-visible:ring-[3px] focus-visible:ring-brand/15 focus-visible:outline-none",
  "disabled:cursor-not-allowed disabled:bg-surface-muted disabled:opacity-60",
  "aria-invalid:border-danger aria-invalid:focus-visible:ring-danger/15",
  "file:inline-flex file:h-6 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-ink",
].join(" ");

function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return <input type={type} data-slot="input" className={cn(inputClassName, className)} {...props} />;
}

export { Input, inputClassName };
