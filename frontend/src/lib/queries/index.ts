"use client";

import { keepPreviousData, useMutation, useQuery, useQueryClient, type QueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { api, type Query } from "@/lib/api/client";
import { keys } from "@/lib/queries/keys";
import { formatDate, formatMoney } from "@/lib/format";
import type {
  ActivityEntry,
  BookDetail,
  BookInput,
  BookSummary,
  DashboardSummary,
  Eligibility,
  Fine,
  FineSummary,
  GenreCount,
  GenreShare,
  IsbnLookup,
  LanguageRef,
  LedgerEntry,
  LibraryRules,
  Loan,
  MemberDetail,
  MemberInput,
  MemberStatus,
  MemberSummary,
  Page,
  ReturnResult,
  SearchResults,
  SessionInfo,
  StaffAccount,
  ImportReport,
  WaitlistEntry,
  StockOutReason,
  StockResult,
  TopBook,
  TrendPoint,
} from "@/types/api";

/** Circulation touches stock, members, fines, the ledger and the dashboard at once. */
function invalidateCirculation(client: QueryClient) {
  for (const key of [keys.loans.all, keys.books.all, keys.members.all, keys.fines.all, keys.inventory.all, keys.dashboard.all, keys.activity.all]) {
    void client.invalidateQueries({ queryKey: key });
  }
}

// ----------------------------------------------------------------------------------------------- books

export function useBooks(query: Query) {
  return useQuery({
    queryKey: keys.books.list(query),
    queryFn: ({ signal }) => api<Page<BookSummary>>("/books", { query, signal }),
    placeholderData: keepPreviousData,
  });
}

export function useBook(id: number, enabled = true) {
  return useQuery({ queryKey: keys.books.detail(id), queryFn: ({ signal }) => api<BookDetail>(`/books/${id}`, { signal }), enabled: enabled && id > 0 });
}

export function useGenres() {
  return useQuery({ queryKey: keys.books.genres, queryFn: ({ signal }) => api<GenreCount[]>("/books/genres", { signal }), staleTime: 5 * 60_000 });
}

export function useLanguages() {
  return useQuery({
    queryKey: keys.books.languages,
    queryFn: ({ signal }) => api<LanguageRef[]>("/books/languages", { signal }),
    staleTime: Infinity,
  });
}

export function useBookTransactions(id: number) {
  return useQuery({
    queryKey: keys.books.transactions(id),
    queryFn: ({ signal }) => api<Page<LedgerEntry>>(`/books/${id}/transactions`, { query: { size: 100 }, signal }),
  });
}

export function useBookLoans(id: number, query: Query) {
  return useQuery({
    queryKey: keys.books.loans(id, query),
    queryFn: ({ signal }) => api<Page<Loan>>(`/books/${id}/loans`, { query, signal }),
    placeholderData: keepPreviousData,
  });
}

export function lookupIsbn(isbn: string, signal?: AbortSignal) {
  return api<IsbnLookup>("/books/lookup", { query: { isbn }, signal });
}

export function useSaveBook(id?: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: BookInput) =>
      id ? api<BookDetail>(`/books/${id}`, { method: "PUT", body: input }) : api<BookDetail>("/books", { method: "POST", body: input }),
    meta: { silentError: true },
    onSuccess: (book) => {
      client.setQueryData(keys.books.detail(book.id), book);
      void client.invalidateQueries({ queryKey: keys.books.all });
      void client.invalidateQueries({ queryKey: keys.dashboard.all });
      void client.invalidateQueries({ queryKey: keys.inventory.all });
      void client.invalidateQueries({ queryKey: keys.activity.all });
    },
  });
}

