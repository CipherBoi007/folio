"use client";

import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, type TooltipContentProps } from "recharts";
import { format, parseISO } from "date-fns";
import type { TrendPoint } from "@/types/api";

function TrendTooltip({ active, payload, label }: Partial<TooltipContentProps<number, string>>) {
  if (!active || !payload?.length || typeof label !== "string") return null;
  return (
    <div className="rounded-md border border-border bg-surface px-3 py-2 text-sm shadow-lg">
      <p className="mb-1 font-medium text-ink">{format(parseISO(label), "EEE, d MMM")}</p>
      {payload.map((item) => (
        <p key={String(item.dataKey)} className="flex items-center gap-2 text-ink-muted">
          <span className="size-2 rounded-full" style={{ background: item.color }} />
          {item.dataKey === "issued" ? "Issued" : "Returned"}
          <span className="ml-auto font-mono text-ink tabular-nums">{item.value}</span>
        </p>
      ))}
    </div>
  );
}

export function LoanTrendChart({ data }: { data: TrendPoint[] }) {
  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 8, right: 8, bottom: 0, left: -24 }}>
          <CartesianGrid vertical={false} stroke="var(--hairline)" />
          <XAxis
            dataKey="date"
            tickLine={false}
            axisLine={false}
            tick={{ fill: "var(--text-muted)", fontSize: 12 }}
            tickFormatter={(value: string) => format(parseISO(value), "d MMM")}
            interval="preserveStartEnd"
            minTickGap={32}
          />
          <YAxis allowDecimals={false} tickLine={false} axisLine={false} tick={{ fill: "var(--text-muted)", fontSize: 12 }} width={40} />
          <Tooltip content={<TrendTooltip />} cursor={{ stroke: "var(--hairline)" }} />
          <Line type="monotone" dataKey="returned" stroke="var(--chart-ink-2)" strokeWidth={1.5} dot={false} activeDot={{ r: 3 }} />
          <Line type="monotone" dataKey="issued" stroke="var(--brand)" strokeWidth={2} dot={false} activeDot={{ r: 3.5 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
