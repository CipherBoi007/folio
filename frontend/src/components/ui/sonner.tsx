"use client";

import { useTheme } from "next-themes";
import { Toaster as Sonner, type ToasterProps } from "sonner";
import { CircleAlert, CircleCheck, Info, LoaderCircle, TriangleAlert } from "lucide-react";

/**
 * Toasts: neutral surface + hairline; success shows an accent check, errors show the backend `message`
 * verbatim with a danger icon. No coloured backgrounds, no bounce.
 */
function Toaster(props: ToasterProps) {
  const { resolvedTheme } = useTheme();

  return (
    <Sonner
      theme={(resolvedTheme ?? "light") as ToasterProps["theme"]}
      position="bottom-right"
      gap={8}
      visibleToasts={4}
      icons={{
        success: <CircleCheck className="size-4 text-brand" />,
        info: <Info className="size-4 text-info" />,
        warning: <TriangleAlert className="size-4 text-warning" />,
        error: <CircleAlert className="size-4 text-danger" />,
        loading: <LoaderCircle className="size-4 animate-spin text-ink-muted" />,
      }}
      toastOptions={{
        classNames: {
          toast:
            "!rounded-lg !border !border-border !bg-surface !text-ink !shadow-lg !font-sans !text-base !gap-2.5 !py-3 !px-4",
          title: "!font-medium !text-base",
          description: "!text-sm !text-ink-muted",
          actionButton: "!bg-brand !text-brand-foreground !rounded-md !text-sm",
          cancelButton: "!bg-surface-muted !text-ink !rounded-md !text-sm",
        },
      }}
      {...props}
    />
  );
}

export { Toaster };
