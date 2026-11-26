"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Upload } from "lucide-react";
import { toast } from "sonner";
import { cn } from "cn";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { errorMessage } from "@/lib/api/errors";
import { importBooks } from "@/lib/queries";
import { keys } from "@/lib/queries/keys";
import type { ImportReport } from "@/types/api";

const TEMPLATE =
  "isbn,title,authors,genre,language,publisher,year,pages,shelf,copies\n9780143039655,Malgudi Days,R. K. Narayan,Indian Literature,English,Penguin Books,2006,264,A-01,2\n";

export function ImportDialog() {
  const client = useQueryClient();
  const [open, setOpen] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [report, setReport] = useState<ImportReport | null>(null);
  const [busy, setBusy] = useState(false);

  async function run(dryRun: boolean) {
    if (!file) return;
    setBusy(true);
    try {
      const result = await importBooks(file, dryRun);
      setReport(result);
      if (!dryRun && result.created > 0) {
        void client.invalidateQueries({ queryKey: keys.books.all });
        void client.invalidateQueries({ queryKey: keys.dashboard.all });
        toast.success(`Imported ${result.created} ${result.created === 1 ? "book" : "books"}`);
      }
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  const templateHref = `data:text/csv;charset=utf-8,${encodeURIComponent(TEMPLATE)}`;

  return (
    <>
      <Button variant="secondary" onClick={() => setOpen(true)}>
        <Upload />
        Import
      </Button>
      <Dialog
        open={open}
        onOpenChange={(next) => {
          setOpen(next);
          if (!next) {
            setFile(null);
            setReport(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>Import books from CSV</DialogTitle>
            <DialogDescription>
              Columns: isbn, title, authors (separate with ;), genre, language, publisher, year, pages, shelf, copies.{" "}
              <a href={templateHref} download="folio-import-template.csv" className="text-brand underline underline-offset-2">
                Download a template
              </a>
              .
            </DialogDescription>
          </DialogHeader>
          <Input
            type="file"
            accept=".csv,text/csv"
            onChange={(e) => {
              setFile(e.target.files?.[0] ?? null);
              setReport(null);
            }}
          />
          {report ? (
            <div className="space-y-2">
              <p className="text-sm text-ink">
                {report.dryRun
                  ? `${report.rows - report.failed} of ${report.rows} rows are ready to import.`
                  : `${report.created} imported, ${report.failed} skipped.`}
              </p>
              <ul className="scrollbar-thin max-h-64 divide-y divide-border overflow-y-auto rounded-md border border-border text-sm">
                {report.results.map((r) => (
                  <li key={r.row} className="flex gap-3 px-3 py-2">
                    <span className="w-10 shrink-0 font-mono text-ink-muted">#{r.row}</span>
                    <span className="min-w-0 flex-1 truncate">{r.title || r.isbn || "—"}</span>
                    <span className={cn("shrink-0", r.status === "FAILED" ? "text-danger" : "text-success")}>
                      {r.status === "FAILED" ? r.message : r.status === "READY" ? "Ready" : "Imported"}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
          <DialogFooter>
            <Button variant="secondary" disabled={!file || busy} onClick={() => run(true)}>
              Check file
            </Button>
            <Button disabled={!file || busy} onClick={() => run(false)}>
              {busy ? "Working…" : "Import"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
