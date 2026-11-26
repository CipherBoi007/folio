"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Download, LayoutGrid, Plus, Rows3 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookCover } from "@/components/common/book-cover";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorState } from "@/components/common/error-state";
import { FilterSelect, MultiSelectFilter } from "@/components/common/filters";
import { PageHeader } from "@/components/common/page-header";
import { SearchInput } from "@/components/common/search-input";
import { AvailabilityBadge } from "@/components/common/status-badges";
import { StockMeter } from "@/components/common/stock-meter";
import { DataTable, formatSort, parseSort, type Column } from "@/components/data-table/data-table";
import { Pagination } from "@/components/data-table/pagination";
import { BookSheet } from "@/components/books/book-sheet";
import { ImportDialog } from "@/components/books/import-dialog";
import { useUrlState } from "@/hooks/use-url-state";
import { download } from "@/lib/api/client";
import { errorMessage } from "@/lib/api/errors";
import { useBooks, useGenres, useLanguages } from "@/lib/queries";
import type { BookSummary } from "@/types/api";

const PAGE_SIZE = 20;
const DEFAULT_SORT = { field: "title", direction: "asc" } as const;

const columns: Column<BookSummary>[] = [
  {
    id: "title",
    header: "Title",
    meta: { sortKey: "title", label: "Title", className: "w-[40%]" },
    cell: ({ row }) => {
      const b = row.original;
      return (
        <div className="flex items-center gap-3 py-1">
          <BookCover isbn={b.isbn13} title={b.title} author={b.authors[0]} size="xs" />
          <div className="min-w-0">
            <Link href={`/books/${b.id}`} className="block truncate font-medium text-ink hover:underline" onClick={(e) => e.stopPropagation()}>
              {b.title}
            </Link>
            <p className="truncate text-sm text-ink-muted">{b.authors.join(", ")}</p>
          </div>
        </div>
      );
    },
  },
  { id: "isbn", header: "ISBN", meta: { label: "ISBN", hideable: true }, cell: ({ row }) => <span className="font-mono text-sm text-ink-muted">{row.original.isbn13}</span> },
  { id: "genre", header: "Genre", meta: { label: "Genre", hideable: true }, cell: ({ row }) => <span className="text-ink-muted">{row.original.genre.name}</span> },
  { id: "language", header: "Language", meta: { label: "Language", hideable: true }, cell: ({ row }) => <span className="text-ink-muted">{row.original.language.name}</span> },
  {
    id: "year",
    header: "Year",
    meta: { sortKey: "year", label: "Year", hideable: true, align: "right" },
    cell: ({ row }) => <span className="font-mono text-ink-muted">{row.original.publishedYear ?? "—"}</span>,
  },
  { id: "shelf", header: "Shelf", meta: { label: "Shelf", hideable: true }, cell: ({ row }) => <span className="font-mono text-sm">{row.original.shelfLocation ?? "—"}</span> },
  {
    id: "status",
    header: "Status",
    meta: { label: "Status" },
    cell: ({ row }) => <AvailabilityBadge available={row.original.availableQuantity} total={row.original.totalQuantity} />,
  },
  {
    id: "stock",
    header: "On shelf",
    meta: { sortKey: "available", label: "On shelf", align: "right" },
    cell: ({ row }) => <StockMeter available={row.original.availableQuantity} total={row.original.totalQuantity} />,
  },
];

