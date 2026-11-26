# CLAUDE.md — "Folio" · Library Management System

The original brief is in `docs/assessment.pdf`. This file is the source of truth for **what** to build and **how**.
We are building a **real, shippable product** — not a CRUD demo. It must look and feel like a commercial SaaS tool
(think Linear, Stripe Dashboard, Notion), with rock-solid business logic underneath.

Time budget: 3 hours. Work in the phases at the bottom. After each phase the app compiles, runs, and is committed.
**Priority order is non-negotiable:** brief's business rules (tested) → product features → polish.

---

## 1. Product

**Folio** — a library operations console for librarians.
Tagline: *"Every copy, accounted for."*

Users: `ADMIN` (head librarian) and `LIBRARIAN` (desk staff) — these are the only accounts that log in.
Members do **not** log in: Folio is a staff-operated desk tool, and librarians issue and return books on a member's behalf.
(The brief requires no auth at all; staff login is added for realism and auditability. A member self-service portal is out of scope — list it under "Future work" in the README.)

### Local database setup (dev machine has PostgreSQL installed locally)

```sql
-- run once in psql as the postgres superuser
CREATE USER folio WITH PASSWORD 'folio';
CREATE DATABASE folio OWNER folio;
CREATE DATABASE folio_test OWNER folio;   -- used by integration tests when Docker is unavailable
```

`application-local.yml` defaults: `jdbc:postgresql://localhost:5432/folio`, user `folio`, password `folio`, all overridable via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Flyway creates the tables and seed data on first start — never create tables by hand.

### Feature tiers

**Tier 1 — Brief (must be perfect):** books CRUD + search/filter/sort, inventory in/out + history, member management, issue, return, all business rules R1–R7.

**Tier 2 — Product layer (must ship):**
- Auth: JWT login, roles, demo-login buttons on the login page, `performed_by` recorded on every action
- Renewals (rules below)
- Overdue fines (rules below)
- ISBN auto-fill: typing a valid ISBN in "Add book" fetches title/author/publisher/year/cover from Open Library
- Book covers (Open Library cover by ISBN, with a generated typographic cover as a fallback)
- Dashboard with KPIs, charts, "Due today", "Overdue", "Recent activity"
- Command palette (⌘K / Ctrl+K): search books and members, jump to pages, quick actions ("Issue book", "Add member")
- Activity log: who did what, when (every write action)
- CSV export of books, members, loans, transactions
- Dark mode ("Reading room")

**Tier 3 — Delight (only if Tiers 1–2 are done):**
- Waitlist: reserve an out-of-stock book; on return, show "Next in queue: <member>" (no automatic stock hold)
- Printable member card with a QR code of the membership number
- CSV import of books with a row-level validation report
- Admin settings page (loan days, max loans, fine per day, fine block threshold) stored in a `library_settings` table
- Keyboard shortcuts: `/` focuses search, `I` issue, `R` return, `N` new (context-aware), `?` shows the shortcut sheet

Anything unfinished goes under **"Incomplete / Known limitations"** in the README (the brief requires this).

---

## 2. Stack (do not change)

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot (latest stable via start.spring.io), Maven, Web, Data JPA, Validation, Security, Flyway, Actuator, springdoc-openapi, jjwt (or Spring OAuth2 resource server with HS256) |
| DB | PostgreSQL 16 — **locally installed Postgres** (no Docker needed for dev), prod Supabase. `docker-compose.yml` is provided only as an optional alternative for reviewers |
| Frontend | Next.js (latest stable, App Router, TS strict), Tailwind, shadcn/ui (heavily re-themed, see §8), TanStack Query + TanStack Table, react-hook-form + zod, Recharts, cmdk, sonner, lucide-react, next-themes, date-fns |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers (if Docker available) |
| Hosting | Vercel (frontend) · Render via Docker (backend) · Supabase (DB) |
| CI | GitHub Actions: `mvn verify`, `npm run lint && npm run typecheck && npm run build` |

Use the latest stable versions from the official generators. Do not pin versions from memory.

---

## 3. Repository layout

