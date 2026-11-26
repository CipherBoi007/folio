"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { signOut } from "@/lib/auth/auth-client";

export function useSignOut() {
  const router = useRouter();
  const queryClient = useQueryClient();

  return useCallback(async () => {
    await signOut();
    queryClient.clear();
    router.replace("/login");
  }, [queryClient, router]);
}
