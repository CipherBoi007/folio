import type { Metadata } from "next";
import { Suspense } from "react";
import { CirculationView } from "@/components/circulation/circulation-view";

export const metadata: Metadata = { title: "Circulation" };

export default function CirculationPage() {
  return (
    <Suspense>
      <CirculationView />
    </Suspense>
  );
}