export function BooksView() {
  const url = useUrlState();
  const router = useRouter();
  const genres = useGenres();
  const languages = useLanguages();
  const [exporting, setExporting] = useState(false);

  const q = url.get("q") ?? "";
  const genreIds = url.getAll("genre");
  const availability = url.get("availability");
  const language = url.get("language");
  const view = url.get("view") === "grid" ? "grid" : "table";
  const page = url.getNumber("page", 0);
  const sort = parseSort(url.get("sort"), DEFAULT_SORT);
  const sheetOpen = url.get("new") === "1";

  const genreKey = genreIds.join(",");
  const sortParam = formatSort(sort);
  const query = useMemo(
    () => ({ q, genreId: genreKey ? genreKey.split(",") : [], availability, language, page, size: view === "grid" ? 24 : PAGE_SIZE, sort: sortParam }),
    [q, genreKey, availability, language, page, view, sortParam],
  );
  const books = useBooks(query);
  const filtered = Boolean(q || genreIds.length || availability || language);

  async function exportCsv() {
    setExporting(true);
    try {
      await download("/books/export.csv", { q, genreId: genreIds, availability, language }, "folio-books.csv");
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setExporting(false);
    }
  }

  const empty = filtered ? (
    <EmptyState
      compact
      title="No books match these filters."
      description="Try a shorter search or clear a filter."
      action={
        <Button variant="secondary" size="sm" onClick={() => url.set({ q: null, genre: null, availability: null, language: null })}>
          Clear filters
        </Button>
      }
    />
  ) : (
    <EmptyState compact title="The catalogue is empty." description="Add the first title to start lending." action={<Button onClick={() => url.set({ new: 1 })}>Add book</Button>} />
  );

  const toolbar = (
    <>
      <SearchInput value={q} onChange={(value) => url.set({ q: value })} placeholder="Search title, author or ISBN" className="w-full sm:w-72" />
      <MultiSelectFilter
        label="Genre"
        values={genreIds}
        onChange={(values) => url.set({ genre: values })}
        options={(genres.data ?? []).map((g) => ({ value: String(g.id), label: g.name, hint: String(g.titles) }))}
      />
      <FilterSelect
        ariaLabel="Availability"
        allLabel="Any availability"
        value={availability}
        onChange={(value) => url.set({ availability: value })}
        options={[
          { value: "IN_STOCK", label: "On the shelf" },
          { value: "LOW", label: "Low stock" },
          { value: "OUT", label: "Out of stock" },
        ]}
      />
      <FilterSelect
        ariaLabel="Language"
        allLabel="Any language"
        value={language}
        onChange={(value) => url.set({ language: value })}
        options={(languages.data ?? []).map((l) => ({ value: l.code, label: l.name }))}
      />
      <Tabs value={view} onValueChange={(value) => url.set({ view: value === "grid" ? "grid" : null })} className="ml-auto">
        <TabsList variant="segmented" aria-label="View">
          <TabsTrigger value="table" aria-label="Table view">
            <Rows3 />
          </TabsTrigger>
          <TabsTrigger value="grid" aria-label="Shelf view">
            <LayoutGrid />
          </TabsTrigger>
        </TabsList>
      </Tabs>
    </>
  );

  const footer = books.data ? (
    <Pagination page={books.data.page} size={books.data.size} total={books.data.totalElements} onPageChange={(p) => url.set({ page: p })} />
  ) : null;

  return (
    <>
      <PageHeader
        title="Books"
        description="The catalogue: every title, its copies and where they sit on the shelf."
        actions={
          <>
            <Button variant="secondary" onClick={exportCsv} disabled={exporting}>
              <Download />
              {exporting ? "Exporting…" : "Export CSV"}
            </Button>
            <ImportDialog />
            <Button onClick={() => url.set({ new: 1 })}>
              <Plus />
              Add book
            </Button>
          </>
        }
      />

      {books.isError ? (
        <div className="rounded-lg border border-border bg-surface">
          <ErrorState error={books.error} title="The catalogue didn't load." onRetry={() => books.refetch()} />
        </div>
      ) : view === "table" ? (
        <DataTable
          columns={columns}
          data={books.data?.content}
          loading={books.isFetching}
          sort={sort}
          onSortChange={(next) => url.set({ sort: formatSort(next) })}
          getRowId={(b) => String(b.id)}
          onRowClick={(b) => router.push(`/books/${b.id}`)}
          toolbar={toolbar}
          footer={footer}
          empty={empty}
          initialHidden={["language", "isbn"]}
        />
      ) : (
        <div className="overflow-hidden rounded-lg border border-border bg-surface">
          <div className="flex flex-wrap items-center gap-2 border-b border-border px-4 py-3">{toolbar}</div>
          {books.isPending ? (
            <ShelfSkeleton />
          ) : books.data?.content.length ? (
            <ul className="grid grid-cols-2 gap-x-5 gap-y-7 p-6 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
              {books.data.content.map((b) => (
                <li key={b.id}>
                  <Link href={`/books/${b.id}`} className="group block rounded-md focus-visible:outline-2 focus-visible:outline-offset-4">
                    <BookCover isbn={b.isbn13} title={b.title} author={b.authors[0]} size="fill" className="transition-transform duration-200 group-hover:-translate-y-0.5" />
                    <p className="mt-2.5 line-clamp-2 text-base leading-snug font-medium text-ink">{b.title}</p>
                    <p className="truncate text-sm text-ink-muted">{b.authors.join(", ")}</p>
                    <div className="mt-1.5">
                      <AvailabilityBadge available={b.availableQuantity} total={b.totalQuantity} />
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          ) : (
            empty
          )}
          {footer}
        </div>
      )}

      <BookSheet
        open={sheetOpen}
        onOpenChange={(open) => url.set({ new: open ? 1 : null, page })}
        onSaved={(book) => router.push(`/books/${book.id}`)}
      />
    </>
  );
}

function ShelfSkeleton() {
  return (
    <div className="grid grid-cols-2 gap-x-5 gap-y-7 p-6 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
      {Array.from({ length: 12 }, (_, i) => (
        <div key={i}>
          <Skeleton className="aspect-[2/3] w-full" />
          <Skeleton className="mt-3 h-3.5 w-3/4" />
          <Skeleton className="mt-1.5 h-3 w-1/2" />
        </div>
      ))}
    </div>
  );
}
