"use client"

import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"
import { Tabs as TabsPrimitive } from "radix-ui"

function Tabs({
  className,
  orientation = "horizontal",
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Root>) {
  return (
    <TabsPrimitive.Root
      data-slot="tabs"
      data-orientation={orientation}
      className={cn(
        "group/tabs flex gap-4 data-horizontal:flex-col",
        className
      )}
      {...props}
    />
  )
}

const tabsListVariants = cva(
  "group/tabs-list inline-flex w-fit items-center text-ink-muted group-data-vertical/tabs:flex-col",
  {
    variants: {
      variant: {
        /** Underline tabs on a hairline — used for page/section tabs. */
        default: "h-10 gap-5 border-b border-border w-full justify-start overflow-x-auto overflow-y-hidden [scrollbar-width:none]",
        /** Compact segmented control — used for view toggles inside toolbars. */
        segmented: "h-9 gap-0.5 rounded-md border border-input bg-surface p-0.5",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
)

function TabsList({
  className,
  variant = "default",
  ...props
}: React.ComponentProps<typeof TabsPrimitive.List> &
  VariantProps<typeof tabsListVariants>) {
  return (
    <TabsPrimitive.List
      data-slot="tabs-list"
      data-variant={variant}
      className={cn(tabsListVariants({ variant }), className)}
      {...props}
    />
  )
}

function TabsTrigger({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Trigger>) {
  return (
    <TabsPrimitive.Trigger
      data-slot="tabs-trigger"
      className={cn(
        "relative inline-flex shrink-0 items-center justify-center gap-1.5 text-base font-medium whitespace-nowrap text-ink-muted transition-colors duration-150 hover:text-ink disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        "group-data-[variant=default]/tabs-list:h-full group-data-[variant=default]/tabs-list:data-active:text-ink",
        "group-data-[variant=default]/tabs-list:after:absolute group-data-[variant=default]/tabs-list:after:inset-x-0 group-data-[variant=default]/tabs-list:after:-bottom-px group-data-[variant=default]/tabs-list:after:h-0.5 group-data-[variant=default]/tabs-list:after:bg-brand group-data-[variant=default]/tabs-list:after:opacity-0 group-data-[variant=default]/tabs-list:data-active:after:opacity-100",
        "group-data-[variant=segmented]/tabs-list:h-full group-data-[variant=segmented]/tabs-list:rounded-[4px] group-data-[variant=segmented]/tabs-list:px-2.5 group-data-[variant=segmented]/tabs-list:text-sm group-data-[variant=segmented]/tabs-list:data-active:bg-surface-muted group-data-[variant=segmented]/tabs-list:data-active:text-ink",
        className
      )}
      {...props}
    />
  )
}

function TabsContent({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Content>) {
  return (
    <TabsPrimitive.Content
      data-slot="tabs-content"
      className={cn("flex-1 outline-none", className)}
      {...props}
    />
  )
}

export { Tabs, TabsList, TabsTrigger, TabsContent, tabsListVariants }
