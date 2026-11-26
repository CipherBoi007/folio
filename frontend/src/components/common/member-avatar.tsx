import { cn } from "cn";
import { initials } from "@/lib/format";

const TINTS = [
  "bg-brand-soft text-brand",
  "bg-info-soft text-info",
  "bg-warning-soft text-warning",
  "bg-surface-muted text-ink-muted",
  "bg-success-soft text-success",
  "bg-danger-soft text-danger",
];

function tint(name: string) {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) >>> 0;
  return TINTS[h % TINTS.length];
}

export function MemberAvatar({ name, size = "md", className }: { name: string; size?: "sm" | "md" | "lg"; className?: string }) {
  return (
    <span
      aria-hidden
      className={cn(
        "inline-flex shrink-0 items-center justify-center rounded-full font-semibold",
        size === "sm" && "size-6 text-[10px]",
        size === "md" && "size-8 text-xs",
        size === "lg" && "size-14 text-lg",
        tint(name),
        className,
      )}
    >
      {initials(name)}
    </span>
  );
}
