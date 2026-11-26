"use client";

import { cloneElement, type ReactElement } from "react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { useIsAdmin } from "@/lib/auth/session";

/** Renders the child disabled, with an explanation, for staff who aren't admins. */
export function AdminOnly({ children, action }: { children: ReactElement<{ disabled?: boolean }>; action: string }) {
  const isAdmin = useIsAdmin();
  if (isAdmin) return children;
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span tabIndex={0} className="inline-flex rounded-md">
          {cloneElement(children, { disabled: true })}
        </span>
      </TooltipTrigger>
      <TooltipContent>Only an admin can {action}.</TooltipContent>
    </Tooltip>
  );
}
