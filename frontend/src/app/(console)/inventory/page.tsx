import type { Metadata } from "next";
import { Suspense } from "react";
import { InventoryView } from "@/components/inventory/inventory-view";

export const metadata: Metadata = { title: "Inventory" };

export default function Page() {
  return (
    <Suspense>
      <InventoryView />
    </Suspense>
  );
}
