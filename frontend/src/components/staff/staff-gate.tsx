"use client";

import { EmptyState } from "@/components/common/empty-state";
import { StaffView } from "@/components/staff/staff-view";
import { useIsAdmin } from "@/lib/auth/session";

export function StaffGate() {
  return useIsAdmin() ? <StaffView /> : <EmptyState title="Admins only." description="Ask a head librarian to manage staff accounts." />;
}
