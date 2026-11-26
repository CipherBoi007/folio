"use client";

import { ChevronsUpDown, LogOut, Monitor, Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { cn } from "cn";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useSignOut } from "@/lib/auth/use-sign-out";
import type { Role, StaffUser } from "@/types/api";
import { initials } from "@/lib/format";

export function RoleBadge({ role }: { role: Role }) {
  return <Badge tone={role === "ADMIN" ? "brand" : "neutral"}>{role === "ADMIN" ? "Admin" : "Librarian"}</Badge>;
}

export function StaffAvatar({ name, className }: { name: string; className?: string }) {
  return (
    <span
      aria-hidden
      className={cn(
        "inline-flex size-8 shrink-0 items-center justify-center rounded-full bg-brand-soft text-xs font-semibold text-brand",
        className,
      )}
    >
      {initials(name)}
    </span>
  );
}

export function UserMenu({ user, collapsed }: { user: StaffUser; collapsed: boolean }) {
  const { theme, setTheme } = useTheme();
  const signOut = useSignOut();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        className={cn(
          "flex w-full min-w-0 items-center gap-2.5 rounded-md p-1.5 text-left transition-colors hover:bg-surface-muted aria-expanded:bg-surface-muted",
          collapsed && "justify-center",
        )}
        aria-label={`Account menu for ${user.name}`}
      >
        <StaffAvatar name={user.name} />
        {collapsed ? null : (
          <>
            <span className="flex min-w-0 flex-1 flex-col">
              <span className="truncate text-base font-medium text-ink">{user.name}</span>
              <span className="mt-0.5">
                <RoleBadge role={user.role} />
              </span>
            </span>
            <ChevronsUpDown className="size-4 shrink-0 text-ink-muted" />
          </>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent side="top" align="start" className="w-60">
        <DropdownMenuLabel className="flex flex-col gap-0.5 py-2">
          <span className="text-base font-medium text-ink">{user.name}</span>
          <span className="truncate text-sm font-normal text-ink-muted">{user.email}</span>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuLabel className="label-caps py-1">Theme</DropdownMenuLabel>
        <DropdownMenuRadioGroup value={theme ?? "light"} onValueChange={setTheme}>
          <DropdownMenuRadioItem value="light">
            <Sun />
            Paper
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value="dark">
            <Moon />
            Reading room
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value="system">
            <Monitor />
            Match system
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem onSelect={() => void signOut()}>
          <LogOut />
          Sign out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
