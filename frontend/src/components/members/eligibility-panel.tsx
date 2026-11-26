import { CircleAlert, CircleCheck } from "lucide-react";
import { cn } from "cn";
import { Skeleton } from "@/components/ui/skeleton";
import { LoanSlots } from "@/components/common/status-badges";
import { formatMoney } from "@/lib/format";
import type { Eligibility } from "@/types/api";

export function EligibilityPanel({ eligibility, loading, className }: { eligibility?: Eligibility; loading?: boolean; className?: string }) {
  if (loading || !eligibility) {
    return (
      <div className={cn("rounded-lg border border-border bg-surface p-4", className)}>
        <Skeleton className="h-4 w-40" />
        <Skeleton className="mt-3 h-3 w-56" />
      </div>
    );
  }
  const ok = eligibility.canBorrow;
  return (
    <div className={cn("rounded-lg border p-4", ok ? "border-success/30 bg-success-soft/50" : "border-danger/30 bg-danger-soft/50", className)}>
      <div className="flex items-center justify-between gap-3">
        <p className={cn("flex items-center gap-2 text-base font-medium", ok ? "text-success" : "text-danger")}>
          {ok ? <CircleCheck className="size-4" /> : <CircleAlert className="size-4" />}
          {ok
            ? `Can borrow · ${eligibility.remainingSlots} ${eligibility.remainingSlots === 1 ? "slot" : "slots"} left`
            : "Can't borrow right now"}
        </p>
        <LoanSlots used={eligibility.activeLoans} max={eligibility.maxLoans} />
      </div>
      {ok ? (
        <p className="mt-1.5 text-sm text-ink-muted">
          {Number(eligibility.unpaidFines) > 0
            ? `${formatMoney(eligibility.unpaidFines)} in unpaid fines, under the ${formatMoney(eligibility.fineLimit)} limit.`
            : "No overdue books or unpaid fines."}
        </p>
      ) : (
        <ul className="mt-2 space-y-1">
          {eligibility.reasons.map((reason) => (
            <li key={reason.code} className="text-sm text-ink">
              {reason.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
