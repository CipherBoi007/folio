"use client";

import { useEffect, useState, type ReactNode } from "react";
import { usePathname, useRouter } from "next/navigation";
import { toast } from "sonner";
import { cn } from "cn";
import { Sheet, SheetContent, SheetDescription, SheetTitle } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { SidebarContent } from "@/components/layout/app-sidebar";
import { BreadcrumbProvider } from "@/components/layout/breadcrumbs";
import { CommandPaletteProvider } from "@/components/layout/command-palette";
import { KeyboardShortcuts } from "@/components/layout/keyboard-shortcuts";
import { Topbar } from "@/components/layout/topbar";
import { ChangePasswordForm } from "@/components/auth/change-password-form";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useAuth } from "@/lib/auth/session";
import { usePersistentFlag } from "@/lib/local-store";
import type { StaffUser } from "@/types/api";

/** The signed-in frame. Unauthenticated visitors go to /login and come back to where they were. */
export function ConsoleShell({ children }: { children: ReactNode }) {
  const auth = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (auth.status !== "unauthenticated") return;
    if (auth.reason === "expired") {
      toast.error("Your session has ended. Sign in again.", { id: "session-ended" });
    }
    router.replace(pathname && pathname !== "/" ? `/login?next=${encodeURIComponent(pathname)}` : "/login");
  }, [auth, pathname, router]);

  if (auth.status !== "authenticated") return <ShellSkeleton />;
  return <SignedInShell user={auth.user}>{children}</SignedInShell>;
}

function SignedInShell({ user, children }: { user: StaffUser; children: ReactNode }) {
  const [collapsed, setCollapsed] = usePersistentFlag("folio.sidebar.collapsed");
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <CommandPaletteProvider user={user}>
      <BreadcrumbProvider>
        <KeyboardShortcuts />
        <Dialog open={user.mustChangePassword}>
          <DialogContent showCloseButton={false} onEscapeKeyDown={(e) => e.preventDefault()} onPointerDownOutside={(e) => e.preventDefault()}>
            <DialogHeader>
              <DialogTitle>Choose a new password</DialogTitle>
              <DialogDescription>Your account is using a temporary password. Set your own to continue.</DialogDescription>
            </DialogHeader>
            <ChangePasswordForm />
          </DialogContent>
        </Dialog>
        <div className="flex min-h-screen">
          <aside
            className={cn(
              "sticky top-0 hidden h-screen shrink-0 border-r border-border bg-sidebar transition-[width] duration-200 ease-out lg:block print:hidden",
              collapsed ? "w-16" : "w-60",
            )}
          >
            <SidebarContent user={user} collapsed={collapsed} onToggleCollapsed={() => setCollapsed(!collapsed)} />
          </aside>

          <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
            <SheetContent side="left" className="w-60 p-0 sm:max-w-60" showCloseButton={false}>
              <SheetTitle className="sr-only">Navigation</SheetTitle>
              <SheetDescription className="sr-only">Folio sections</SheetDescription>
              <SidebarContent user={user} onNavigate={() => setMobileOpen(false)} />
            </SheetContent>
          </Sheet>

          <div className="flex min-w-0 flex-1 flex-col">
            <Topbar onOpenMobileNav={() => setMobileOpen(true)} />
            <main id="main" className="mx-auto w-full max-w-[1280px] flex-1 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
              {children}
            </main>
          </div>
        </div>
      </BreadcrumbProvider>
    </CommandPaletteProvider>
  );
}

function ShellSkeleton() {
  return (
    <div className="flex min-h-screen" aria-busy="true" aria-label="Loading Folio">
      <div className="hidden w-60 shrink-0 border-r border-border p-4 lg:block">
        <Skeleton className="h-6 w-16" />
        <div className="mt-8 space-y-6">
          {[1, 2, 1, 2, 2].map((count, group) => (
            <div key={group} className="space-y-2">
              <Skeleton className="h-3 w-16" />
              {Array.from({ length: count }, (_, i) => (
                <Skeleton key={i} className="h-7 w-full" />
              ))}
            </div>
          ))}
        </div>
      </div>
      <div className="flex-1">
        <div className="flex h-14 items-center justify-between border-b border-border px-8">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-8 w-64" />
        </div>
        <div className="mx-auto max-w-[1280px] px-8 py-8">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="mt-2 h-4 w-72" />
        </div>
      </div>
    </div>
  );
}
