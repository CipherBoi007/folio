"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { ConfirmDialog } from "@/components/common/confirm-dialog";
import { formatDate, formatMoney } from "@/lib/format";
import { useRenewLoan, useReturnLoan } from "@/lib/queries";
import { toast } from "sonner";
import type { Loan } from "@/types/api";

/** Renew / Return buttons for an active loan, with the return confirmation showing any fine up front. */
export function LoanActions({ loan, size = "xs" }: { loan: Loan; size?: "xs" | "sm" }) {
  const renew = useRenewLoan();
  const ret = useReturnLoan();
  const [confirming, setConfirming] = useState(false);

  if (loan.status !== "ACTIVE") return null;

  const renewBlocked = loan.overdue ? "Overdue loans can't be renewed." : loan.renewalCount > 0 ? "Already renewed once." : null;

  return (
    <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
      {renewBlocked ? (
        <Tooltip>
          <TooltipTrigger asChild>
            <span tabIndex={0}>
              <Button variant="ghost" size={size} disabled>
                Renew
              </Button>
            </span>
          </TooltipTrigger>
          <TooltipContent>{renewBlocked}</TooltipContent>
        </Tooltip>
      ) : (
        <Button variant="ghost" size={size} disabled={renew.isPending} onClick={() => renew.mutate(loan.id)}>
          Renew
        </Button>
      )}
      <Button variant="secondary" size={size} onClick={() => setConfirming(true)}>
        Return
      </Button>
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title="Record return"
        confirmLabel={loan.projectedFine ? `Return and fine ${formatMoney(loan.projectedFine)}` : "Record return"}
        pending={ret.isPending}
        description={
          <>
            &ldquo;{loan.book.title}&rdquo; from {loan.member.fullName}, due {formatDate(loan.dueDate)}.
            {loan.projectedFine ? (
              <span className="mt-2 block text-danger">
                {loan.overdueDays} {loan.overdueDays === 1 ? "day" : "days"} late. A fine of {formatMoney(loan.projectedFine)} will be added to the
                member&apos;s account.
              </span>
            ) : (
              <span className="mt-2 block">Returned on time. No fine.</span>
            )}
          </>
        }
        onConfirm={() =>
          ret.mutate(loan.id, {
            onSuccess: (result) => {
              setConfirming(false);
              toast.success(result.fine ? `Returned. Fine of ${formatMoney(result.fine.amount)} recorded.` : "Return recorded");
            },
          })
        }
      />
    </div>
  );
}
