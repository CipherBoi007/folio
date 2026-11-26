export type Role = "ADMIN" | "LIBRARIAN";

export interface StaffUser {
  id: number;
  name: string;
  email: string;
  role: Role;
  mustChangePassword: boolean;
}

export interface StaffAccount extends StaffUser {
  active: boolean;
  lastLoginAt: string | null;
}

export interface WaitlistEntry {
  id: number;
  position: number;
  bookId: number;
  bookTitle: string;
  member: MemberRef;
  createdAt: string;
}

export interface ImportReport {
  dryRun: boolean;
  rows: number;
  created: number;
  failed: number;
  results: { row: number; isbn: string; title: string; status: "READY" | "CREATED" | "FAILED"; message: string | null }[];
}

export interface TokenResponse {
  accessToken: string;
  expiresAt: string;
  user: StaffUser;
}

export interface SessionInfo {
  id: string;
  createdAt: string;
  lastUsedAt: string;
  expiresAt: string;
  ipAddress: string | null;
  userAgent: string | null;
  current: boolean;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface FieldErrorItem {
  field: string;
  message: string;
}

export interface ProblemBody {
  status: number;
  code: string;
  message: string;
  fieldErrors?: FieldErrorItem[];
  requestId?: string;
  details?: Record<string, unknown>;
}

// Catalogue

export interface Ref {
  id: number;
  name: string;
}

export interface LanguageRef {
  code: string;
  name: string;
}

export interface GenreCount extends Ref {
  titles: number;
}

export type Availability = "IN_STOCK" | "LOW" | "OUT";

export interface BookSummary {
  id: number;
  isbn13: string;
  title: string;
  subtitle: string | null;
  authors: string[];
  genre: Ref;
  language: LanguageRef;
  publisher: string | null;
  publishedYear: number | null;
  shelfLocation: string | null;
  coverUrl: string | null;
  totalQuantity: number;
  availableQuantity: number;
}

export interface BookDetail extends BookSummary {
  pages: number | null;
  description: string | null;
  activeLoans: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface BookInput {
  isbn: string;
  title: string;
  subtitle?: string | null;
  authors: string[];
  genre: string;
  publisher?: string | null;
  languageCode: string;
  publishedYear?: number | null;
  pages?: number | null;
  shelfLocation?: string | null;
  description?: string | null;
  coverUrl?: string | null;
  initialQuantity?: number;
  version?: number;
}

export interface IsbnLookup {
  isbn13: string;
  found: boolean;
  existingBookId: number | null;
  title: string | null;
  subtitle: string | null;
  authors: string[];
  publisher: string | null;
  publishedYear: number | null;
  pages: number | null;
  coverUrl: string | null;
  subjects: string[];
}

export interface StockResult {
  bookId: number;
  totalQuantity: number;
  availableQuantity: number;
}

export type StockOutReason = "DAMAGED" | "LOST" | "WITHDRAWN";

// Inventory

export type TransactionType = "INITIAL_STOCK" | "STOCK_IN" | "STOCK_OUT" | "ISSUE" | "RETURN";

export interface LedgerEntry {
  id: number;
  book: { id: number; title: string; isbn13: string };
  type: TransactionType;
  quantityChange: number;
  totalBefore: number;
  totalAfter: number;
  availableBefore: number;
  availableAfter: number;
  reason: string | null;
  note: string | null;
  loanId: number | null;
  performedBy: { id: number; name: string };
  performedAt: string;
}

// Members

export type MemberStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

export interface MemberSummary {
  id: number;
  membershipNumber: string;
  fullName: string;
  email: string;
  phone: string;
  city: string | null;
  status: MemberStatus;
  joinedOn: string;
  activeLoans: number;
  overdueLoans: number;
  unpaidFines: number;
}

export interface MemberDetail extends MemberSummary {
  addressLine: string | null;
  postalCode: string | null;
  totalLoans: number;
  createdAt: string;
  updatedAt: string;
}

export interface MemberInput {
  fullName: string;
  email: string;
  phone: string;
  addressLine?: string | null;
  city?: string | null;
  postalCode?: string | null;
}

export interface Eligibility {
  canBorrow: boolean;
  activeLoans: number;
  maxLoans: number;
  remainingSlots: number;
  overdueLoans: number;
  unpaidFines: number;
  fineLimit: number;
  loanDays: number;
  dueDateIfIssuedToday: string;
  reasons: { code: string; message: string }[];
}

// Circulation

export type LoanStatus = "ACTIVE" | "RETURNED";
export type FineStatus = "UNPAID" | "PAID" | "WAIVED";

export interface BookRef {
  id: number;
  title: string;
  isbn13: string;
  authors: string[];
  coverUrl: string | null;
}

export interface MemberRef {
  id: number;
  fullName: string;
  membershipNumber: string;
}

export interface StaffRef {
  id: number;
  name: string;
}

export interface Loan {
  id: number;
  book: BookRef;
  member: MemberRef;
  issuedAt: string;
  dueDate: string;
  returnedAt: string | null;
  renewalCount: number;
  status: LoanStatus;
  overdue: boolean;
  dueToday: boolean;
  overdueDays: number;
  projectedFine: number | null;
  renewable: boolean;
  issuedBy: StaffRef;
  returnedBy: StaffRef | null;
  fine: { id: number; amount: number; status: FineStatus } | null;
}

export interface Fine {
  id: number;
  loanId: number;
  member: MemberRef;
  book: BookRef;
  overdueDays: number;
  dailyRate: number;
  amount: number;
  status: FineStatus;
  createdAt: string;
  resolvedAt: string | null;
  resolvedBy: StaffRef | null;
  resolutionNote: string | null;
}

export interface FineSummary {
  unpaid: { count: number; amount: number };
  paid: { count: number; amount: number };
  waived: { count: number; amount: number };
}

export interface ReturnResult {
  loan: Loan;
  fine: Fine | null;
  nextInQueue: MemberRef | null;
}

// Dashboard, activity, settings

export interface DashboardSummary {
  titles: number;
  copies: number;
  copiesOnShelf: number;
  onLoan: number;
  overdue: number;
  dueToday: number;
  unpaidFines: number;
  unpaidFineCount: number;
  activeMembers: number;
  issuedThisWeek: number;
  returnedThisWeek: number;
  titlesAddedThisWeek: number;
  membersJoinedThisWeek: number;
}

export interface TrendPoint {
  date: string;
  issued: number;
  returned: number;
}

export interface TopBook {
  id: number;
  title: string;
  authors: string | null;
  isbn13: string;
  coverUrl: string | null;
  loans: number;
}

export interface GenreShare {
  id: number;
  name: string;
  titles: number;
  copies: number;
}

export interface ActivityEntry {
  id: number;
  action: string;
  entityType: "BOOK" | "MEMBER" | "LOAN" | "FINE" | "SETTINGS" | "STAFF";
  entityId: number | null;
  summary: string;
  metadata: Record<string, unknown>;
  createdAt: string;
  actor: { id: number; name: string; role: Role };
}

export interface LibraryRules {
  loanDays: number;
  renewalDays: number;
  maxActiveLoans: number;
  finePerDay: number;
  fineBlockThreshold: number;
  updatedAt: string;
}

export interface SearchResults {
  books: BookSummary[];
  members: MemberSummary[];
}
