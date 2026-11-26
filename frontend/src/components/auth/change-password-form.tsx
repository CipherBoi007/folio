"use client";

import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/components/common/form-field";
import { errorMessage, isApiError } from "@/lib/api/errors";
import { getAuthState, setAuthState } from "@/lib/auth/session";
import { useChangePassword } from "@/lib/queries";

const strong = (v: string) => v.length >= 8 && /[A-Za-z]/.test(v) && /\d/.test(v);

export function ChangePasswordForm({ onDone }: { onDone?: () => void }) {
  const change = useChangePassword();
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirm, setConfirm] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  function submit(e: FormEvent) {
    e.preventDefault();
    const found: Record<string, string> = {};
    if (!current) found.currentPassword = "Enter your current password.";
    if (!strong(next)) found.newPassword = "Use at least 8 characters with letters and numbers.";
    if (next !== confirm) found.confirm = "The passwords don't match.";
    setErrors(found);
    if (Object.keys(found).length) return;
    change.mutate(
      { currentPassword: current, newPassword: next },
      {
        onSuccess: () => {
          const state = getAuthState();
          if (state.status === "authenticated") setAuthState({ ...state, user: { ...state.user, mustChangePassword: false } });
          toast.success("Password changed. Other devices were signed out.");
          setCurrent("");
          setNext("");
          setConfirm("");
          onDone?.();
        },
        onError: (error) => {
          if (isApiError(error) && error.fieldErrors.length) {
            setErrors(Object.fromEntries(error.fieldErrors.map((f) => [f.field, f.message])));
          } else toast.error(errorMessage(error));
        },
      },
    );
  }

  return (
    <form noValidate onSubmit={submit} className="space-y-4">
      <FormField id="currentPassword" label="Current password" error={errors.currentPassword}>
        <Input id="currentPassword" type="password" autoComplete="current-password" value={current} onChange={(e) => setCurrent(e.target.value)} />
      </FormField>
      <FormField id="newPassword" label="New password" error={errors.newPassword} hint="At least 8 characters, with letters and numbers">
        <Input id="newPassword" type="password" autoComplete="new-password" value={next} onChange={(e) => setNext(e.target.value)} />
      </FormField>
      <FormField id="confirm" label="Confirm new password" error={errors.confirm}>
        <Input id="confirm" type="password" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
      </FormField>
      <Button type="submit" disabled={change.isPending}>
        {change.isPending ? "Saving…" : "Change password"}
      </Button>
    </form>
  );
}
