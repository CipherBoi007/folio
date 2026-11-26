/** Client-side mirror of the server's ISBN rules, used to decide when to look a book up. */
export function normalizeIsbn(raw: string): string | null {
  const digits = raw.replace(/[\s-]/g, "").toUpperCase();
  if (/^\d{13}$/.test(digits)) {
    if (!/^97[89]/.test(digits)) return null;
    const sum = [...digits.slice(0, 12)].reduce((acc, d, i) => acc + Number(d) * (i % 2 === 0 ? 1 : 3), 0);
    return (10 - (sum % 10)) % 10 === Number(digits[12]) ? digits : null;
  }
  if (/^\d{9}[\dX]$/.test(digits)) {
    const sum = [...digits].reduce((acc, c, i) => acc + (c === "X" ? 10 : Number(c)) * (10 - i), 0);
    if (sum % 11 !== 0) return null;
    const core = `978${digits.slice(0, 9)}`;
    const s13 = [...core].reduce((acc, d, i) => acc + Number(d) * (i % 2 === 0 ? 1 : 3), 0);
    return core + ((10 - (s13 % 10)) % 10);
  }
  return null;
}

export function formatIsbn(isbn13: string): string {
  return isbn13.length === 13 ? `${isbn13.slice(0, 3)}-${isbn13.slice(3, 4)}-${isbn13.slice(4, 8)}-${isbn13.slice(8, 12)}-${isbn13.slice(12)}` : isbn13;
}
