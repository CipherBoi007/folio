"use client";

import { Check, ChevronDown } from "lucide-react";
import { cn } from "cn";
import { Button } from "@/components/ui/button";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

const ALL = "__all";

export interface Option {
  value: string;
  label: string;
  hint?: string;
}

/** Single-choice filter. The first entry ("All …") clears the filter. */
export function FilterSelect({
  value,
  onChange,
  options,
  allLabel,
  className,
  ariaLabel,
}: {
  value: string | undefined;
  onChange: (value: string | undefined) => void;
  options: Option[];
  allLabel: string;
  className?: string;
  ariaLabel: string;
}) {
  return (
    <Select value={value ?? ALL} onValueChange={(next) => onChange(next === ALL ? undefined : next)}>
      <SelectTrigger className={cn("min-w-36", className)} aria-label={ariaLabel}>
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value={ALL}>{allLabel}</SelectItem>
        {options.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

/** Multi-choice filter in a searchable popover, e.g. genres. */
export function MultiSelectFilter({
  values,
  onChange,
  options,
  label,
}: {
  values: string[];
  onChange: (values: string[]) => void;
  options: Option[];
  label: string;
}) {
  const selected = new Set(values);
  const summary =
    values.length === 0
      ? label
      : values.length === 1
        ? (options.find((o) => o.value === values[0])?.label ?? label)
        : `${label} · ${values.length}`;

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant="secondary" className={cn("justify-between gap-2 font-normal", values.length > 0 && "border-brand/40 text-brand")}>
          <span className="truncate">{summary}</span>
          <ChevronDown className="size-4 text-ink-muted" />
        </Button>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-64 p-0">
        <Command>
          <CommandInput placeholder={`Filter ${label.toLowerCase()}…`} />
          <CommandList className="max-h-72 p-1">
            <CommandEmpty>No matches.</CommandEmpty>
            <CommandGroup>
              {options.map((option) => {
                const checked = selected.has(option.value);
                return (
                  <CommandItem
                    key={option.value}
                    value={option.label}
                    onSelect={() => {
                      const next = new Set(selected);
                      if (checked) next.delete(option.value);
                      else next.add(option.value);
                      onChange([...next]);
                    }}
                  >
                    <span
                      className={cn(
                        "flex size-4 items-center justify-center rounded-[4px] border",
                        checked ? "border-brand bg-brand text-brand-foreground" : "border-input",
                      )}
                    >
                      {checked ? <Check className="size-3" /> : null}
                    </span>
                    <span className="flex-1 truncate">{option.label}</span>
                    {option.hint ? <span className="font-mono text-xs text-ink-muted">{option.hint}</span> : null}
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
          {values.length > 0 ? (
            <div className="border-t border-border p-1">
              <Button variant="ghost" size="sm" className="w-full" onClick={() => onChange([])}>
                Clear
              </Button>
            </div>
          ) : null}
        </Command>
      </PopoverContent>
    </Popover>
  );
}
