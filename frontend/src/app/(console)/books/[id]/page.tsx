import { Suspense } from "react";
import { notFound } from "next/navigation";
import { BookDetailView } from "@/components/books/book-detail-view";

async function BookDetail({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const bookId = Number(id);
  if (!Number.isSafeInteger(bookId) || bookId <= 0) notFound();
  return <BookDetailView id={bookId} />;
}

export default function BookPage({ params }: { params: Promise<{ id: string }> }) {
  return (
    <Suspense>
      <BookDetail params={params} />
    </Suspense>
  );
}
