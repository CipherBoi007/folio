"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { FormField } from "@/components/common/form-field";
import { errorMessage } from "@/lib/api/errors";
import { useStockChange } from "@/lib/queries";
import type { BookDetail, StockOutReason } from "@/types/api";

const IN_REASONS = ["Purchase", "Donation", "Transfer in", "Found"];
const OUT_REASONS: { value: StockOutReason; label: string }[] = [
  { value: "DAMAGED", label: "Damaged" },
  { value: "LOST", label: "Lost" },
  { value: "WITHDRAWN", label: "Withdrawn" },
];

export function StockDialog({ book, direction, onOpenChange }: { book: BookDetail; direction: "in" | "out" | null; onOpenChange: (open: boolean) => void }) {
  return (
    <Dialog open={direction !== null} onOpenChange={onOpenChange}>
      <DialogContent>{direction ? <StockForm key={direction} book={book} direction={direction} onDone={() => onOpenChange(false)} /> : null}</DialogContent>
    </Dialog>
  );
}

function StockForm({ book, direction, onDone }: { book: BookDetail; direction: "in" | "out"; onDone: () => void }) {
  const mutation = useStockChange(book.id);
  const [quantity, setQuantity] = useState("1");
  const [reason, setReason] = useState<string>(direction === "in" ? "Purchase" : "DAMAGED");
  const [note, setNote] = useState("");
  const [error, setError] = useState<string | null>(null);

  const qty = Number(quantity);
  const validQty = /^\d+$/.test(quantity) && qty >= 1 && qty <= 1000;
  const exceeds = direction === "out" && validQty && qty > book.availableQuantity;
  const totalAfter = direction === "in" ? book.totalQuantity + (validQty ? qty : 0) : book.totalQuantity - (validQty ? qty : 0);
  const availableAfter = direction === "in" ? book.availableQuantity + (validQty ? qty : 0) : book.availableQuantity - (validQty ? qty : 0);

  function submit() {
    if (!validQty || exceeds || !reason.trim()) return;
    setError(null);
    const input =
      direction === "in"
        ? { direction: "in" as const, quantity: qty, reason: reason.trim(), note }
        : { direction: "out" as const, quantity: qty, reason: reason as StockOutReason, note };
    mutation.mutate(input, {
      onSuccess: () => {
        toast.success(direction === "in" ? `Added ${qty} ${qty === 1 ? "copy" : "copies"}` : `Removed ${qty} ${qty === 1 ? "copy" : "copies"}`);
        onDone();
      },
      onError: (e) => setError(errorMessage(e)),
    });
  }

  return (
    <>
        <DialogHeader>
          <DialogTitle>{direction === "in" ? "Add copies" : "Remove copies"}</DialogTitle>
          <DialogDescription>
            {direction === "in"
              ? "New copies go straight onto the shelf."
              : `Only copies on the shelf can be removed. ${book.totalQuantity - book.availableQuantity} on loan.`}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="grid grid-cols-[120px_1fr] gap-4">
            <FormField id="stock-qty" label="Copies" error={!validQty ? "1 to 1000" : exceeds ? `Max ${book.availableQuantity}` : undefined}>
              <Input id="stock-qty" inputMode="numeric" className="font-mono" value={quantity} onChange={(e) => setQuantity(e.target.value)} autoFocus />
            </FormField>
            <FormField id="stock-reason" label="Reason">
              {direction === "in" ? (
                <>
                  <Input id="stock-reason" list="in-reasons" value={reason} onChange={(e) => setReason(e.target.value)} maxLength={40} />
                  <datalist id="in-reasons">
                    {IN_REASONS.map((r) => (
                      <option key={r} value={r} />
                    ))}
                  </datalist>
                </>
              ) : (
                <Select value={reason} onValueChange={setReason}>
                  <SelectTrigger id="stock-reason" className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {OUT_REASONS.map((r) => (
                      <SelectItem key={r.value} value={r.value}>
                        {r.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </FormField>
          </div>
          <FormField id="stock-note" label="Note" optional>
            <Textarea id="stock-note" rows={2} maxLength={500} value={note} onChange={(e) => setNote(e.target.value)} />
          </FormField>
          <div className="flex items-center justify-between rounded-md bg-surface-muted px-3 py-2 text-sm">
            <span className="text-ink-muted">After this change</span>
            <span className="font-mono tabular-nums">
              {Math.max(availableAfter, 0)} on shelf · {Math.max(totalAfter, 0)} total
            </span>
          </div>
          {error ? (
            <p role="alert" className="text-sm text-danger">
              {error}
            </p>
          ) : null}
        </div>

        <DialogFooter>
          <Button variant="secondary" onClick={onDone} disabled={mutation.isPending}>
            Cancel
          </Button>
          <Button variant={direction === "out" ? "danger" : "default"} onClick={submit} disabled={!validQty || exceeds || !reason.trim() || mutation.isPending}>
            {mutation.isPending ? "Saving…" : direction === "in" ? "Add copies" : "Remove copies"}
          </Button>
        </DialogFooter>
    </>
  );
}
