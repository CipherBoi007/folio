import * as React from "react";
import { cn } from "cn";

/**
 * Dense data table (CLAUDE.md §8): 44px rows, 13px text, sticky header with small-caps-style muted labels,
 * hairline row dividers, hover = surface-muted. Numeric cells should add `text-right font-mono`.
 */
function Table({ className, containerClassName, ...props }: React.ComponentProps<"table"> & { containerClassName?: string }) {
  return (
    <div data-slot="table-container" className={cn("relative w-full overflow-auto scrollbar-thin", containerClassName)}>
      <table data-slot="table" className={cn("w-full caption-bottom border-separate border-spacing-0 text-sm", className)} {...props} />
    </div>
  );
}

function TableHeader({ className, ...props }: React.ComponentProps<"thead">) {
  return <thead data-slot="table-header" className={cn("sticky top-0 z-10 bg-surface", className)} {...props} />;
}

function TableBody({ className, ...props }: React.ComponentProps<"tbody">) {
  return <tbody data-slot="table-body" className={cn("[&_tr:last-child_td]:border-b-0", className)} {...props} />;
}

function TableFooter({ className, ...props }: React.ComponentProps<"tfoot">) {
  return <tfoot data-slot="table-footer" className={cn("bg-surface-muted font-medium", className)} {...props} />;
}

function TableRow({ className, ...props }: React.ComponentProps<"tr">) {
  return (
    <tr
      data-slot="table-row"
      className={cn(
        "transition-colors duration-150 hover:bg-surface-muted/70 data-[state=selected]:bg-brand-soft/60",
        className,
      )}
      {...props}
    />
  );
}

function TableHead({ className, ...props }: React.ComponentProps<"th">) {
  return (
    <th
      data-slot="table-head"
      className={cn(
        "label-caps h-9 border-b border-border px-3 text-left align-middle whitespace-nowrap first:pl-5 last:pr-5",
        className,
      )}
      {...props}
    />
  );
}

function TableCell({ className, ...props }: React.ComponentProps<"td">) {
  return (
    <td
      data-slot="table-cell"
      className={cn(
        "h-11 border-b border-border px-3 align-middle whitespace-nowrap text-ink first:pl-5 last:pr-5",
        className,
      )}
      {...props}
    />
  );
}

function TableCaption({ className, ...props }: React.ComponentProps<"caption">) {
  return <caption data-slot="table-caption" className={cn("mt-4 text-sm text-ink-muted", className)} {...props} />;
}

export { Table, TableHeader, TableBody, TableFooter, TableHead, TableRow, TableCell, TableCaption };
