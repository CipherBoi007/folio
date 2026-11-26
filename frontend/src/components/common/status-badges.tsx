import { cn } from "cn";
import { Badge } from "@/components/ui/badge";
import { stockLevel } from "@/components/common/stock-meter";
import { formatMoneyShort } from "@/lib/format";
import type { FineStatus, Loan, MemberStatus, TransactionType } from "@/types/api";

export function AvailabilityBadge({ available, total }: { available: number; total: number }) {
  const level = stockLevel(available, total);
  if (level === "out") return <Badge tone="danger" dot>Out of stock</Badge>;
  if (level === "low") return <Badge tone="warning" dot>Low stock</Badge>;
  return <Badge tone="success" dot>Available</Badge>;
}

export function LoanStatusBadge({ loan }: { loan: Pick<Loan, "status" | "overdue" | "overdueDays" | "dueToday" | "renewalCount"> }) {
  if (loan.status === "RETURNED") return <Badge tone="neutral">Returned</Badge>;
  if (loan.overdue) return <Badge tone="danger" dot>Overdue · {loan.overdueDays}d</Badge>;
  if (loan.dueToday) return <Badge tone="warning" dot>Due today</Badge>;
  if (loan.renewalCount > 0) return <Badge tone="info" dot>Renewed</Badge>;
  return <Badge tone="brand" dot>Issued</Badge>;
}

const MEMBER_TONES = { ACTIVE: "success", INACTIVE: "neutral", SUSPENDED: "danger" } as const;
const MEMBER_LABELS = { ACTIVE: "Active", INACTIVE: "Inactive", SUSPENDED: "Suspended" } as const;

export function MemberStatusBadge({ status }: { status: MemberStatus }) {
  return (
    <Badge tone={MEMBER_TONES[status]} dot>
      {MEMBER_LABELS[status]}
    </Badge>
  );
}

export function FineStatusBadge({ status, amount }: { status: FineStatus; amount?: number }) {
  if (status === "UNPAID") return <Badge tone="danger">Unpaid{amount !== undefined ? ` ${formatMoneyShort(amount)}` : ""}</Badge>;
  if (status === "PAID") return <Badge tone="success">Paid</Badge>;
  return <Badge tone="neutral">Waived</Badge>;
}

export const TRANSACTION_LABELS: Record<TransactionType, string> = {
  INITIAL_STOCK: "Initial stock",
  STOCK_IN: "Stock in",
  STOCK_OUT: "Stock out",
  ISSUE: "Issue",
  RETURN: "Return",
};
const TYPE_TONE = { INITIAL_STOCK: "brand", STOCK_IN: "success", STOCK_OUT: "danger", ISSUE: "info", RETURN: "neutral" } as const;

export function TransactionTypeBadge({ type }: { type: TransactionType }) {
  return <Badge tone={TYPE_TONE[type]}>{TRANSACTION_LABELS[type]}</Badge>;
}

export function SignedQuantity({ value }: { value: number }) {
  return (
    <span className={cn("font-mono tabular-nums", value > 0 ? "text-success" : "text-danger")}>
      {value > 0 ? `+${value}` : `−${Math.abs(value)}`}
    </span>
  );
}

export function LoanSlots({ used, max }: { used: number; max: number }) {
  return (
    <span className="inline-flex items-center gap-1.5" aria-label={`${used} of ${max} loans`}>
      <span className="inline-flex gap-0.5">
        {Array.from({ length: max }, (_, i) => (
          <span key={i} className={cn("size-1.5 rounded-full", i < used ? (used >= max ? "bg-warning" : "bg-brand") : "bg-border")} />
        ))}
      </span>
      <span className="font-mono text-sm text-ink-muted tabular-nums">
        {used}/{max}
      </span>
    </span>
  );
}