```
folio/
├── CLAUDE.md
├── README.md                 # see §11
├── docker-compose.yml        # OPTIONAL: postgres for reviewers without a local install
├── .env.example
├── .github/workflows/ci.yml
├── docs/
│   ├── assessment.pdf
│   ├── DECISIONS.md          # assumptions + design decisions
│   ├── API.md                # short endpoint table (+ link to Swagger)
│   └── screenshots/          # added at the end for the README
├── backend/
│   ├── Dockerfile            # multi-stage: maven → eclipse-temurin:21-jre, non-root user
│   └── src/main/java/com/folio/
│       ├── common/           # errors, ProblemDetail handler, PageResponse, Clock, CORS, OpenAPI, request-id filter
│       ├── auth/             # User, JWT, SecurityConfig, AuthController
│       ├── book/             # + IsbnValidator, CSV export
│       ├── member/
│       ├── inventory/        # append-only transactions
│       ├── loan/             # issue, return, renew
│       ├── fine/
│       ├── activity/         # activity log (who/what/when)
│       ├── dashboard/
│       └── settings/         # Tier 3
│   └── src/main/resources/db/migration/  V1__schema.sql, V2__seed.sql, ...
└── frontend/src/
    ├── app/(auth)/login
    ├── app/(console)/        # layout with sidebar; dashboard, books, books/[id], members, members/[id],
    │                         # circulation, inventory, fines, activity, settings
    ├── components/ui/        # shadcn, re-themed
    ├── components/<feature>/
    ├── lib/                  # api client, auth, query keys, zod schemas, format helpers
    └── types/
```

The backend is package-by-feature: thin controllers, all rules in services, entities never leave the service layer (DTO records only).

---

## 4. Business rules

Every rule = service check **+** DB constraint where possible **+** unit test **+** specific error code.

### From the brief (R1–R7)

| # | Rule | Enforcement | Error |
|---|---|---|---|
| R1 | Unique ISBN | Normalize (strip spaces/hyphens, uppercase X); validate the ISBN-10/13 checksum; store as ISBN-13 (convert ISBN-10); service check + `UNIQUE` | 409 `DUPLICATE_ISBN` / 400 `INVALID_ISBN` |
| R2 | available ≤ total | `CHECK` + service | 409 `STOCK_LIMIT_EXCEEDED` |
| R3 | No negative stock | `CHECK (>= 0)` on both + service | 409 `INSUFFICIENT_STOCK` |
| R4 | Max 3 active loans per member | Count `ISSUED` loans inside the transaction with the member row locked | 409 `BORROW_LIMIT_REACHED` |
| R5 | Loan = 14 days | `due_date = issue_date + 14` from an injected `Clock` (zone `Asia/Kolkata`); configurable, default 14 | — |
| R6 | Issue/return update stock | Issue −1 available, return +1, in the same transaction as the loan | — |
| R7 | Every stock change logged | Exactly one `inventory_transactions` row per change, in the same transaction; append-only | — |

### Derived rules

