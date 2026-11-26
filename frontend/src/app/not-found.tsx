import Link from "next/link";
import { Wordmark } from "@/components/brand/wordmark";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="flex min-h-screen flex-col px-6 py-10 sm:px-12">
      <Wordmark className="text-ink" />
      <div className="my-auto max-w-md py-16">
        <p className="label-caps">Error 404</p>
        <h1 className="mt-2 font-display text-2xl font-medium text-ink">This page isn&apos;t on the shelf.</h1>
        <p className="mt-2 text-base text-ink-muted">The link may be old, or the record may have been removed.</p>
        <Button asChild className="mt-6">
          <Link href="/dashboard">Back to the dashboard</Link>
        </Button>
      </div>
    </main>
  );
}
