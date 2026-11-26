import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "cn";
import { Slot } from "radix-ui";

/**
 * Folio status badge: small, 4px radius, soft tinted background with darker text, optional 6px dot.
 * Copy is sentence case: "Available", "Overdue · 3d", "Unpaid ₹45".
 */
const badgeVariants = cva(
  "inline-flex h-5 w-fit shrink-0 items-center gap-1.5 rounded-sm px-1.5 text-xs font-medium whitespace-nowrap [&>svg]:size-3 [&>svg]:shrink-0",
  {
    variants: {
      tone: {
        neutral: "bg-surface-muted text-ink-muted",
        brand: "bg-brand-soft text-brand",
        success: "bg-success-soft text-success",
        warning: "bg-warning-soft text-warning",
        danger: "bg-danger-soft text-danger",
        info: "bg-info-soft text-info",
        outline: "border border-border bg-transparent text-ink-muted",
      },
    },
    defaultVariants: {
      tone: "neutral",
    },
  },
);

type BadgeProps = React.ComponentProps<"span"> &
  VariantProps<typeof badgeVariants> & {
    asChild?: boolean;
    /** Show a 6px status dot in the current text colour. */
    dot?: boolean;
  };

function Badge({ className, tone = "neutral", asChild = false, dot = false, children, ...props }: BadgeProps) {
  const Comp = asChild ? Slot.Root : "span";

  return (
    <Comp data-slot="badge" data-tone={tone} className={cn(badgeVariants({ tone }), className)} {...props}>
      {dot ? <span aria-hidden className="size-1.5 shrink-0 rounded-full bg-current" /> : null}
      {children}
    </Comp>
  );
}

export type BadgeTone = NonNullable<VariantProps<typeof badgeVariants>["tone"]>;
export { Badge, badgeVariants };
