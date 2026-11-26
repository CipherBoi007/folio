import type { Metadata } from "next";
import { Suspense } from "react";
import { FinesView } from "@/components/fines/fines-view";

export const metadata: Metadata = { title: "Fines" };

export default function Page() {
  return (
    <Suspense>
      <FinesView />
    </Suspense>
  );
}
