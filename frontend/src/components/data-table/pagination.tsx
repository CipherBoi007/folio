"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatNumber } from "@/lib/format";

/** "1–20 of 143" with previous/next. Pages are zero-based to match the API. */
export function Pagination({
  page,
  size,
  total,
  onPageChange,
}: {
  page: number;
  size: number;
  total: number;
  onPageChange: (page: number) => void;
}) {
  const totalPages = Math.max(1, Math.ceil(total / size));
  const from = total === 0 ? 0 : page * size + 1;
  const to = Math.min(total, (page + 1) * size);

  return (
    <div className="flex items-center justify-between gap-4 border-t border-border px-4 py-2.5">
      <p className="text-sm text-ink-muted tabular-nums">
        {total === 0 ? "No results" : `${formatNumber(from)}–${formatNumber(to)} of ${formatNumber(total)}`}
      </p>
      <div className="flex items-center gap-1">
        <Button variant="ghost" size="icon-sm" disabled={page <= 0} onClick={() => onPageChange(page - 1)} aria-label="Previous page">
          <ChevronLeft />
        </Button>
        <span className="px-1 text-sm text-ink-muted tabular-nums">
          {page + 1} / {totalPages}
        </span>
        <Button variant="ghost" size="icon-sm" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)} aria-label="Next page">
          <ChevronRight />
        </Button>
      </div>
    </div>
  );
}
