import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "cn";
import { Slot } from "radix-ui";

/**
 * Folio button. 6px radius, no shadows, no gradients. `default` is the single primary action on a page;
 * `secondary` is the hairline-bordered workhorse; `danger` is reserved for confirmed destructive actions.
 */
const buttonVariants = cva(
  [
    "inline-flex shrink-0 items-center justify-center gap-1.5 rounded-md border font-medium whitespace-nowrap select-none",
    "transition-[background-color,border-color,color] duration-150 ease-out",
    "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
    "disabled:pointer-events-none disabled:opacity-50",
    "[&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
  ].join(" "),
  {
    variants: {
      variant: {
        default: "border-transparent bg-brand text-brand-foreground hover:bg-brand-hover",
        secondary:
          "border-input bg-surface text-ink hover:bg-surface-muted aria-expanded:bg-surface-muted",
        outline:
          "border-input bg-surface text-ink hover:bg-surface-muted aria-expanded:bg-surface-muted",
        ghost:
          "border-transparent bg-transparent text-ink-muted hover:bg-surface-muted hover:text-ink aria-expanded:bg-surface-muted aria-expanded:text-ink",
        danger: "border-transparent bg-danger text-white hover:bg-danger/90 dark:text-background",
        "danger-ghost":
          "border-transparent bg-transparent text-danger hover:bg-danger-soft",
        destructive: "border-transparent bg-danger text-white hover:bg-danger/90 dark:text-background",
        link: "h-auto border-transparent px-0 text-brand underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-3.5 text-base",
        sm: "h-8 px-3 text-sm",
        xs: "h-7 gap-1 px-2 text-xs [&_svg:not([class*='size-'])]:size-3.5",
        lg: "h-10 px-4 text-base",
        icon: "size-9",
        "icon-sm": "size-8",
        "icon-xs": "size-7 [&_svg:not([class*='size-'])]:size-3.5",
        "icon-lg": "size-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  },
);

function Button({
  className,
  variant = "default",
  size = "default",
  asChild = false,
  ...props
}: React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
  }) {
  const Comp = asChild ? Slot.Root : "button";

  return (
    <Comp
      data-slot="button"
      data-variant={variant}
      data-size={size}
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  );
}

export { Button, buttonVariants };
