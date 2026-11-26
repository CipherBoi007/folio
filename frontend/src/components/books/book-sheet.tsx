"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { CircleAlert, CircleCheck, LoaderCircle } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { BookCover } from "@/components/common/book-cover";
import { FormField, applyServerErrors } from "@/components/common/form-field";
import { errorMessage, isApiError } from "@/lib/api/errors";
import { normalizeIsbn } from "@/lib/isbn";
import { lookupIsbn, useGenres, useLanguages, useSaveBook } from "@/lib/queries";
import type { BookDetail, IsbnLookup } from "@/types/api";

const currentYear = new Date().getFullYear();

const schema = z.object({
  isbn: z.string().trim().min(1, "Enter the ISBN.").refine((v) => normalizeIsbn(v) !== null, "That ISBN is not valid. Check the digits."),
  title: z.string().trim().min(1, "Enter the title.").max(300),
  subtitle: z.string().trim().max(300),
  authors: z.string().trim().min(1, "Add at least one author.").max(1000),
  genre: z.string().trim().min(1, "Choose or type a genre.").max(60),
  publisher: z.string().trim().max(200),
  languageCode: z.string().min(1, "Choose a language."),
  publishedYear: z.string().trim().refine((v) => v === "" || (/^\d{4}$/.test(v) && Number(v) <= currentYear + 1), "Enter a four-digit year, not in the future."),
  pages: z.string().trim().refine((v) => v === "" || (/^\d+$/.test(v) && Number(v) > 0), "Pages must be a positive number."),
  shelfLocation: z.string().trim().max(20).refine((v) => v === "" || /^[A-Za-z0-9][A-Za-z0-9 ./-]*$/.test(v), "Use letters, numbers and dashes, e.g. A-03."),
  description: z.string().trim().max(5000),
  coverUrl: z.string().trim(),
  initialQuantity: z.string().trim().refine((v) => /^\d+$/.test(v) && Number(v) <= 1000, "Enter 0 to 1000 copies."),
});

type Values = z.infer<typeof schema>;
const FIELDS = ["isbn", "title", "subtitle", "authors", "genre", "publisher", "languageCode", "publishedYear", "pages", "shelfLocation", "description", "coverUrl", "initialQuantity"] as const;

function toValues(book?: BookDetail): Values {
  return {
    isbn: book?.isbn13 ?? "",
    title: book?.title ?? "",
    subtitle: book?.subtitle ?? "",
    authors: book?.authors.join(", ") ?? "",
    genre: book?.genre.name ?? "",
    publisher: book?.publisher ?? "",
    languageCode: book?.language.code ?? "en",
    publishedYear: book?.publishedYear?.toString() ?? "",
    pages: book?.pages?.toString() ?? "",
    shelfLocation: book?.shelfLocation ?? "",
    description: book?.description ?? "",
    coverUrl: book?.coverUrl ?? "",
    initialQuantity: "1",
  };
}

export function BookSheet({
  open,
  onOpenChange,
  book,
  onSaved,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  book?: BookDetail;
  onSaved?: (book: BookDetail) => void;
}) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="sm:max-w-[560px]">
        {open ? <BookSheetBody key={book?.id ?? "new"} book={book} onOpenChange={onOpenChange} onSaved={onSaved} /> : null}
      </SheetContent>
    </Sheet>
  );
}

