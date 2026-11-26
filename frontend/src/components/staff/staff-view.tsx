"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ErrorState } from "@/components/common/error-state";
import { FormField } from "@/components/common/form-field";
import { MemberAvatar } from "@/components/common/member-avatar";
import { PageHeader } from "@/components/common/page-header";
import { RoleBadge } from "@/components/layout/user-menu";
import { errorMessage, isApiError } from "@/lib/api/errors";
import { useCurrentUser } from "@/lib/auth/session";
import { formatRelative } from "@/lib/format";
import { useResetPassword, useSaveStaff, useStaff } from "@/lib/queries";
import type { Role, StaffAccount } from "@/types/api";

type Editing = { mode: "create" } | { mode: "edit"; account: StaffAccount } | { mode: "reset"; account: StaffAccount } | null;

export function StaffView() {
  const staff = useStaff();
  const me = useCurrentUser();
  const [editing, setEditing] = useState<Editing>(null);

  return (
    <>
      <PageHeader
        title="Staff"
        description="Who can sign in to Folio. New accounts and resets use a temporary password that must be changed at first sign-in."
        actions={
          <Button onClick={() => setEditing({ mode: "create" })}>
            <Plus />
            Add staff
          </Button>
        }
      />
      {staff.isError ? (
        <ErrorState error={staff.error} onRetry={() => staff.refetch()} />
      ) : (
        <div className="overflow-hidden rounded-lg border border-border bg-surface">
          <Table>
            <TableHeader>
              <TableRow className="hover:bg-transparent">
                <TableHead>Name</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Last sign-in</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(staff.data ?? []).map((a) => (
                <TableRow key={a.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <MemberAvatar name={a.name} />
                      <div>
                        <p className="font-medium">
                          {a.name}
                          {a.id === me?.id ? <span className="ml-2 text-xs text-ink-muted">you</span> : null}
                        </p>
                        <p className="text-sm text-ink-muted">{a.email}</p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <RoleBadge role={a.role} />
                  </TableCell>
                  <TableCell>
                    {!a.active ? <Badge tone="neutral">Deactivated</Badge> : a.mustChangePassword ? <Badge tone="warning">Temporary password</Badge> : <Badge tone="success" dot>Active</Badge>}
                  </TableCell>
                  <TableCell className="text-sm text-ink-muted">{a.lastLoginAt ? formatRelative(a.lastLoginAt) : "Never"}</TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" size="xs" onClick={() => setEditing({ mode: "edit", account: a })}>
                        Edit
                      </Button>
                      <Button variant="secondary" size="xs" onClick={() => setEditing({ mode: "reset", account: a })}>
                        Reset password
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      <Dialog open={editing !== null} onOpenChange={(open) => !open && setEditing(null)}>
        <DialogContent>{editing ? <StaffForm key={editing.mode} editing={editing} self={me?.id} onDone={() => setEditing(null)} /> : null}</DialogContent>
      </Dialog>
    </>
  );
}

function StaffForm({ editing, self, onDone }: { editing: NonNullable<Editing>; self?: number; onDone: () => void }) {
  const account = editing.mode === "create" ? undefined : editing.account;
  const save = useSaveStaff(editing.mode === "edit" ? account?.id : undefined);
  const reset = useResetPassword();
  const [name, setName] = useState(account?.name ?? "");
  const [email, setEmail] = useState(account?.email ?? "");
  const [role, setRole] = useState<Role>(account?.role ?? "LIBRARIAN");
  const [active, setActive] = useState(account?.active ?? true);
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const isSelf = account?.id === self;

  function fail(error: unknown) {
    if (isApiError(error) && error.fieldErrors.length) setErrors(Object.fromEntries(error.fieldErrors.map((f) => [f.field, f.message])));
    else toast.error(errorMessage(error));
  }

  function submit() {
    setErrors({});
    if (editing.mode === "reset" && account) {
      reset.mutate({ id: account.id, temporaryPassword: password }, { onSuccess: () => (toast.success(`Password reset for ${account.name}`), onDone()), onError: fail });
    } else if (editing.mode === "edit") {
      save.mutate({ name, role, active }, { onSuccess: () => (toast.success("Account updated"), onDone()), onError: fail });
    } else {
      save.mutate({ name, email, role, temporaryPassword: password }, { onSuccess: () => (toast.success(`Account created for ${name}`), onDone()), onError: fail });
    }
  }

  const title = editing.mode === "create" ? "Add staff" : editing.mode === "edit" ? `Edit ${account?.name}` : `Reset password for ${account?.name}`;

  return (
    <>
      <DialogHeader>
        <DialogTitle>{title}</DialogTitle>
        <DialogDescription>
          {editing.mode === "reset"
            ? "They'll be signed out everywhere and asked to choose a new password at their next sign-in."
            : editing.mode === "create"
              ? "Share the temporary password with them privately."
              : "Deactivating signs them out immediately."}
        </DialogDescription>
      </DialogHeader>
      <div className="space-y-4">
        {editing.mode !== "reset" ? (
          <>
            <FormField id="staff-name" label="Name" error={errors.name}>
              <Input id="staff-name" value={name} onChange={(e) => setName(e.target.value)} />
            </FormField>
            {editing.mode === "create" ? (
              <FormField id="staff-email" label="Email" error={errors.email}>
                <Input id="staff-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              </FormField>
            ) : null}
            <div className="grid grid-cols-2 gap-4">
              <FormField id="staff-role" label="Role" hint={isSelf ? "You can't change your own role." : undefined}>
                <Select value={role} onValueChange={(v) => setRole(v as Role)} disabled={isSelf}>
                  <SelectTrigger id="staff-role" className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="LIBRARIAN">Librarian</SelectItem>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                  </SelectContent>
                </Select>
              </FormField>
              {editing.mode === "edit" ? (
                <FormField id="staff-active" label="Status">
                  <Select value={active ? "1" : "0"} onValueChange={(v) => setActive(v === "1")} disabled={isSelf}>
                    <SelectTrigger id="staff-active" className="w-full">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="1">Active</SelectItem>
                      <SelectItem value="0">Deactivated</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
              ) : null}
            </div>
          </>
        ) : null}
        {editing.mode !== "edit" ? (
          <FormField id="staff-password" label="Temporary password" error={errors.temporaryPassword} hint="At least 8 characters, with letters and numbers">
            <Input id="staff-password" type="text" autoComplete="off" className="font-mono" value={password} onChange={(e) => setPassword(e.target.value)} />
          </FormField>
        ) : null}
      </div>
      <DialogFooter>
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button onClick={submit} disabled={save.isPending || reset.isPending}>
          {editing.mode === "reset" ? "Reset password" : "Save"}
        </Button>
      </DialogFooter>
    </>
  );
}
