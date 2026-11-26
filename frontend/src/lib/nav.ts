import {
  ArrowLeftRight,
  BookOpen,
  History,
  LayoutGrid,
  Layers,
  type LucideIcon,
  Receipt,
  Settings2,
  UserCog,
  Users,
} from "lucide-react";
import type { Role } from "@/types/api";

export interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
  /** Only these roles see the item. Omitted = everyone. */
  roles?: Role[];
}

export interface NavGroup {
  label: string;
  items: NavItem[];
}

/** Sidebar information architecture (CLAUDE.md §8 › Layout). */
export const NAV_GROUPS: NavGroup[] = [
  { label: "Overview", items: [{ label: "Dashboard", href: "/dashboard", icon: LayoutGrid }] },
  {
    label: "Collection",
    items: [
      { label: "Books", href: "/books", icon: BookOpen },
      { label: "Inventory", href: "/inventory", icon: Layers },
    ],
  },
  { label: "People", items: [{ label: "Members", href: "/members", icon: Users }] },
  {
    label: "Desk",
    items: [
      { label: "Circulation", href: "/circulation", icon: ArrowLeftRight },
      { label: "Fines", href: "/fines", icon: Receipt },
    ],
  },
  {
    label: "Admin",
    items: [
      { label: "Activity", href: "/activity", icon: History },
      { label: "Staff", href: "/staff", icon: UserCog, roles: ["ADMIN"] },
      { label: "Settings", href: "/settings", icon: Settings2 },
    ],
  },
];

export function navGroupsFor(role: Role | undefined): NavGroup[] {
  return NAV_GROUPS.map((group) => ({
    ...group,
    items: group.items.filter((item) => !item.roles || (role !== undefined && item.roles.includes(role))),
  })).filter((group) => group.items.length > 0);
}

/** The nav item that owns a pathname (e.g. /books/42 → Books). */
export function findNavItem(pathname: string): NavItem | undefined {
  return NAV_GROUPS.flatMap((g) => g.items).find((item) => pathname === item.href || pathname.startsWith(item.href + "/"));
}
