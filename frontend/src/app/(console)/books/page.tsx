import type { Metadata } from "next";
import { Suspense } from "react";
import { BooksView } from "@/components/books/books-view";

export const metadata: Metadata = { title: "Books" };

export default function BooksPage() {
  return (
    <Suspense>
      <BooksView />
    </Suspense>
  );
}