function BookSheetBody({
  onOpenChange,
  book,
  onSaved,
}: {
  onOpenChange: (open: boolean) => void;
  book?: BookDetail;
  onSaved?: (book: BookDetail) => void;
}) {
  const editing = Boolean(book);
  const genres = useGenres();
  const languages = useLanguages();
  const save = useSaveBook(book?.id);
  const form = useForm<Values>({ resolver: zodResolver(schema), defaultValues: toValues(book) });
  const { register, handleSubmit, formState, control, setValue, getValues, setError } = form;
  const [isbnValue, languageCode, title, authors] = useWatch({ control, name: ["isbn", "languageCode", "title", "authors"] });
  const [lookup, setLookup] = useState<{ state: "idle" | "loading" | "done" | "error"; result?: IsbnLookup }>({ state: "idle" });
  const lastLooked = useRef<string | null>(book?.isbn13 ?? null);

  const normalized = useMemo(() => normalizeIsbn(isbnValue ?? ""), [isbnValue]);

  useEffect(() => {
    if (!normalized || normalized === lastLooked.current) return;
    const controller = new AbortController();
    const timer = setTimeout(async () => {
      lastLooked.current = normalized;
      setLookup({ state: "loading" });
      try {
        const result = await lookupIsbn(normalized, controller.signal);
        setLookup({ state: "done", result });
        if (result.found && !editing) {
          const fill = (field: keyof Values, value: string | number | null | undefined) => {
            if (value !== null && value !== undefined && String(value).trim() && !String(getValues(field)).trim()) {
              setValue(field, String(value), { shouldDirty: true });
            }
          };
          fill("title", result.title);
          fill("subtitle", result.subtitle);
          fill("authors", result.authors.join(", "));
          fill("publisher", result.publisher);
          fill("publishedYear", result.publishedYear);
          fill("pages", result.pages);
          fill("coverUrl", result.coverUrl);
        }
      } catch (error) {
        if (!controller.signal.aborted) setLookup({ state: "error", result: undefined });
        if (!(error instanceof DOMException)) lastLooked.current = null;
      }
    }, 400);
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [normalized, editing, getValues, setValue]);

  async function onSubmit(values: Values) {
    try {
      const saved = await save.mutateAsync({
        isbn: values.isbn,
        title: values.title,
        subtitle: values.subtitle || null,
        authors: values.authors.split(",").map((a) => a.trim()).filter(Boolean),
        genre: values.genre,
        publisher: values.publisher || null,
        languageCode: values.languageCode,
        publishedYear: values.publishedYear ? Number(values.publishedYear) : null,
        pages: values.pages ? Number(values.pages) : null,
        shelfLocation: values.shelfLocation || null,
        description: values.description || null,
        coverUrl: values.coverUrl || null,
        initialQuantity: editing ? undefined : Number(values.initialQuantity),
        version: book?.version,
      });
      toast.success(editing ? "Book updated" : `"${saved.title}" added to the catalogue`);
      onOpenChange(false);
      onSaved?.(saved);
    } catch (error) {
      if (isApiError(error)) {
        if (error.code === "DUPLICATE_ISBN" || error.code === "INVALID_ISBN") {
          setError("isbn", { type: "server", message: error.message });
          return;
        }
        if (applyServerErrors(error.fieldErrors, setError, FIELDS)) return;
      }
      toast.error(errorMessage(error));
    }
  }

  const errors = formState.errors;
  const result = lookup.result;

  return (
    <>
        <SheetHeader>
          <SheetTitle>{editing ? "Edit book" : "Add book"}</SheetTitle>
          <SheetDescription>
            {editing ? "Update the catalogue record. Copies are changed from the stock card." : "Start with the ISBN; known books fill themselves in."}
          </SheetDescription>
        </SheetHeader>

        <form id="book-form" noValidate onSubmit={handleSubmit(onSubmit)} className="scrollbar-thin flex-1 space-y-5 overflow-y-auto px-6 py-5">
          <div className="flex gap-4">
            <div className="flex-1 space-y-2">
              <FormField id="isbn" label="ISBN" error={errors.isbn?.message} hint="ISBN-10 or ISBN-13, with or without dashes">
                <Input id="isbn" autoFocus={!editing} inputMode="numeric" className="font-mono" aria-invalid={!!errors.isbn} {...register("isbn")} />
              </FormField>
              <LookupStatus state={lookup.state} result={result} editing={editing} />
            </div>
            <BookCover isbn={normalized} title={title || "Untitled"} author={authors?.split(",")[0]} size="md" key={normalized ?? "none"} />
          </div>

          <FormField id="title" label="Title" error={errors.title?.message}>
            <Input id="title" aria-invalid={!!errors.title} {...register("title")} />
          </FormField>
          <FormField id="subtitle" label="Subtitle" optional error={errors.subtitle?.message}>
            <Input id="subtitle" {...register("subtitle")} />
          </FormField>
          <FormField id="authors" label="Authors" error={errors.authors?.message} hint="Separate multiple authors with commas">
            <Input id="authors" aria-invalid={!!errors.authors} {...register("authors")} />
          </FormField>

          <div className="grid grid-cols-2 gap-4">
            <FormField id="genre" label="Genre" error={errors.genre?.message}>
              <Input id="genre" list="genre-options" aria-invalid={!!errors.genre} {...register("genre")} />
              <datalist id="genre-options">
                {genres.data?.map((g) => <option key={g.id} value={g.name} />)}
              </datalist>
            </FormField>
            <FormField id="languageCode" label="Language" error={errors.languageCode?.message}>
              <Select value={languageCode} onValueChange={(v) => setValue("languageCode", v, { shouldDirty: true })}>
                <SelectTrigger id="languageCode" className="w-full">
                  <SelectValue placeholder="Choose" />
                </SelectTrigger>
                <SelectContent>
                  {languages.data?.map((l) => (
                    <SelectItem key={l.code} value={l.code}>
                      {l.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </FormField>
          </div>

          <FormField id="publisher" label="Publisher" optional error={errors.publisher?.message}>
            <Input id="publisher" {...register("publisher")} />
          </FormField>

          <div className="grid grid-cols-3 gap-4">
            <FormField id="publishedYear" label="Year" optional error={errors.publishedYear?.message}>
              <Input id="publishedYear" inputMode="numeric" className="font-mono" {...register("publishedYear")} />
            </FormField>
            <FormField id="pages" label="Pages" optional error={errors.pages?.message}>
              <Input id="pages" inputMode="numeric" className="font-mono" {...register("pages")} />
            </FormField>
            <FormField id="shelfLocation" label="Shelf" optional error={errors.shelfLocation?.message}>
              <Input id="shelfLocation" placeholder="A-03" className="font-mono uppercase" {...register("shelfLocation")} />
            </FormField>
          </div>

          <FormField id="description" label="Description" optional error={errors.description?.message}>
            <Textarea id="description" rows={3} {...register("description")} />
          </FormField>

          {!editing ? (
            <FormField id="initialQuantity" label="Copies" error={errors.initialQuantity?.message} hint="Recorded as initial stock in the inventory ledger">
              <Input id="initialQuantity" inputMode="numeric" className="w-28 font-mono" {...register("initialQuantity")} />
            </FormField>
          ) : null}
        </form>

        <SheetFooter>
          <Button variant="secondary" onClick={() => onOpenChange(false)} disabled={save.isPending}>
            Cancel
          </Button>
          <Button type="submit" form="book-form" disabled={save.isPending}>
            {save.isPending ? "Saving…" : editing ? "Save changes" : "Add book"}
          </Button>
        </SheetFooter>
    </>
  );
}

function LookupStatus({ state, result, editing }: { state: string; result?: IsbnLookup; editing: boolean }) {
  if (state === "loading") {
    return (
      <p className="flex items-center gap-1.5 text-sm text-ink-muted">
        <LoaderCircle className="size-3.5 animate-spin" /> Looking up Open Library…
      </p>
    );
  }
  if (state === "error") return <p className="text-sm text-ink-muted">Open Library didn&apos;t respond. Fill in the details by hand.</p>;
  if (state !== "done" || !result) return null;
  if (result.existingBookId && !editing) {
    return (
      <p className="flex items-start gap-1.5 text-sm text-warning">
        <CircleAlert className="mt-0.5 size-3.5 shrink-0" />
        <span>
          Already in the catalogue.{" "}
          <Link href={`/books/${result.existingBookId}`} className="underline underline-offset-2">
            Open the record
          </Link>{" "}
          to add copies instead.
        </span>
      </p>
    );
  }
  if (result.found) {
    return (
      <p className="flex items-center gap-1.5 text-sm text-success">
        <CircleCheck className="size-3.5" /> Found on Open Library{editing ? "" : "; details filled in"}.
      </p>
    );
  }
  return <p className="text-sm text-ink-muted">Not on Open Library. Fill in the details by hand.</p>;
}