- **Quantities change only via the inventory/loan services.** `PUT /books/{id}` edits metadata only.
- Creating a book with `initialQuantity > 0` logs `INITIAL_STOCK`.
- `STOCK_IN`: q ≥ 1, total += q, available += q, reason required.
- `STOCK_OUT` (damaged/lost/withdrawn — reason is an enum + an optional note): q ≥ 1, requires `available ≥ q` (copies on loan can't be removed), both quantities −= q. ADMIN only.
- **Issue checks**, in order: member exists + `ACTIVE` (`MEMBER_INACTIVE`) → book exists, not deleted → has no overdue loans (`HAS_OVERDUE_LOANS`) → unpaid fines ≤ threshold (default ₹100, `FINE_LIMIT_EXCEEDED`) → active loans < 3 (`BORROW_LIMIT_REACHED`) → not already holding this book (`ALREADY_BORROWED`, backed by a partial unique index) → available > 0 (`OUT_OF_STOCK`).
- `GET /members/{id}/eligibility` returns `{ canBorrow, activeLoans, remainingSlots, reasons[] }` — the UI uses this before issuing.
- **Return:** loan exists, status `ISSUED` (`ALREADY_RETURNED`); available + 1 ≤ total (defensive). Logs `RETURN` with `loan_id`. If overdue → create a fine automatically.
- **Renew:** status `ISSUED`, not overdue (`LOAN_OVERDUE`), `renewal_count < 1` (`RENEWAL_LIMIT_REACHED`), no one on the waitlist for the book (`BOOK_RESERVED`, Tier 3). New due date = current due date + 14. No stock change, so no inventory row; logged in the activity log.
- **Fines:** ₹5 per overdue day (configurable), created on return. Status `UNPAID | PAID | WAIVED`. Pay = ADMIN or LIBRARIAN; waive = ADMIN only, reason required. Amounts are `numeric(10,2)` / `BigDecimal`.
- **Overdue** is derived (`ISSUED AND due_date < today`), never stored.
- **Concurrency:** issue/return/renew/stock changes load the book (and member) with `PESSIMISTIC_WRITE` inside `@Transactional`; `@Version` on `Book`; DB CHECKs as the last line of defence. Add a test that fires two concurrent issues for the last copy and asserts exactly one succeeds (Testcontainers; skip with a note if Docker is unavailable).
- **Delete book:** blocked with active loans (`BOOK_HAS_ACTIVE_LOANS`); otherwise soft delete; the ISBN stays reserved. ADMIN only.
- **Member:** unique email (lowercased), auto `membership_number` `FOL-2026-0001`, Indian 10-digit mobile validation, status `ACTIVE | INACTIVE | SUSPENDED`; deactivating or deleting with active loans is blocked (`MEMBER_HAS_ACTIVE_LOANS`); soft delete.
- Trim inputs, whitelist sort fields, page size ≤ 100.

All assumptions go in `docs/DECISIONS.md`.

---

## 5. Schema (Flyway)

- `users` (id, name, email unique, password_hash bcrypt, role, created_at)
- `books` (id, isbn13 unique, title, subtitle, author, publisher, published_year, genre, language, pages, shelf_location, description, cover_url, total_quantity, available_quantity, version, created_at, updated_at, deleted_at) + CHECKs + indexes on lower(title), lower(author), genre
- `members` (id, membership_number unique, full_name, email unique, phone, address, status, joined_at, created_at, updated_at, deleted_at)
- `loans` (id, book_id, member_id, issued_at, due_date, returned_at, status, renewal_count, issued_by, returned_by) + partial unique index `(member_id, book_id) WHERE status='ISSUED'`
- `inventory_transactions` (id, book_id, type `INITIAL_STOCK|STOCK_IN|STOCK_OUT|ISSUE|RETURN`, quantity_change, total_before, total_after, available_before, available_after, reason, note, loan_id, performed_by, performed_at)
- `fines` (id, loan_id unique, member_id, amount, overdue_days, status, paid_at, waived_reason, resolved_by, created_at)
- `activity_log` (id, actor_id, action, entity_type, entity_id, summary, metadata jsonb, created_at)
- `waitlist` (Tier 3), `library_settings` (Tier 3)

`ddl-auto: validate`. Timestamps `timestamptz`/`Instant`; due dates `date`/`LocalDate`.

**Seed (`V2__seed.sql`) — must feel real and be internally consistent:**
- 2 users: `admin@folio.dev / Admin@123` (ADMIN), `desk@folio.dev / Desk@123` (LIBRARIAN) — bcrypt hashes
- ~40 books with **real ISBN-13s** that exist on Open Library (so covers load): a mix of Indian literature (R.K. Narayan, Arundhati Roy, Amitav Ghosh, Kalki, Perumal Murugan, A.P.J. Abdul Kalam), classics, tech (Clean Code, DDIA, Effective Java), and popular non-fiction. Varied genres, shelf codes like `A-03`, a few with 0 available and a few with low stock.
- ~25 members with Indian names (Ramanathapuram/Madurai/Chennai addresses), 1 INACTIVE, 1 SUSPENDED
- Loans over the last 60 days: ~15 active (one member at exactly 3, ~4 overdue, 2 due today), ~30 returned (some late → fines, some paid, one waived), a couple renewed
- Inventory transactions and activity log rows that **add up exactly** to the current quantities. Write a quick consistency query in a test to verify this.

---

## 6. API (`/api/v1`, all except auth require a JWT)

```
POST /auth/login → { token, user }          GET /auth/me
GET/POST /books   GET/PUT/DELETE /books/{id}   GET /books/genres   GET /books/export.csv
POST /books/{id}/stock/in | /stock/out       GET /books/{id}/transactions   GET /books/{id}/loans
GET  /inventory/transactions?bookId&type&from&to   GET /inventory/transactions/export.csv
GET/POST /members  GET/PUT/DELETE /members/{id}  PATCH /members/{id}/status
GET  /members/{id}/eligibility   GET /members/{id}/loans   GET /members/export.csv
POST /loans/issue   POST /loans/{id}/return   POST /loans/{id}/renew
GET  /loans?memberId&bookId&status&overdue&dueToday   GET /loans/export.csv
GET  /fines?status&memberId   POST /fines/{id}/pay   POST /fines/{id}/waive
GET  /activity?actorId&entityType&from&to
GET  /dashboard/summary   GET /dashboard/loan-trend?days=30   GET /dashboard/top-books   GET /dashboard/genre-distribution
GET  /search?q=   (unified: books + members, top 5 each — for the command palette)
```

- Paged shape: `{ content, page, size, totalElements, totalPages }`.
- Errors: RFC 7807 `ProblemDetail` + `code`, `message` (human, shown as-is in the UI), `fieldErrors`, `timestamp`, `requestId`. 400 validation / 401 / 403 / 404 / 409 rules / 500 generic (log the stack trace, never leak it).
- Security: stateless JWT (HS256, secret from env, 8h expiry), BCrypt, method-level `@PreAuthorize` for ADMIN-only actions, CORS from env, Swagger at `/swagger-ui.html` (with a bearer auth button), `/actuator/health` public.
- Logging: request-id MDC filter (header `X-Request-Id`), INFO log per business action.

---

## 7. Tests (business rules first)

Unit tests (fixed `Clock`) for every rule in §4: ISBN checksum/normalize/duplicate; stock in/out limits; a transaction row on every change; every issue rejection code; issue success (stock, due date, log); return (already returned, stock restored, fine created with the correct amount); renew (overdue, limit); delete with active loans.
Integration (MockMvc against **real PostgreSQL** — never H2, since partial indexes, CHECKs and `FOR UPDATE` must behave exactly like prod). Use Testcontainers when Docker is available; otherwise fall back to the local `folio_test` database via a `test` profile (`TEST_DB_URL` env var, Flyway `clean` + `migrate` before the suite). Tests: issue → renew → return happy path; the concurrent last-copy test; the seed consistency check; 401/403 checks. CI (GitHub Actions) uses a `postgres:16` service container with the same `test` profile.

---

## 8. Design system — "Folio" (read carefully: this is what makes or breaks the product)

**Goal:** calm, editorial, precise. It should look like a well-funded product team designed it, **not** like an AI template.

### Banned (the "AI look")
No gradients (backgrounds, text, or buttons). No glassmorphism or backdrop blur cards. No purple/indigo/violet. No emoji in the UI. No neon or saturated multi-colour palettes. No `rounded-2xl`/`rounded-3xl` cards. No large drop shadows. No centered hero-style headings inside the app. No "Welcome back! 👋" copy. No icon-in-a-coloured-circle KPI cards. No generic sparkle/AI icons. No lorem ipsum.

### Typography
- Display/headings: **Fraunces** (variable serif; `opsz` high, weight 500–600, slight negative tracking) — page titles, book titles on detail pages, KPI numbers, empty-state headlines.
- UI/body: **Inter** (or Geist Sans), 14px base, 13px in dense tables.
- Data: **JetBrains Mono** (or Geist Mono) for ISBNs, membership numbers, shelf codes, amounts, dates in tables. Use `tabular-nums` everywhere numbers align.
- Load via `next/font`. Use a clear type scale: 12 / 13 / 14 / 16 / 20 / 28 / 40.

### Colour (CSS variables, mapped into the shadcn theme tokens)
Light — "Paper":
- `--bg` #F6F4EF (warm paper) · `--surface` #FFFFFF · `--surface-muted` #EFECE5
- `--border` #E3DFD6 (hairline, 1px) · `--text` #1B1A17 (ink) · `--text-muted` #6B675E
- `--accent` #1F4D3A (library green) · `--accent-hover` #173B2C · `--accent-soft` #E4EDE7
- Semantic, muted: success #2F6B4F · warning #A86A12 · danger #A33A2B (oxblood) · info #2C5A7A — each with a soft tinted background for badges

Dark — "Reading room": `--bg` #121211 · `--surface` #1A1A18 · `--border` #2A2925 · `--text` #EDEAE3 · `--text-muted` #9A958A · `--accent` #6FAF8E. Keep it warm, never pure black or blue-grey.

### Shape, depth, motion
- Radius: 6px for inputs/buttons, 8px for cards/dialogs, 4px for badges. Nothing bigger.
- Depth comes from **1px borders**, not shadows. The only shadow: a soft one on popovers/dialogs/command palette.
- Motion: 150–200ms ease-out; fade/slide 4px for dialogs and toasts; no bouncy animations. Respect `prefers-reduced-motion`.
- Spacing on a 4px grid. Content max-width 1280px. Generous page padding (32px desktop).

### Layout
- **Sidebar** (240px, collapsible to 64px icons): the wordmark "Folio" in Fraunces at the top; nav groups *Overview* (Dashboard), *Collection* (Books, Inventory), *People* (Members), *Desk* (Circulation, Fines), *Admin* (Activity, Settings); user menu with role badge and a theme toggle at the bottom. Active item = accent-soft background + accent text, no left bar gimmicks.
- **Top bar:** breadcrumb, a ⌘K search trigger (looks like a search input showing the shortcut hint), primary action button on the right.
- **Page header:** Fraunces title (28px), a one-line muted description, actions on the right.

### Components
- **Tables** (TanStack Table): dense rows (44px), sticky header in muted small-caps-style labels (12px, uppercase, letter-spacing 0.04em, muted), hairline row dividers, row hover = surface-muted, sortable headers with subtle arrows, column visibility menu, pagination footer "1–20 of 143". Numbers right-aligned, mono.
- **Badges:** small, 4px radius, soft tinted background + darker text, an optional 6px dot. Language: `Available`, `Low stock`, `Out of stock`, `Issued`, `Due today`, `Overdue · 3d`, `Returned`, `Renewed`, `Active`, `Inactive`, `Suspended`, `Unpaid ₹45`, `Paid`, `Waived`.
- **Stock meter:** a thin 4px bar showing available/total plus `3 / 5` in mono — used in tables and on the book page.
- **Book cover component:** Open Library cover (`https://covers.openlibrary.org/b/isbn/{isbn}-M.jpg?default=false`) at a 2:3 ratio, 4px radius, hairline border. On failure → a **generated typographic cover**: a muted solid colour picked deterministically from the ISBN hash (from a palette of 8 earthy tones: moss, clay, slate, ochre, plum-brown, teal-grey, sand, ink), with the title in Fraunces and the author in small caps. No gradients.
- **KPI cards:** label (12px muted), big number in Fraunces 40px tabular, a small delta line ("+12 this week") in muted text. White surface, hairline border, no icons in circles.
- **Charts** (Recharts): accent green as the single primary series, muted ink greys for secondary; hairline gridlines; no legends when there is one series; custom tooltip matching the popover style.
- **Empty states:** a short Fraunces headline + a one-line explanation + a primary action. E.g. "No overdue loans. The shelves thank you."
- **Toasts** (sonner): success uses neutral styling with an accent check; errors show the backend `message` verbatim.
- **Forms:** labels above inputs, helper text muted, inline errors in danger colour, a sticky footer with Cancel / Save in sheets.
- **Detail views:** books and members open in a dedicated page (not a modal); create/edit happens in a right-side **Sheet**.

### Copy voice
Confident, plain, librarian-professional. Sentence case everywhere. "Issue book", "Record return", "Add copies", "Remove copies", "Renew loan". Error messages say what happened and what to do: "Priya Raman already has 3 books on loan. Return one before issuing another."

---

## 9. Screens

1. **Login** — split layout: left half a calm panel in accent green with the Fraunces wordmark, the tagline, and a quiet line-art book illustration (SVG, no stock photos); right half the form + two "Continue as demo Admin / Librarian" buttons.
2. **Dashboard** — greeting line in muted text ("Thursday, 26 November"), 5 KPI cards (Titles, Copies on shelf, On loan, Overdue, Unpaid fines ₹), loan-trend chart (30 days, issues vs returns), top 5 borrowed books (with covers), genre distribution (horizontal bars), "Due today" and "Overdue" lists with one-click Return, recent activity feed.
3. **Books** — toolbar: search, genre multi-select, availability, language, sort, a **table/grid view toggle** (the grid shows covers like a bookshelf), Export CSV, Add book. The Add book sheet has the ISBN field first → auto-fill from Open Library with a cover preview, then editable fields + initial quantity.
4. **Book detail** — left: large cover; right: title (Fraunces 40), author, metadata grid (ISBN mono, publisher, year, pages, language, shelf), stock card (meter + "Add copies" / "Remove copies"), tabs: *Current loans*, *Loan history*, *Inventory history* (a vertical timeline: type badge, signed qty, before→after, reason, who, when).
5. **Members** — table with avatar initials (muted tint), name, membership no. (mono), contact, status, a `●●○ 2/3` loan-slot indicator, fines. The Add member sheet.
6. **Member detail** — profile header with status control, eligibility panel ("Can borrow · 1 slot left" or the reasons it can't), active loans with Renew/Return, loan history, fines with Pay/Waive, printable card (Tier 3).
7. **Circulation** — the hero workflow, built like a checkout desk:
   - *Issue:* member combobox (search name/phone/membership no.) → eligibility card → book combobox (cover thumbnails, availability, out-of-stock disabled with the reason) → a receipt-style summary card ("Due Thu, 10 Dec 2026 · 14 days") → **Issue book**. After success: a receipt view with "Issue another" / "Print slip".
   - *Return:* search by member/book/ISBN/loan → a loan card showing the overdue status and the calculated fine before confirming → **Record return** → a result showing the fine created, if any.
   - *Renew* is available from the loan rows.
8. **Inventory** — global ledger table with filters (book, type, date range, user), signed quantities coloured (+ success, − danger), before → after in mono, Export CSV.
9. **Fines** — tabs Unpaid / Paid / Waived, totals at the top, Pay / Waive (waive requires a reason, ADMIN only).
10. **Activity** — an audit feed grouped by day, filter by user/entity.
11. **Command palette** — groups: Books (with covers), Members, Actions, Navigation; arrow-key navigation; enter opens.

Frontend engineering rules: TanStack Query for all server state; URL-synced filters/sort/page; 300ms debounced search; skeleton loaders matching the final layout (no spinners for page loads); empty + error states with retry; precise query invalidation after mutations; confirm dialogs for destructive actions; role-aware UI (hide or disable ADMIN-only actions with a tooltip explaining why); responsive down to tablet (the sidebar becomes a sheet on mobile); accessible (focus rings in accent, aria labels, keyboard-navigable combobox/palette, colour contrast AA); no `any`; format dates as `26 Nov 2026` and money as `₹1,250.00` (en-IN).

---

## 10. Deployment

- **Render:** Docker deploy, root `backend`, health check `/actuator/health`, env `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`. `server.port=${PORT:8080}`. The free tier sleeps → note the cold start in the README.
- **Supabase:** use the **Session pooler** string (port 5432; the direct host is IPv6 and unreachable from Render): `jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require`, user `postgres.<project-ref>`. Hikari pool size 5. Avoid the transaction pooler (6543) with Hibernate; if it's unavoidable, add `prepareThreshold=0`.
- **Vercel:** root `frontend`, `NEXT_PUBLIC_API_URL=https://<api>.onrender.com/api/v1`. Add `covers.openlibrary.org` to `next.config` `images.remotePatterns`.
- Deploy the skeleton at the end of Phase 1 so deploy problems surface early.

---

## 11. README (graders read this first)

Product name + one-line pitch, **live demo links + demo credentials** (state clearly: staff accounts only; members are records, not users), 3–4 screenshots, feature list mapped to the brief's checklist (✅), business rules and how each is enforced (a table), architecture diagram (Mermaid), tech stack, local setup (prerequisites: Java 21, Node 20+, PostgreSQL 16 — the SQL to create the user/databases, then the run commands; Docker Compose as an alternative), env var table, running tests, deployment notes, assumptions (link to DECISIONS.md), **Incomplete / Known limitations**.

---

## 12. Phases (commit after each; app always runnable)

1. **Foundation (25m):** repo, optional compose file, CI, backend skeleton (security, error handling, request-id, CORS, OpenAPI, health), Flyway V1, frontend skeleton with the full design system tokens/fonts, sidebar shell, login page, API client with auth. **Deploy all three now.**
2. **Books + inventory (35m):** backend + tests for R1–R3, R7; Books list (table/grid), add/edit sheet with ISBN auto-fill, book detail with stock actions and timeline.
3. **Members (20m):** backend + tests; list, detail, sheets, eligibility endpoint.
4. **Circulation (35m):** issue/return/renew + fines with locking + tests for R4–R6 and the concurrency test; the Circulation page; renew/return from member and book pages.
5. **Dashboard, fines, inventory ledger, activity, ⌘K, CSV export, dark mode (35m).**
6. **Seed + polish + docs (20m):** V2 seed with consistency test, empty states, responsive pass, README with screenshots, DECISIONS.md, final deploy and a smoke test on the live URLs.
7. **Tier 3** only if time remains.

If behind schedule: never cut Tier 1 or its tests. Cut in this order: Tier 3 → activity page → CSV export → grid view → dark mode.

## 13. Definition of done (brief checklist)

- [ ] Books: create, view, update, delete, search, filter, sort
- [ ] Members: register and manage
- [ ] Stock additions/reductions recorded in inventory history
- [ ] Issue validates member eligibility and available stock
- [ ] Return validates the borrowing record and updates stock
- [ ] ISBN uniqueness, stock limits, 3-book limit, 14-day duration enforced in the backend and tested
- [ ] App starts using only the README; live demo works with the demo credentials
