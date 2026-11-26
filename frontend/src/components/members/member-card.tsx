"use client";

import { useEffect, useState } from "react";
import QRCode from "qrcode";
import { Printer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Wordmark } from "@/components/brand/wordmark";
import { formatDate } from "@/lib/format";
import type { MemberDetail } from "@/types/api";

/** Credit-card sized membership card with a QR code of the membership number, printable on its own. */
export function MemberCardDialog({ member, open, onOpenChange }: { member: MemberDetail; open: boolean; onOpenChange: (open: boolean) => void }) {
  const [qr, setQr] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    void QRCode.toString(member.membershipNumber, { type: "svg", margin: 0, errorCorrectionLevel: "M", color: { dark: "#1b1a17", light: "#00000000" } }).then(setQr);
  }, [open, member.membershipNumber]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg print:max-w-none print:border-0 print:shadow-none">
        <DialogHeader className="print:hidden">
          <DialogTitle>Membership card</DialogTitle>
          <DialogDescription>Print on card stock and trim to size. The QR code holds the membership number.</DialogDescription>
        </DialogHeader>

        <div id="member-card" className="mx-auto flex aspect-[85.6/54] w-full max-w-[400px] flex-col justify-between overflow-hidden rounded-lg border border-border bg-[#f6f4ef] p-5 text-[#1b1a17]">
          <div className="flex items-start justify-between">
            <Wordmark className="text-xl" />
            <span className="text-[10px] font-medium tracking-[0.08em] uppercase opacity-70">Library member</span>
          </div>
          <div className="flex items-end justify-between gap-4">
            <div className="min-w-0">
              <p className="truncate font-display text-xl font-medium">{member.fullName}</p>
              <p className="mt-1 font-mono text-sm">{member.membershipNumber}</p>
              <p className="mt-0.5 text-[11px] opacity-70">Member since {formatDate(member.joinedOn)}</p>
            </div>
            {qr ? <div className="size-20 shrink-0" dangerouslySetInnerHTML={{ __html: qr }} aria-label="QR code of the membership number" /> : <div className="size-20" />}
          </div>
        </div>

        <div className="flex justify-end print:hidden">
          <Button onClick={() => window.print()}>
            <Printer />
            Print card
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
