import type { Metadata } from "next";
import { Suspense } from "react";
import { MembersView } from "@/components/members/members-view";

export const metadata: Metadata = { title: "Members" };

export default function MembersPage() {
  return (
    <Suspense>
      <MembersView />
    </Suspense>
  );
}
