import type { ReactNode } from "react";
import { cn } from "cn";
import { Label } from "@/components/ui/label";

/** Label above, control, then either the inline error (danger) or the muted helper text. */
export function FormField({
  id,
  label,
  error,
  hint,
  optional,
  className,
  children,
}: {
  id: string;
  label: string;
  error?: string;
  hint?: ReactNode;
  optional?: boolean;
  className?: string;
  children: ReactNode;
}) {
  return (
    <div className={cn("space-y-1.5", className)}>
      <Label htmlFor={id} className="gap-1.5">
        {label}
        {optional ? <span className="font-normal text-ink-muted">optional</span> : null}
      </Label>
      {children}
      {error ? (
        <p role="alert" id={`${id}-error`} className="text-sm text-danger">
          {error}
        </p>
      ) : hint ? (
        <p className="text-sm text-ink-muted">{hint}</p>
      ) : null}
    </div>
  );
}

/** Applies server field errors (fieldErrors[]) to a react-hook-form instance. */
export function applyServerErrors<T extends string>(
  fieldErrors: { field: string; message: string }[],
  setError: (name: T, error: { type: string; message: string }) => void,
  fields: readonly T[],
): boolean {
  let applied = false;
  for (const error of fieldErrors) {
    if ((fields as readonly string[]).includes(error.field)) {
      setError(error.field as T, { type: "server", message: error.message });
      applied = true;
    }
  }
  return applied;
}
