"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { AdminOnly } from "@/components/common/admin-only";
import { ConfirmDialog } from "@/components/common/confirm-dialog";
import { errorMessage } from "@/lib/api/errors";
import { formatMoney } from "@/lib/format";
import { usePayFine, useWaiveFine } from "@/lib/queries";
import { toast } from "sonner";
import type { Fine } from "@/types/api";

export function FineActions({ fine }: { fine: Fine }) {
  const pay = usePayFine();
  const waive = useWaiveFine();
  const [dialog, setDialog] = useState<"pay" | "waive" | null>(null);

  if (fine.status !== "UNPAID") return null;

  return (
    <div className="flex justify-end gap-1" onClick={(e) => e.stopPropagation()}>
      <AdminOnly action="waive fines">
        <Button variant="ghost" size="xs" onClick={() => setDialog("waive")}>
          Waive
        </Button>
      </AdminOnly>
      <Button variant="secondary" size="xs" onClick={() => setDialog("pay")}>
        Record payment
      </Button>
      <ConfirmDialog
        open={dialog === "pay"}
        onOpenChange={(open) => setDialog(open ? "pay" : null)}
        title={`Collect ${formatMoney(fine.amount)}`}
        description={`${fine.member.fullName} returned "${fine.book.title}" ${fine.overdueDays} ${fine.overdueDays === 1 ? "day" : "days"} late at ${formatMoney(fine.dailyRate)} a day.`}
        confirmLabel="Record payment"
        pending={pay.isPending}
        onConfirm={() => pay.mutate({ id: fine.id }, { onSuccess: () => setDialog(null) })}
      />
      <ConfirmDialog
        open={dialog === "waive"}
        onOpenChange={(open) => setDialog(open ? "waive" : null)}
        title={`Waive ${formatMoney(fine.amount)}?`}
        description={`The fine for "${fine.book.title}" will be cleared from ${fine.member.fullName}'s account. The reason is kept in the activity log.`}
        reasonLabel="Reason"
        confirmLabel="Waive fine"
        pending={waive.isPending}
        onConfirm={(reason) =>
          waive.mutate({ id: fine.id, reason }, { onSuccess: () => setDialog(null), onError: (e) => toast.error(errorMessage(e)) })
        }
      />
    </div>
  );
}
