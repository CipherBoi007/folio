"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";

/** Last-resort boundary for unexpected rendering errors. API errors are handled inline where they occur. */
export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <main className="flex min-h-[60vh] flex-col justify-center px-6 py-16 sm:px-12">
      <div className="max-w-md">
        <p className="label-caps">Unexpected error</p>
        <h1 className="mt-2 font-display text-2xl font-medium text-ink">Something went wrong on this page.</h1>
        <p className="mt-2 text-base text-ink-muted">
          Your data is safe. Try again, and if it keeps happening, reload the page.
        </p>
        {error.digest ? <p className="mt-2 font-mono text-xs text-ink-muted">Reference {error.digest}</p> : null}
        <Button className="mt-6" onClick={reset}>
          Try again
        </Button>
      </div>
    </main>
  );
}
