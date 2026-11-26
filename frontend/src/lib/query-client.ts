import { MutationCache, QueryCache, QueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { errorMessage, isApiError } from "@/lib/api/errors";

declare module "@tanstack/react-query" {
  interface Register {
    mutationMeta: {
      /** Skip the global error toast (the caller renders the error inline instead). */
      silentError?: boolean;
    };
  }
}

/**
 * Query defaults: 30s freshness, no retries for client errors (4xx are answers, not glitches),
 * and one global place that turns failed mutations into toasts showing the backend message verbatim.
 */
export function createQueryClient(): QueryClient {
  return new QueryClient({
    queryCache: new QueryCache(),
    mutationCache: new MutationCache({
      onError: (error, _variables, _context, mutation) => {
        if (mutation.meta?.silentError) return;
        if (isApiError(error) && error.status === 401) return; // handled by the session expiry flow
        toast.error(errorMessage(error));
      },
    }),
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        refetchOnWindowFocus: true,
        retry: (failureCount, error) => {
          if (isApiError(error) && error.status > 0 && error.status < 500) return false;
          return failureCount < 2;
        },
      },
      mutations: {
        retry: false,
      },
    },
  });
}
