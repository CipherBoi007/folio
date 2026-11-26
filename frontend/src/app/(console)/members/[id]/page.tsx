import { Suspense } from "react";
import { notFound } from "next/navigation";
import { MemberDetailView } from "@/components/members/member-detail-view";

async function MemberDetail({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const memberId = Number(id);
  if (!Number.isSafeInteger(memberId) || memberId <= 0) notFound();
  return <MemberDetailView id={memberId} />;
}

export default function MemberPage({ params }: { params: Promise<{ id: string }> }) {
  return (
    <Suspense>
      <MemberDetail params={params} />
    </Suspense>
  );
}
