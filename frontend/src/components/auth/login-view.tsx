"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { CircleAlert, LoaderCircle } from "lucide-react";
import { BookIllustration } from "@/components/brand/book-illustration";
import { Wordmark } from "@/components/brand/wordmark";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { errorMessage } from "@/lib/api/errors";
import { signIn } from "@/lib/auth/auth-client";
import { useAuth } from "@/lib/auth/session";

const DEMO_ACCOUNTS = {
  ADMIN: { email: "admin@folio.dev", password: "Admin@123", label: "Continue as demo Admin", hint: "Head librarian · full access" },
  LIBRARIAN: { email: "desk@folio.dev", password: "Desk@123", label: "Continue as demo Librarian", hint: "Desk staff · day-to-day circulation" },
} as const;

const loginSchema = z.object({
  email: z.string().trim().min(1, "Enter your email address.").pipe(z.email("Enter a valid email address.")),
  password: z.string().min(1, "Enter your password."),
});

type LoginValues = z.infer<typeof loginSchema>;

/** Only allow same-origin relative redirects after sign-in (no open redirects). */
function safeNext(next: string | null): string {
  return next && next.startsWith("/") && !next.startsWith("//") ? next : "/dashboard";
}

export function LoginView() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const auth = useAuth();
  const next = safeNext(searchParams.get("next"));

  useEffect(() => {
    if (auth.status === "authenticated") router.replace(next);
  }, [auth.status, next, router]);

  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const login = useMutation({
    mutationFn: (values: LoginValues) => signIn(values.email, values.password),
    meta: { silentError: true },
    onSuccess: () => router.replace(next),
  });

  // Which control started the sign-in, so only that one shows progress.
  const [source, setSource] = useState<"form" | keyof typeof DEMO_ACCOUNTS>("form");
  const busy = login.isPending || login.isSuccess;

  function signInAs(role: keyof typeof DEMO_ACCOUNTS) {
    const { email, password } = DEMO_ACCOUNTS[role];
    form.clearErrors();
    setSource(role);
    login.mutate({ email, password });
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Brand panel */}
      <aside className="relative hidden flex-col justify-between overflow-hidden bg-brand p-12 text-brand-foreground lg:flex">
        <Wordmark className="text-[26px]" />
        <div className="max-w-lg">
          <BookIllustration className="mb-12 w-full max-w-md opacity-80" />
          <p className="font-display text-3xl font-medium">Every copy, accounted for.</p>
          <p className="mt-4 max-w-md text-lg leading-relaxed opacity-80">
            The operations console for the library desk: stock, members, issues and returns, with a ledger behind every change.
          </p>
        </div>
        <p className="text-sm opacity-70">Folio · Library operations console</p>
      </aside>

      {/* Sign-in */}
      <main className="flex items-center justify-center px-6 py-12 sm:px-12">
        <div className="w-full max-w-[380px]">
          <Wordmark className="mb-10 text-[26px] text-ink lg:hidden" />
          <h1 className="font-display text-2xl font-medium text-ink">Sign in</h1>
          <p className="mt-1.5 text-base text-ink-muted">Staff accounts only. Library members don&apos;t sign in to Folio.</p>

          <form className="mt-8" noValidate onSubmit={form.handleSubmit((values) => {
              setSource("form");
              login.mutate(values);
            })}>
            <FieldGroup className="gap-4">
              {login.isError ? (
                <div role="alert" className="flex items-start gap-2.5 rounded-md bg-danger-soft px-3 py-2.5 text-base text-danger">
                  <CircleAlert aria-hidden className="mt-0.5 size-4 shrink-0" />
                  <span>{errorMessage(login.error)}</span>
                </div>
              ) : null}

              <Field data-invalid={Boolean(form.formState.errors.email)}>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  autoComplete="username"
                  autoFocus
                  placeholder="you@library.org"
                  aria-invalid={Boolean(form.formState.errors.email)}
                  disabled={busy}
                  {...form.register("email")}
                />
                <FieldError errors={[form.formState.errors.email]} />
              </Field>

              <Field data-invalid={Boolean(form.formState.errors.password)}>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  aria-invalid={Boolean(form.formState.errors.password)}
                  disabled={busy}
                  {...form.register("password")}
                />
                <FieldError errors={[form.formState.errors.password]} />
              </Field>

              <Button type="submit" className="mt-1 w-full" disabled={busy}>
                {busy && source === "form" ? <LoaderCircle className="animate-spin" /> : null}
                {busy && source === "form" ? "Signing in…" : "Sign in"}
              </Button>
            </FieldGroup>
          </form>

          <div className="my-7 flex items-center gap-3">
            <span className="h-px flex-1 bg-border" />
            <span className="text-sm text-ink-muted">or explore with a demo account</span>
            <span className="h-px flex-1 bg-border" />
          </div>

          <div className="space-y-2">
            {(Object.keys(DEMO_ACCOUNTS) as Array<keyof typeof DEMO_ACCOUNTS>).map((role) => {
              const account = DEMO_ACCOUNTS[role];
              const pending = busy && source === role;
              return (
                <Button
                  key={role}
                  type="button"
                  variant="secondary"
                  className="h-auto w-full justify-between px-3.5 py-2.5"
                  disabled={busy}
                  onClick={() => signInAs(role)}
                >
                  <span className="flex flex-col items-start gap-0.5">
                    <span className="text-base font-medium">{account.label}</span>
                    <span className="text-sm font-normal text-ink-muted">{account.hint}</span>
                  </span>
                  {pending ? <LoaderCircle className="animate-spin text-ink-muted" /> : null}
                </Button>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}
