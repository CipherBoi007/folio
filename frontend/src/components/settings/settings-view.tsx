"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Monitor } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { FormField } from "@/components/common/form-field";
import { PageHeader } from "@/components/common/page-header";
import { SectionCard } from "@/components/common/section-card";
import { ChangePasswordForm } from "@/components/auth/change-password-form";
import { errorMessage } from "@/lib/api/errors";
import { useIsAdmin } from "@/lib/auth/session";
import { formatDateTime, formatRelative } from "@/lib/format";
import { useRevokeSession, useSaveSettings, useSessions, useSettings } from "@/lib/queries";
import type { LibraryRules } from "@/types/api";

const int = (min: number, max: number, message: string) =>
  z.string().trim().refine((v) => /^\d+$/.test(v) && Number(v) >= min && Number(v) <= max, message);
const money = (min: number, message: string) =>
  z.string().trim().refine((v) => /^\d+(\.\d{1,2})?$/.test(v) && Number(v) >= min, message);

const schema = z.object({
  loanDays: int(1, 90, "1 to 90 days"),
  renewalDays: int(1, 90, "1 to 90 days"),
  maxActiveLoans: int(1, 20, "1 to 20 books"),
  finePerDay: money(0.01, "At least ₹0.01"),
  fineBlockThreshold: money(0, "₹0 or more"),
});
type Values = z.infer<typeof schema>;

function toValues(rules?: LibraryRules): Values {
  return {
    loanDays: String(rules?.loanDays ?? 14),
    renewalDays: String(rules?.renewalDays ?? 14),
    maxActiveLoans: String(rules?.maxActiveLoans ?? 3),
    finePerDay: String(rules?.finePerDay ?? 5),
    fineBlockThreshold: String(rules?.fineBlockThreshold ?? 100),
  };
}

export function SettingsView() {
  const settings = useSettings();
  const save = useSaveSettings();
  const isAdmin = useIsAdmin();
  const form = useForm<Values>({ resolver: zodResolver(schema), defaultValues: toValues() });

  useEffect(() => {
    if (settings.data) form.reset(toValues(settings.data));
  }, [settings.data, form]);

  function onSubmit(values: Values) {
    save.mutate(
      {
        loanDays: Number(values.loanDays),
        renewalDays: Number(values.renewalDays),
        maxActiveLoans: Number(values.maxActiveLoans),
        finePerDay: Number(values.finePerDay),
        fineBlockThreshold: Number(values.fineBlockThreshold),
      },
      { onError: (error) => toast.error(errorMessage(error)) },
    );
  }

  const e = form.formState.errors;

  return (
    <>
      <PageHeader title="Settings" description="Library rules and your signed-in sessions." />
      <div className="grid gap-6 xl:grid-cols-[3fr_2fr]">
        <SectionCard
          title="Lending rules"
          description={settings.data ? `Last changed ${formatRelative(settings.data.updatedAt)}. Changes apply to new loans and returns.` : undefined}
        >
          {!settings.data ? (
            <Skeleton className="m-5 h-48" />
          ) : (
            <form noValidate onSubmit={form.handleSubmit(onSubmit)}>
              <fieldset disabled={!isAdmin || save.isPending} className="grid gap-5 p-5 sm:grid-cols-2">
                <FormField id="loanDays" label="Loan period" error={e.loanDays?.message} hint="Days from issue to due date">
                  <Input id="loanDays" inputMode="numeric" className="font-mono" {...form.register("loanDays")} />
                </FormField>
                <FormField id="renewalDays" label="Renewal adds" error={e.renewalDays?.message} hint="Days added by one renewal">
                  <Input id="renewalDays" inputMode="numeric" className="font-mono" {...form.register("renewalDays")} />
                </FormField>
                <FormField id="maxActiveLoans" label="Books per member" error={e.maxActiveLoans?.message} hint="Held at the same time">
                  <Input id="maxActiveLoans" inputMode="numeric" className="font-mono" {...form.register("maxActiveLoans")} />
                </FormField>
                <FormField id="finePerDay" label="Fine per late day (₹)" error={e.finePerDay?.message}>
                  <Input id="finePerDay" inputMode="decimal" className="font-mono" {...form.register("finePerDay")} />
                </FormField>
                <FormField
                  id="fineBlockThreshold"
                  label="Block borrowing above (₹)"
                  error={e.fineBlockThreshold?.message}
                  hint="Members owing more than this can't borrow"
                  className="sm:col-span-2"
                >
                  <Input id="fineBlockThreshold" inputMode="decimal" className="w-40 font-mono" {...form.register("fineBlockThreshold")} />
                </FormField>
              </fieldset>
              <div className="flex items-center justify-between gap-4 border-t border-border px-5 py-3">
                <p className="text-sm text-ink-muted">{isAdmin ? "Every change is recorded in the activity log." : "Only an admin can change lending rules."}</p>
                {isAdmin ? (
                  <Button type="submit" disabled={save.isPending || !form.formState.isDirty}>
                    {save.isPending ? "Saving…" : "Save rules"}
                  </Button>
                ) : null}
              </div>
            </form>
          )}
        </SectionCard>

        <div className="space-y-6">
          <SessionsCard />
          <SectionCard title="Change password" bodyClassName="p-5">
            <ChangePasswordForm />
          </SectionCard>
        </div>
      </div>
    </>
  );
}

function SessionsCard() {
  const sessions = useSessions();
  const revoke = useRevokeSession();

  return (
    <SectionCard title="Your sessions" description="Devices signed in to your account. Sign out any you don't recognise." bodyClassName="px-5 py-1">
      {!sessions.data ? (
        <Skeleton className="my-4 h-24" />
      ) : (
        <ul className="divide-y divide-border">
          {sessions.data.map((session) => (
            <li key={session.id} className="flex items-start gap-3 py-3">
              <Monitor className="mt-0.5 size-4 shrink-0 text-ink-muted" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-base text-ink" title={session.userAgent ?? undefined}>
                  {describeAgent(session.userAgent)}
                  {session.current ? <span className="ml-2 text-sm text-success">This device</span> : null}
                </p>
                <p className="text-sm text-ink-muted">
                  {session.ipAddress ?? "Unknown address"} · active {formatRelative(session.lastUsedAt)} · signed in {formatDateTime(session.createdAt)}
                </p>
              </div>
              {!session.current ? (
                <Button variant="ghost" size="xs" disabled={revoke.isPending} onClick={() => revoke.mutate(session.id)}>
                  Sign out
                </Button>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </SectionCard>
  );
}

function describeAgent(ua: string | null): string {
  if (!ua) return "Unknown device";
  const browser = /Edg\//.test(ua) ? "Edge" : /Chrome\//.test(ua) ? "Chrome" : /Firefox\//.test(ua) ? "Firefox" : /Safari\//.test(ua) ? "Safari" : "Browser";
  const os = /Windows/.test(ua) ? "Windows" : /Mac OS X/.test(ua) ? "macOS" : /Android/.test(ua) ? "Android" : /iPhone|iPad/.test(ua) ? "iOS" : /Linux/.test(ua) ? "Linux" : "";
  return os ? `${browser} on ${os}` : browser;
}
