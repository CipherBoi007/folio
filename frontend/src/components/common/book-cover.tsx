"use client";

import { useState } from "react";
import Image from "next/image";
import { cn } from "cn";

/** Earthy, muted solids for generated covers: moss, clay, slate, ochre, plum-brown, teal-grey, sand, ink. */
const COVER_TONES = [
  { bg: "#4f5d3a", fg: "#f3efe4" }, // moss
  { bg: "#9a5b3f", fg: "#f7efe6" }, // clay
  { bg: "#4a5560", fg: "#eef0f0" }, // slate
  { bg: "#a57a2a", fg: "#faf3e3" }, // ochre
  { bg: "#5e3f45", fg: "#f4ebeb" }, // plum-brown
  { bg: "#3f5f5c", fg: "#ecf2f0" }, // teal-grey
  { bg: "#c9b78f", fg: "#2b2a27" }, // sand
  { bg: "#2b2a27", fg: "#ece8de" }, // ink
] as const;

function hash(value: string): number {
  let h = 2166136261;
  for (let i = 0; i < value.length; i++) {
    h ^= value.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return h >>> 0;
}

export type CoverSize = "xs" | "sm" | "md" | "lg" | "xl" | "fill";

const SIZES: Record<CoverSize, { width: number; title: string; author: string; pad: string; imgSize: "S" | "M" | "L" }> = {
  xs: { width: 28, title: "text-[5px]", author: "hidden", pad: "p-1", imgSize: "S" },
  sm: { width: 40, title: "text-[6.5px]", author: "hidden", pad: "p-1.5", imgSize: "S" },
  md: { width: 96, title: "text-[11px]", author: "text-[7px]", pad: "p-2.5", imgSize: "M" },
  lg: { width: 160, title: "text-base", author: "text-[9px]", pad: "p-4", imgSize: "M" },
  xl: { width: 240, title: "text-xl", author: "text-xs", pad: "p-6", imgSize: "L" },
  fill: { width: 180, title: "text-lg", author: "text-[10px]", pad: "p-4", imgSize: "M" },
};

/**
 * Book cover at 2:3 with a hairline border. Tries the Open Library cover for the ISBN; on a miss (they return
 * 404 thanks to `default=false`) it renders a typographic cover whose colour is derived from the ISBN, so the
 * same book always looks the same.
 */
export function BookCover({
  isbn,
  title,
  author,
  size = "md",
  className,
  priority = false,
}: {
  isbn?: string | null;
  title: string;
  author?: string | null;
  size?: CoverSize;
  className?: string;
  priority?: boolean;
}) {
  const [failedIsbn, setFailedIsbn] = useState<string | null>(null);
  const spec = SIZES[size];
  const showImage = Boolean(isbn) && failedIsbn !== isbn;

  return (
    <div
      className={cn("relative shrink-0 overflow-hidden rounded-sm border border-border bg-surface-muted", className)}
      style={{ width: size === "fill" ? "100%" : spec.width, aspectRatio: "2 / 3" }}
    >
      {showImage && isbn ? (
        <Image
          src={`https://covers.openlibrary.org/b/isbn/${isbn}-${spec.imgSize}.jpg?default=false`}
          alt={`Cover of ${title}`}
          fill
          sizes={`${spec.width}px`}
          unoptimized
          priority={priority}
          className="object-cover"
          onError={() => setFailedIsbn(isbn)}
        />
      ) : (
        <TypographicCover seed={isbn || title} title={title} author={author} size={size} />
      )}
    </div>
  );
}

function TypographicCover({
  seed,
  title,
  author,
  size,
}: {
  seed: string;
  title: string;
  author?: string | null;
  size: CoverSize;
}) {
  const tone = COVER_TONES[hash(seed) % COVER_TONES.length];
  const spec = SIZES[size];

  return (
    <div
      role="img"
      aria-label={`Cover of ${title}`}
      className={cn("absolute inset-0 flex flex-col justify-between", spec.pad)}
      style={{ backgroundColor: tone.bg, color: tone.fg }}
    >
      {/* inset hairline frame, like a printed cloth cover */}
      <span aria-hidden className="pointer-events-none absolute inset-[6%] rounded-[2px] border opacity-35" style={{ borderColor: tone.fg }} />
      <span className={cn("relative font-display leading-[1.1] font-medium line-clamp-4 break-words", spec.title)}>{title}</span>
      {author ? (
        <span className={cn("relative font-sans font-medium tracking-[0.08em] uppercase line-clamp-2 opacity-80", spec.author)}>
          {author}
        </span>
      ) : null}
    </div>
  );
}
