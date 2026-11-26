import { differenceInCalendarDays, format, formatDistanceToNowStrict, isValid, parseISO } from "date-fns";

/** Formatting rules from CLAUDE.md §9: dates as "26 Nov 2026", money as "₹1,250.00" (en-IN). */

type DateInput = string | Date | null | undefined;

function toDate(value: DateInput): Date | null {
  if (!value) return null;
  const date = typeof value === "string" ? parseISO(value) : value;
  return isValid(date) ? date : null;
}

/** 26 Nov 2026 */
export function formatDate(value: DateInput): string {
  const date = toDate(value);
  return date ? format(date, "d MMM yyyy") : "—";
}

/** 26 Nov 2026, 14:05 */
export function formatDateTime(value: DateInput): string {
  const date = toDate(value);
  return date ? format(date, "d MMM yyyy, HH:mm") : "—";
}

/** Thu, 10 Dec 2026 */
export function formatDateWithWeekday(value: DateInput): string {
  const date = toDate(value);
  return date ? format(date, "EEE, d MMM yyyy") : "—";
}

/** Thursday, 26 November */
export function formatGreetingDate(value: Date = new Date()): string {
  return format(value, "EEEE, d MMMM");
}

/** "3 minutes ago" */
export function formatRelative(value: DateInput): string {
  const date = toDate(value);
  return date ? formatDistanceToNowStrict(date, { addSuffix: true }) : "—";
}

/** Whole calendar days from `from` until `to` (negative when `to` is in the past). */
export function daysBetween(from: DateInput, to: DateInput): number | null {
  const a = toDate(from);
  const b = toDate(to);
  return a && b ? differenceInCalendarDays(b, a) : null;
}

const moneyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const compactMoneyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

/** ₹1,250.00 */
export function formatMoney(value: number | string | null | undefined): string {
  const amount = typeof value === "string" ? Number(value) : value;
  return amount === null || amount === undefined || Number.isNaN(amount) ? "—" : moneyFormatter.format(amount);
}

/** ₹1,250 — for badges and compact summaries */
export function formatMoneyShort(value: number | string | null | undefined): string {
  const amount = typeof value === "string" ? Number(value) : value;
  return amount === null || amount === undefined || Number.isNaN(amount) ? "—" : compactMoneyFormatter.format(amount);
}

const numberFormatter = new Intl.NumberFormat("en-IN");

/** 1,24,500 (Indian digit grouping) */
export function formatNumber(value: number | null | undefined): string {
  return value === null || value === undefined ? "—" : numberFormatter.format(value);
}

/** "Priya Raman" → "PR" */
export function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  const first = parts[0][0] ?? "";
  const last = parts.length > 1 ? (parts[parts.length - 1][0] ?? "") : "";
  return (first + last).toUpperCase();
}

/** Sentence-cases an enum constant: "STOCK_IN" → "Stock in" */
export function humanize(constant: string): string {
  const lower = constant.toLowerCase().replace(/_/g, " ");
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}