export function useDeleteBook() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api<void>(`/books/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.books.all });
      void client.invalidateQueries({ queryKey: keys.dashboard.all });
      toast.success("Book deleted");
    },
  });
}

export function useStockChange(bookId: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: { direction: "in"; quantity: number; reason: string; note?: string } | { direction: "out"; quantity: number; reason: StockOutReason; note?: string }) =>
      api<StockResult>(`/books/${bookId}/stock/${input.direction}`, {
        method: "POST",
        body: { quantity: input.quantity, reason: input.reason, note: input.note || undefined },
      }),
    meta: { silentError: true },
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.books.all });
      void client.invalidateQueries({ queryKey: keys.inventory.all });
      void client.invalidateQueries({ queryKey: keys.dashboard.all });
      void client.invalidateQueries({ queryKey: keys.activity.all });
    },
  });
}

// --------------------------------------------------------------------------------------------- members

export function useMembers(query: Query, enabled = true) {
  return useQuery({
    queryKey: keys.members.list(query),
    queryFn: ({ signal }) => api<Page<MemberSummary>>("/members", { query, signal }),
    placeholderData: keepPreviousData,
    enabled,
  });
}

export function useMember(id: number, enabled = true) {
  return useQuery({ queryKey: keys.members.detail(id), queryFn: ({ signal }) => api<MemberDetail>(`/members/${id}`, { signal }), enabled: enabled && id > 0 });
}

export function useEligibility(id: number | null) {
  return useQuery({
    queryKey: keys.members.eligibility(id ?? 0),
    queryFn: ({ signal }) => api<Eligibility>(`/members/${id}/eligibility`, { signal }),
    enabled: id !== null,
  });
}

export function useMemberLoans(id: number, query: Query) {
  return useQuery({
    queryKey: keys.members.loans(id, query),
    queryFn: ({ signal }) => api<Page<Loan>>(`/members/${id}/loans`, { query, signal }),
    placeholderData: keepPreviousData,
    enabled: id > 0,
  });
}

export function useMemberFines(id: number) {
  return useQuery({
    queryKey: keys.members.fines(id),
    queryFn: ({ signal }) => api<Page<Fine>>("/fines", { query: { memberId: id, size: 50 }, signal }),
  });
}

export function useSaveMember(id?: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: MemberInput) =>
      id ? api<MemberDetail>(`/members/${id}`, { method: "PUT", body: input }) : api<MemberDetail>("/members", { method: "POST", body: input }),
    meta: { silentError: true },
    onSuccess: (member) => {
      client.setQueryData(keys.members.detail(member.id), member);
      void client.invalidateQueries({ queryKey: keys.members.all });
      void client.invalidateQueries({ queryKey: keys.dashboard.all });
      void client.invalidateQueries({ queryKey: keys.activity.all });
    },
  });
}

export function useMemberStatus(id: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (status: MemberStatus) => api<MemberDetail>(`/members/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: (member) => {
      client.setQueryData(keys.members.detail(id), member);
      void client.invalidateQueries({ queryKey: keys.members.all });
      void client.invalidateQueries({ queryKey: keys.activity.all });
      toast.success(`Membership is now ${member.status.toLowerCase()}`);
    },
  });
}

export function useDeleteMember() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api<void>(`/members/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.members.all });
      toast.success("Member removed");
    },
  });
}

// ----------------------------------------------------------------------------------------- circulation

export function useLoans(query: Query, enabled = true) {
  return useQuery({
    queryKey: keys.loans.list(query),
    queryFn: ({ signal }) => api<Page<Loan>>("/loans", { query, signal }),
    placeholderData: keepPreviousData,
    enabled,
  });
}

export function useIssueBook() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: { memberId: number; bookId: number }) => api<Loan>("/loans/issue", { method: "POST", body: input }),
    meta: { silentError: true },
    onSuccess: () => invalidateCirculation(client),
  });
}

export function useReturnLoan() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (loanId: number) => api<ReturnResult>(`/loans/${loanId}/return`, { method: "POST" }),
    onSuccess: () => invalidateCirculation(client),
  });
}

export function useRenewLoan() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (loanId: number) => api<Loan>(`/loans/${loanId}/renew`, { method: "POST" }),
    onSuccess: (loan) => {
      invalidateCirculation(client);
      toast.success(`Renewed until ${formatDate(loan.dueDate)}`);
    },
  });
}

export function useFines(query: Query) {
  return useQuery({
    queryKey: keys.fines.list(query),
    queryFn: ({ signal }) => api<Page<Fine>>("/fines", { query, signal }),
    placeholderData: keepPreviousData,
  });
}

export function useFineSummary() {
  return useQuery({ queryKey: keys.fines.summary, queryFn: ({ signal }) => api<FineSummary>("/fines/summary", { signal }) });
}

export function usePayFine() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: { id: number; note?: string }) => api<Fine>(`/fines/${input.id}/pay`, { method: "POST", body: { note: input.note } }),
    onSuccess: (fine) => {
      invalidateCirculation(client);
      toast.success(`Payment of ${formatMoney(fine.amount)} recorded`);
    },
  });
}

export function useWaiveFine() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: { id: number; reason: string }) => api<Fine>(`/fines/${input.id}/waive`, { method: "POST", body: { reason: input.reason } }),
    meta: { silentError: true },
    onSuccess: () => {
      invalidateCirculation(client);
      toast.success("Fine waived");
    },
  });
}

// ------------------------------------------------------------------------------- ledger, activity, etc.

export function useLedger(query: Query) {
  return useQuery({
    queryKey: keys.inventory.list(query),
    queryFn: ({ signal }) => api<Page<LedgerEntry>>("/inventory/transactions", { query, signal }),
    placeholderData: keepPreviousData,
  });
}

export function useActivity(query: Query) {
  return useQuery({
    queryKey: keys.activity.list(query),
    queryFn: ({ signal }) => api<Page<ActivityEntry>>("/activity", { query, signal }),
    placeholderData: keepPreviousData,
  });
}

export function useStaff() {
  return useQuery({ queryKey: keys.staff, queryFn: ({ signal }) => api<StaffAccount[]>("/staff", { signal }), staleTime: 60_000 });
}

export function useSaveStaff(id?: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: { name: string; email?: string; role: string; active?: boolean; temporaryPassword?: string }) =>
      id ? api<StaffAccount>(`/staff/${id}`, { method: "PUT", body: input }) : api<StaffAccount>("/staff", { method: "POST", body: input }),
    meta: { silentError: true },
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.staff });
      void client.invalidateQueries({ queryKey: keys.activity.all });
    },
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (input: { id: number; temporaryPassword: string }) =>
      api<void>(`/staff/${input.id}/reset-password`, { method: "POST", body: { temporaryPassword: input.temporaryPassword } }),
    meta: { silentError: true },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (input: { currentPassword: string; newPassword: string }) => api<void>("/auth/change-password", { method: "POST", body: input }),
    meta: { silentError: true },
  });
}

export function useBookWaitlist(bookId: number) {
  return useQuery({ queryKey: ["books", "detail", bookId, "waitlist"], queryFn: ({ signal }) => api<WaitlistEntry[]>(`/books/${bookId}/waitlist`, { signal }) });
}

export function useMemberWaitlist(memberId: number) {
  return useQuery({ queryKey: ["members", "detail", memberId, "waitlist"], queryFn: ({ signal }) => api<WaitlistEntry[]>(`/members/${memberId}/waitlist`, { signal }) });
}

export function useJoinWaitlist(bookId: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (memberId: number) => api<WaitlistEntry>(`/books/${bookId}/waitlist`, { method: "POST", body: { memberId } }),
    onSuccess: (entry) => {
      void client.invalidateQueries({ queryKey: keys.books.detail(bookId) });
      void client.invalidateQueries({ queryKey: keys.members.all });
      toast.success(`${entry.member.fullName} is #${entry.position} in the queue`);
    },
  });
}

