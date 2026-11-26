"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { FormField, applyServerErrors } from "@/components/common/form-field";
import { errorMessage, isApiError } from "@/lib/api/errors";
import { useSaveMember } from "@/lib/queries";
import type { MemberDetail } from "@/types/api";

const schema = z.object({
  fullName: z.string().trim().min(1, "Enter the member's name.").max(120),
  email: z.string().trim().min(1, "Enter an email address.").pipe(z.email("Enter a valid email address.")),
  phone: z
    .string()
    .trim()
    .refine((v) => /^(?:\+?91|0)?[6-9]\d{9}$/.test(v.replace(/[\s-]/g, "")), "Enter a 10-digit mobile number starting with 6, 7, 8 or 9."),
  addressLine: z.string().trim().max(300),
  city: z.string().trim().max(80),
  postalCode: z.string().trim().refine((v) => v === "" || /^[1-9]\d{5}$/.test(v), "Enter a 6-digit PIN code."),
});

type Values = z.infer<typeof schema>;
const FIELDS = ["fullName", "email", "phone", "addressLine", "city", "postalCode"] as const;

function toValues(member?: MemberDetail): Values {
  return {
    fullName: member?.fullName ?? "",
    email: member?.email ?? "",
    phone: member?.phone ?? "",
    addressLine: member?.addressLine ?? "",
    city: member?.city ?? "",
    postalCode: member?.postalCode ?? "",
  };
}

export function MemberSheet({
  open,
  onOpenChange,
  member,
  onSaved,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  member?: MemberDetail;
  onSaved?: (member: MemberDetail) => void;
}) {
  const save = useSaveMember(member?.id);
  const { register, handleSubmit, formState, reset, setError } = useForm<Values>({ resolver: zodResolver(schema), defaultValues: toValues(member) });

  useEffect(() => {
    if (open) reset(toValues(member));
  }, [open, member, reset]);

  async function onSubmit(values: Values) {
    try {
      const saved = await save.mutateAsync({
        ...values,
        addressLine: values.addressLine || null,
        city: values.city || null,
        postalCode: values.postalCode || null,
      });
      toast.success(member ? "Member updated" : `${saved.fullName} registered as ${saved.membershipNumber}`);
      onOpenChange(false);
      onSaved?.(saved);
    } catch (error) {
      if (isApiError(error) && applyServerErrors(error.fieldErrors, setError, FIELDS)) return;
      toast.error(errorMessage(error));
    }
  }

  const errors = formState.errors;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent>
        <SheetHeader>
          <SheetTitle>{member ? "Edit member" : "Add member"}</SheetTitle>
          <SheetDescription>
            {member ? `Membership ${member.membershipNumber}` : "A membership number is assigned when you save."}
          </SheetDescription>
        </SheetHeader>
        <form id="member-form" noValidate onSubmit={handleSubmit(onSubmit)} className="scrollbar-thin flex-1 space-y-5 overflow-y-auto px-6 py-5">
          <FormField id="fullName" label="Full name" error={errors.fullName?.message}>
            <Input id="fullName" autoFocus autoComplete="off" aria-invalid={!!errors.fullName} {...register("fullName")} />
          </FormField>
          <FormField id="email" label="Email" error={errors.email?.message}>
            <Input id="email" type="email" autoComplete="off" aria-invalid={!!errors.email} {...register("email")} />
          </FormField>
          <FormField id="phone" label="Mobile" error={errors.phone?.message} hint="10 digits; +91 is optional">
            <Input id="phone" inputMode="tel" className="font-mono" aria-invalid={!!errors.phone} {...register("phone")} />
          </FormField>
          <FormField id="addressLine" label="Address" optional error={errors.addressLine?.message}>
            <Input id="addressLine" {...register("addressLine")} />
          </FormField>
          <div className="grid grid-cols-[1fr_140px] gap-4">
            <FormField id="city" label="City" optional error={errors.city?.message}>
              <Input id="city" {...register("city")} />
            </FormField>
            <FormField id="postalCode" label="PIN code" optional error={errors.postalCode?.message}>
              <Input id="postalCode" inputMode="numeric" className="font-mono" {...register("postalCode")} />
            </FormField>
          </div>
        </form>
        <SheetFooter>
          <Button variant="secondary" onClick={() => onOpenChange(false)} disabled={save.isPending}>
            Cancel
          </Button>
          <Button type="submit" form="member-form" disabled={save.isPending}>
            {save.isPending ? "Saving…" : member ? "Save changes" : "Add member"}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
