"use client";

import { RotateCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { errorMessage, isApiError } from "@/lib/api/errors";
import { cn } from "cn";

/** Inline failure state with the server's message, its request id for support, and a retry. */
export function ErrorState({
  error,
  title = "This didn't load.",
  onRetry,
  className,
}: {
  error: unknown;
  title?: string;
  onRetry?: () => void;
  className?: string;
}) {
  const requestId = isApiError(error) ? error.requestId : undefined;
  return (
    <div role="alert" className={cn("flex flex-col items-start gap-1.5 px-8 py-12", className)}>
      <p className="font-display text-xl font-medium text-ink">{title}</p>
      <p className="max-w-prose text-base text-ink-muted">{errorMessage(error)}</p>
      {requestId ? <p className="font-mono text-xs text-ink-muted">Reference {requestId}</p> : null}
      {onRetry ? (
        <Button variant="secondary" size="sm" className="mt-3" onClick={onRetry}>
          <RotateCw />
          Try again
        </Button>
      ) : null}
    </div>
  );
}