export function useCancelWaitlist() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (entryId: number) => api<void>(`/waitlist/${entryId}`, { method: "DELETE" }),
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.books.all });
      void client.invalidateQueries({ queryKey: keys.members.all });
    },
  });
}

export async function importBooks(file: File, dryRun: boolean): Promise<ImportReport> {
  const form = new FormData();
  form.append("file", file);
  return api<ImportReport>("/books/import", { method: "POST", query: { dryRun }, formData: form });
}

export function useDashboard() {
  const summary = useQuery({
    queryKey: [...keys.dashboard.all, "summary"],
    queryFn: ({ signal }) => api<DashboardSummary>("/dashboard/summary", { signal }),
  });
  const trend = useQuery({
    queryKey: [...keys.dashboard.all, "trend"],
    queryFn: ({ signal }) => api<TrendPoint[]>("/dashboard/loan-trend", { query: { days: 30 }, signal }),
  });
  const topBooks = useQuery({
    queryKey: [...keys.dashboard.all, "top"],
    queryFn: ({ signal }) => api<TopBook[]>("/dashboard/top-books", { query: { limit: 5 }, signal }),
  });
  const genres = useQuery({
    queryKey: [...keys.dashboard.all, "genres"],
    queryFn: ({ signal }) => api<GenreShare[]>("/dashboard/genre-distribution", { signal }),
  });
  return { summary, trend, topBooks, genres };
}

export function useSettings() {
  return useQuery({ queryKey: keys.settings, queryFn: ({ signal }) => api<LibraryRules>("/settings", { signal }) });
}

export function useSaveSettings() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: Omit<LibraryRules, "updatedAt">) => api<LibraryRules>("/settings", { method: "PUT", body: input }),
    meta: { silentError: true },
    onSuccess: (rules) => {
      client.setQueryData(keys.settings, rules);
      void client.invalidateQueries({ queryKey: keys.activity.all });
      toast.success("Library rules saved");
    },
  });
}

export function useSessions() {
  return useQuery({ queryKey: keys.sessions, queryFn: ({ signal }) => api<SessionInfo[]>("/auth/sessions", { signal }) });
}

export function useRevokeSession() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api<void>(`/auth/sessions/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      void client.invalidateQueries({ queryKey: keys.sessions });
      toast.success("Session signed out");
    },
  });
}

export function useSearch(q: string) {
  return useQuery({
    queryKey: keys.search(q),
    queryFn: ({ signal }) => api<SearchResults>("/search", { query: { q }, signal }),
    enabled: q.trim().length > 0,
    placeholderData: keepPreviousData,
    staleTime: 15_000,
  });
}
