"use client";

import { useState, type ReactNode } from "react";
import { ArrowDown, ArrowUp, ChevronsUpDown, Columns3 } from "lucide-react";
import {
  columnVisibilityFeature,
  tableFeatures,
  useTable,
  type ColumnDef,
  type ColumnVisibilityState,
  type RowData,
} from "@tanstack/react-table";
import { cn } from "cn";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

const features = tableFeatures({ columnVisibilityFeature });
export type Features = typeof features;
export type Column<T extends RowData> = ColumnDef<Features, T>;

declare module "@tanstack/react-table" {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface ColumnMeta<TFeatures, TData, TValue> {
    /** Server-side sort key; makes the header clickable. */
    sortKey?: string;
    align?: "left" | "right";
    className?: string;
    label?: string;
    hideable?: boolean;
  }
}

export interface SortState {
  field: string;
  direction: "asc" | "desc";
}

export function parseSort(raw: string | undefined, fallback: SortState): SortState {
  if (!raw) return fallback;
  const [field, direction] = raw.split(",");
  return { field, direction: direction === "desc" ? "desc" : "asc" };
}

export function formatSort(sort: SortState): string {
  return `${sort.field},${sort.direction}`;
}

interface DataTableProps<T extends RowData> {
  columns: Column<T>[];
  data: T[] | undefined;
  loading?: boolean;
  sort?: SortState;
  onSortChange?: (sort: SortState) => void;
  getRowId: (row: T) => string;
  onRowClick?: (row: T) => void;
  empty?: ReactNode;
  footer?: ReactNode;
  toolbar?: ReactNode;
  skeletonRows?: number;
  initialHidden?: string[];
}

/**
 * Dense, server-driven table: sorting and paging live in the URL and the API; the table only renders.
 * Header click cycles the column's server sort; the column menu hides optional columns.
 */
export function DataTable<T extends RowData>({
  columns,
  data,
  loading,
  sort,
  onSortChange,
  getRowId,
  onRowClick,
  empty,
  footer,
  toolbar,
  skeletonRows = 8,
  initialHidden = [],
}: DataTableProps<T>) {
  const [columnVisibility, setColumnVisibility] = useState<ColumnVisibilityState>(() =>
    Object.fromEntries(initialHidden.map((id) => [id, false])),
  );

  const table = useTable({
    features,
    columns,
    data: data ?? [],
    getRowId,
    state: { columnVisibility },
    onColumnVisibilityChange: setColumnVisibility,
  });

  const hideable = table.getAllLeafColumns().filter((column) => column.columnDef.meta?.hideable);
  const visibleCount = table.getVisibleLeafColumns().length;
  const rows = table.getRowModel().rows;

  return (
    <div className="overflow-hidden rounded-lg border border-border bg-surface">
      {toolbar || hideable.length > 0 ? (
        <div className="flex flex-wrap items-center gap-2 border-b border-border px-4 py-3">
          <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">{toolbar}</div>
          {hideable.length > 0 ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" aria-label="Choose columns">
                  <Columns3 />
                  <span className="hidden sm:inline">Columns</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
                <DropdownMenuLabel className="label-caps">Show columns</DropdownMenuLabel>
                {hideable.map((column) => (
                  <DropdownMenuCheckboxItem
                    key={column.id}
                    checked={column.getIsVisible()}
                    onCheckedChange={(checked) => column.toggleVisibility(Boolean(checked))}
                    onSelect={(e) => e.preventDefault()}
                  >
                    {column.columnDef.meta?.label ?? column.id}
                  </DropdownMenuCheckboxItem>
                ))}
              </DropdownMenuContent>
            </DropdownMenu>
          ) : null}
        </div>
      ) : null}

      <Table containerClassName="max-h-[calc(100vh-260px)] min-h-[200px]">
        <TableHeader>
          {table.getHeaderGroups().map((group) => (
            <TableRow key={group.id} className="hover:bg-transparent">
              {group.headers.map((header) => {
                const meta = header.column.columnDef.meta;
                const sortKey = meta?.sortKey;
                const active = sortKey && sort?.field === sortKey;
                return (
                  <TableHead key={header.id} className={cn(meta?.align === "right" && "text-right", meta?.className)}>
                    {sortKey && onSortChange ? (
                      <button
                        type="button"
                        className={cn("inline-flex items-center gap-1 uppercase hover:text-ink", active && "text-ink", meta?.align === "right" && "flex-row-reverse")}
                        onClick={() =>
                          onSortChange({ field: sortKey, direction: active && sort?.direction === "asc" ? "desc" : "asc" })
                        }
                        aria-label={`Sort by ${meta?.label ?? sortKey}`}
                      >
                        <table.FlexRender header={header} />
                        {active ? (
                          sort?.direction === "asc" ? <ArrowUp className="size-3" /> : <ArrowDown className="size-3" />
                        ) : (
                          <ChevronsUpDown className="size-3 opacity-40" />
                        )}
                      </button>
                    ) : (
                      <table.FlexRender header={header} />
                    )}
                  </TableHead>
                );
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {loading && !data ? (
            Array.from({ length: skeletonRows }, (_, i) => (
              <TableRow key={i} className="hover:bg-transparent">
                {Array.from({ length: visibleCount }, (_, j) => (
                  <TableCell key={j}>
                    <Skeleton className={cn("h-3.5", j === 0 ? "w-40" : "w-20")} />
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : rows.length === 0 ? (
            <TableRow className="hover:bg-transparent">
              <TableCell colSpan={visibleCount} className="h-auto p-0 whitespace-normal">
                {empty}
              </TableCell>
            </TableRow>
          ) : (
            rows.map((row) => (
              <TableRow
                key={row.id}
                onClick={onRowClick ? () => onRowClick(row.original) : undefined}
                className={cn(onRowClick && "cursor-pointer", loading && "opacity-60")}
              >
                {row.getVisibleCells().map((cell) => {
                  const meta = cell.column.columnDef.meta;
                  return (
                    <TableCell key={cell.id} className={cn(meta?.align === "right" && "text-right", meta?.className)}>
                      <table.FlexRender cell={cell} />
                    </TableCell>
                  );
                })}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
      {footer}
    </div>
  );
}
