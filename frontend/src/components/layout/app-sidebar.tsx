"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { PanelLeftClose, PanelLeftOpen } from "lucide-react";
import { cn } from "cn";
import { Wordmark } from "@/components/brand/wordmark";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { UserMenu } from "@/components/layout/user-menu";
import type { StaffUser } from "@/types/api";
import { navGroupsFor, type NavItem } from "@/lib/nav";

function isActive(pathname: string, href: string) {
  return pathname === href || pathname.startsWith(href + "/");
}

function NavLink({ item, collapsed, onNavigate }: { item: NavItem; collapsed: boolean; onNavigate?: () => void }) {
  const pathname = usePathname();
  const active = isActive(pathname, item.href);
  const Icon = item.icon;

  const link = (
    <Link
      href={item.href}
      onClick={onNavigate}
      aria-current={active ? "page" : undefined}
      className={cn(
        "flex h-8 items-center gap-2.5 rounded-md px-2.5 text-base transition-colors duration-150",
        active ? "bg-brand-soft font-medium text-brand" : "text-ink-muted hover:bg-surface-muted hover:text-ink",
        collapsed && "justify-center px-0",
      )}
    >
      <Icon aria-hidden className="size-4 shrink-0" strokeWidth={active ? 2 : 1.75} />
      {collapsed ? <span className="sr-only">{item.label}</span> : <span className="truncate">{item.label}</span>}
    </Link>
  );

  if (!collapsed) return link;
  return (
    <Tooltip>
      <TooltipTrigger asChild>{link}</TooltipTrigger>
      <TooltipContent side="right">{item.label}</TooltipContent>
    </Tooltip>
  );
}

export function SidebarContent({
  user,
  collapsed = false,
  onToggleCollapsed,
  onNavigate,
}: {
  user: StaffUser;
  collapsed?: boolean;
  onToggleCollapsed?: () => void;
  onNavigate?: () => void;
}) {
  const groups = navGroupsFor(user.role);

  return (
    <div className="flex h-full flex-col">
      <div className={cn("flex h-14 shrink-0 items-center px-4", collapsed ? "justify-center px-0" : "justify-between")}>
        <Link href="/dashboard" onClick={onNavigate} className="rounded-sm text-ink" aria-label="Folio dashboard">
          <Wordmark compact={collapsed} />
        </Link>
        {onToggleCollapsed && !collapsed ? (
          <Button variant="ghost" size="icon-xs" onClick={onToggleCollapsed} aria-label="Collapse sidebar">
            <PanelLeftClose />
          </Button>
        ) : null}
      </div>

      <nav aria-label="Primary" className="scrollbar-thin flex-1 overflow-y-auto px-3 pt-2 pb-4">
        {groups.map((group) => (
          <div key={group.label} className="mb-5 last:mb-0">
            {collapsed ? (
              <div aria-hidden className="mx-auto mb-2 h-px w-6 bg-border" />
            ) : (
              <div className="label-caps mb-1.5 px-2.5">{group.label}</div>
            )}
            <ul className="space-y-0.5">
              {group.items.map((item) => (
                <li key={item.href}>
                  <NavLink item={item} collapsed={collapsed} onNavigate={onNavigate} />
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      <div className={cn("shrink-0 border-t border-border p-3", collapsed ? "flex flex-col items-center gap-2" : "space-y-2")}>
        {collapsed && onToggleCollapsed ? (
          <Tooltip>
            <TooltipTrigger asChild>
              <Button variant="ghost" size="icon-sm" onClick={onToggleCollapsed} aria-label="Expand sidebar">
                <PanelLeftOpen />
              </Button>
            </TooltipTrigger>
            <TooltipContent side="right">Expand sidebar</TooltipContent>
          </Tooltip>
        ) : null}
        <div className={cn("flex items-center gap-1", collapsed && "flex-col")}>
          <div className="min-w-0 flex-1">
            <UserMenu user={user} collapsed={collapsed} />
          </div>
          <ThemeToggle side={collapsed ? "right" : "top"} />
        </div>
      </div>
    </div>
  );
}
