import * as React from "react"
import { cn } from "cn"

function Textarea({ className, ...props }: React.ComponentProps<"textarea">) {
  return (
    <textarea
      data-slot="textarea"
      className={cn(
        "flex field-sizing-content min-h-20 w-full rounded-md border border-input bg-surface px-3 py-2 text-base text-ink transition-[border-color,box-shadow] outline-none placeholder:text-ink-muted/80 focus-visible:border-brand focus-visible:ring-[3px] focus-visible:ring-brand/15 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-danger",
        className
      )}
      {...props}
    />
  )
}

export { Textarea }
