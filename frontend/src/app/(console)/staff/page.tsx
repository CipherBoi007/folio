import type { Metadata } from "next";
import { StaffGate } from "@/components/staff/staff-gate";

export const metadata: Metadata = { title: "Staff" };

export default function StaffPage() {
  return <StaffGate />;
}
